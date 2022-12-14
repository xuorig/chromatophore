package com.xuorig.chromatophore

import graphql.schema.*

class SchemaVersionTransformer(private val persistenceAdapter: ChromatophoreStore) {
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

