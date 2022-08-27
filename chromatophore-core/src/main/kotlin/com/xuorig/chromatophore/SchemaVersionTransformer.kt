package com.xuorig.chromatophore

import graphql.GraphQL
import graphql.schema.*

class SchemaVersionTransformer(private val persistenceAdapter: ChromophoreStore) {
    fun versionSchema(
        schema: GraphQLSchema,
        clientId: String,
    ): GraphQLSchema {
        val clientIndex = persistenceAdapter.getClientIndex(clientId)

        val visitor = if (clientIndex == null) {
            SchemaVersionTransformVisitor(NullClientVersionIndex())
        } else {
            SchemaVersionTransformVisitor(DefaultClientVersionIndex(clientIndex))
        }

        return SchemaTransformer.transformSchema(schema, visitor)
    }
}

