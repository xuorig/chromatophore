package com.xuorig.chromatophore

import graphql.schema.GraphQLObjectType
import graphql.schema.idl.*
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SchemaVersionTransformerTest {
    @Test
    fun `replaces with most recent versions for brand new client`() {
        val sdl = """
            directive @supersedesField(field: String!, version: Int = 1) on FIELD_DEFINITION
            
            type Query {
              product: Product
            }
            
            type Product {
              name: String
              price: Int
              priceV2: Price @supersedesField(field: "price", version: 1)
              description: String
            }
            
            type Price {
              cents: Int
            }
        """.trimIndent()

        val graphQLSchema = buildSchema(sdl, EchoingWiringFactory.newEchoingWiring())

        val transformed = SchemaVersionTransformer(InMemoryStore()).versionSchema(graphQLSchema, "client1")

        val productType = transformed.getType("Product") as GraphQLObjectType
        val priceField = productType.getField("price")

        // price field should be of Money type
        assertEquals("Price", (priceField.type as GraphQLObjectType).name)

        val versionArgument = priceField.getAppliedDirective(CHROMATOPHORE_VERSION_DIRECTIVE).getArgument("number")
        val versionNumber = versionArgument.getValue<Int>()
        assertEquals(1, versionNumber)

        assertNull(productType.getField("priceV2"))
    }

    @Test
    fun `replaces with client index version`() {
        val sdl = """
            directive @supersedesField(field: String!, version: Int = 1) on FIELD_DEFINITION
            
            type Query {
              product: Product
            }
            
            type Product {
              name: String
              price: Int
              priceV2: Price @supersedesField(field: "price", version: 1)
              priceV3: Price2 @supersedesField(field: "price", version: 2)
              description: String
            }
            
            type Price {
              cents: Int
            }
            
            type Price2 {
              cents: Int
            }
        """.trimIndent()

        val graphQLSchema = buildSchema(sdl, EchoingWiringFactory.newEchoingWiring())

        val adapter = InMemoryStore()
        adapter.persistClientIndex("client1", mutableMapOf(
            "Product.price" to FieldVersionInfo(1, firstRequested = Instant.now())
        ))

        val transformed = SchemaVersionTransformer(adapter).versionSchema(graphQLSchema, "client1")

        val productType = transformed.getType("Product") as GraphQLObjectType
        val priceField = productType.getField("price")

        // price field should be of Money type
        assertEquals("Price", (priceField.type as GraphQLObjectType).name)

        // version 1 was selected
        val versionArgument = priceField.getAppliedDirective(CHROMATOPHORE_VERSION_DIRECTIVE).getArgument("number")
        val versionNumber = versionArgument.getValue<Int>()
        assertEquals(1, versionNumber)

        assertNull(productType.getField("priceV2"))
        assertNull(productType.getField("priceV3"))
    }
}