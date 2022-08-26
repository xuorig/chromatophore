package chromatophore

import graphql.ExecutionResult
import graphql.GraphQLContext
import graphql.execution.instrumentation.InstrumentationContext
import graphql.execution.instrumentation.InstrumentationState
import graphql.execution.instrumentation.SimpleInstrumentation
import graphql.execution.instrumentation.SimpleInstrumentationContext
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import graphql.execution.instrumentation.parameters.InstrumentationFieldParameters
import graphql.schema.*
import java.time.Instant
import java.util.concurrent.CompletableFuture

/**
 * The version collector records which version of the schema fields a client is using
 */
class VersionCollector(
    private val persistenceAdapter: ChromophorePersistenceAdapter,
    private val clientIdFromContext: (ctx: GraphQLContext) -> String
) : SimpleInstrumentation() {
    override fun createState(parameters: InstrumentationCreateStateParameters): InstrumentationState {
        return VersionCollectorState()
    }

    override fun beginField(
        parameters: InstrumentationFieldParameters,
        state: InstrumentationState
    ): InstrumentationContext<ExecutionResult> {
        return object: SimpleInstrumentationContext<ExecutionResult>() {
            override fun onCompleted(result: ExecutionResult, t: Throwable?) {
                val collector = state as VersionCollectorState
                val parentType = parameters.executionStepInfo.parent.type
                collector.addField(parentType, parameters.field)
            }
        }
    }

    override fun instrumentExecutionResult(
        executionResult: ExecutionResult,
        parameters: InstrumentationExecutionParameters,
        state: InstrumentationState
    ): CompletableFuture<ExecutionResult> {
        val result = super.instrumentExecutionResult(executionResult, parameters, state)
        val collector = state as VersionCollectorState
        val clientId = clientIdFromContext(parameters.graphQLContext)
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
            directive.getArgument("version").getValue<Int>()
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