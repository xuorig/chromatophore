package com.xuorig.chromatophore.example.internalapi

import com.xuorig.chromatophore.InMemoryStore
import graphql.GraphQLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

data class ChromatophoreField(
    val originalFieldName: String,
    val version: Int
)

@Controller
class InternalController(
    @Autowired val chromatophoreInMemoryStore: InMemoryStore
) {
    @SchemaMapping(field = "_chromatophore", typeName = "Query")
    fun chromatophore(graphQLContext: GraphQLContext): List<ChromatophoreField> {
        val clientId: String = graphQLContext["chromatophore.clientId"]

        return chromatophoreInMemoryStore.store[clientId]!!.entries.map {
            ChromatophoreField(it.key, it.value.version)
        }
    }
}