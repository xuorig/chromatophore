package com.xuorig.chromatophore.instrumentation

import com.xuorig.chromatophore.ChromatophoreStore
import com.xuorig.chromatophore.SchemaVersionTransformer
import graphql.GraphQLContext
import graphql.execution.instrumentation.SimpleInstrumentation
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import graphql.schema.GraphQLSchema

/**
 * [SchemaTransformInstrumentation] is a GraphQL-Java [SimpleInstrumentation] that
 * transforms and versions the schema on every request based on clientId.
 */
class SchemaTransformInstrumentation(
    private val persistenceAdapter: ChromatophoreStore,
    private val clientIdFromContext: ClientIdContextExtractor
) : SimpleInstrumentation() {
    private val transformer = SchemaVersionTransformer(persistenceAdapter)

    override fun instrumentSchema(
        schema: GraphQLSchema,
        parameters: InstrumentationExecutionParameters,
    ): GraphQLSchema {
        val clientId = clientIdFromContext.extract(parameters.graphQLContext) ?:
            return super.instrumentSchema(schema, parameters)

        return transformer.versionSchema(schema, clientId)
    }
}
