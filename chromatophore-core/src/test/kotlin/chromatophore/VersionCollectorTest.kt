package chromatophore

import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.schema.idl.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VersionCollectorTest {
    @Test
    fun `collects default versions for all fields`() {
        val sdl = """
            type Query {
              product: Product
            }
            
            type Product {
              name: String
              price: Int
              description: String
              image(size: Int): Image
            }
            
            type Image {
              size: Int
              alt: String
              url: String
            }
        """.trimIndent()

        val graphQLSchema = buildSchema(sdl, EchoingWiringFactory.newEchoingWiring())
        val mmrAdapter = InMemoryPersitenceAdapter()
        val versionCollector = VersionCollector(mmrAdapter) {
            it["chromatophore.clientId"]
        }
        val build = GraphQL.newGraphQL(graphQLSchema).instrumentation(versionCollector).build()

        val query = "{ product { name price description image(size: 4) { size alt url } } }"
        val executionInput = ExecutionInput.newExecutionInput().query(query).graphQLContext(mapOf("chromatophore.clientId" to "client1"))
        build.execute(executionInput.build())

        val clientIndex = mmrAdapter.store["client1"]
        assertNotNull(clientIndex, "No client index was created for client1")

        assertEquals(8, clientIndex.keys.size)
        assertNotNull(clientIndex["Query.product"])
        assertEquals(0, clientIndex["Product.name"]!!.version, "Expected field to have same signature as original field name")
    }

    @Test
    fun `collects superseded versions for fields`() {
        val sdl = """
            directive @supersedesFieldInternal(version: String) on FIELD_DEFINITION
            
            type Query {
              product: Product @supersedesFieldInternal(version: "2")
            }
            
            type Product {
              name: String
              price: Int
              description: String
              image(size: Int): Image
            }
            
            type Image {
              size: Int
              alt: String
              url: String
            }
        """.trimIndent()

        val graphQLSchema = buildSchema(sdl, EchoingWiringFactory.newEchoingWiring())

        val mmrAdapter = InMemoryPersitenceAdapter()
        val versionCollector = VersionCollector(mmrAdapter) {
            it["chromatophore.clientId"]
        }
        val build = GraphQL.newGraphQL(graphQLSchema).instrumentation(versionCollector).build()

        val query = "{ product { name price description image(size: 4) { size alt url } } }"
        val executionInput = ExecutionInput.newExecutionInput().query(query).graphQLContext(mapOf("chromatophore.clientId" to "client1"))

        val clientIndex = mmrAdapter.store["client1"]
        assertNotNull(clientIndex, "No client index was created for client1")

        assertEquals(8, clientIndex.keys.size)
        assertNotNull(clientIndex["Query.product"])
        assertEquals(2, clientIndex["Query.product"]!!.version, "Expected field to have same signature as original field name")
    }
}