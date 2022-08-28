package com.xuorig.chromatophore.instrumentation

import com.xuorig.chromatophore.CHROMATOPHORE_VERSION_DIRECTIVE
import com.xuorig.chromatophore.ChromatophoreStore
import com.xuorig.chromatophore.FieldVersionInfo
import graphql.ExecutionResult
import graphql.GraphQLContext
import graphql.execution.instrumentation.InstrumentationContext
import graphql.execution.instrumentation.InstrumentationState
import graphql.execution.instrumentation.SimpleInstrumentation
import graphql.execution.instrumentation.SimpleInstrumentationContext
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import graphql.execution.instrumentation.parameters.InstrumentationFieldParameters
import graphql.schema.GraphQLAppliedDirective
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLNamedOutputType
import graphql.schema.GraphQLOutputType
import java.time.Instant
import java.util.concurrent.CompletableFuture

/**
 * The version collector is a graphql-java [SimpleInstrumentation]
 * that records which version of the schema fields a client is using.
 */
class VersionCollectionInstrumentation(
    private val persistenceAdapter: ChromatophoreStore,
    private val clientIdFromContext: ClientIdContextExtractor
) : SimpleInstrumentation() {
    override fun createState(parameters: InstrumentationCreateStateParameters): InstrumentationState {
        return VersionCollectorState()
    }

    override fun beginField(
        parameters: InstrumentationFieldParameters,
    ): InstrumentationContext<ExecutionResult> {
        return object: SimpleInstrumentationContext<ExecutionResult>() {
            override fun onCompleted(result: ExecutionResult, t: Throwable?) {
                val collector = parameters.getInstrumentationState<VersionCollectorState>()
                val parentType = parameters.executionStepInfo.parent.type
                collector.addField(parentType, parameters.field)
            }
        }
    }

    override fun instrumentExecutionResult(
        executionResult: ExecutionResult,
        parameters: InstrumentationExecutionParameters,
    ): CompletableFuture<ExecutionResult> {
        val collector = parameters.getInstrumentationState<VersionCollectorState>()
        val result = super.instrumentExecutionResult(executionResult, parameters)

        val clientId = clientIdFromContext.extract(parameters.graphQLContext) ?: return result
        persistenceAdapter.persistClientIndex(clientId, collector.index)

        return result
    }
}

class VersionCollectorState : InstrumentationState {
    val index = mutableMapOf<String, FieldVersionInfo>()

    private val now: Instant = Instant.now()

    fun addField(parent: GraphQLOutputType, field: GraphQLFieldDefinition) {
        val namedType = parent as GraphQLNamedOutputType

        val directive = supersedingDirective(field)

        val fieldKey = "${namedType.name}.${field.name}"

        val version = if (directive != null) {
            directive.getArgument("number").getValue()
        } else {
            0
        }

        index[fieldKey] = FieldVersionInfo(
            version = version,
            firstRequested = now
        )
    }

    private fun supersedingDirective(field: GraphQLFieldDefinition): GraphQLAppliedDirective? {
        return field.appliedDirectives.find { directive -> directive.name == CHROMATOPHORE_VERSION_DIRECTIVE }
    }
}