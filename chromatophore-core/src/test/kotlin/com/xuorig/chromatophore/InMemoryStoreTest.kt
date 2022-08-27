package com.xuorig.chromatophore

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class InMemoryStoreTest {

    @Test
    fun `persist merges indexes`() {
        val store = InMemoryStore()

        store.persistClientIndex(
            "client1", mapOf(
                "Product.name" to FieldVersionInfo(0, Instant.now())
            )
        )

        store.persistClientIndex(
            "client1", mapOf(
                "Product.description" to FieldVersionInfo(0, Instant.now())
            )
        )

        assertEquals(2, store.getClientIndex("client1")!!.size)

        store.persistClientIndex(
            "client1", mapOf(
                "Product.name" to FieldVersionInfo(1, Instant.now())
            )
        )

        assertEquals(1, store.getClientIndex("client1")!!["Product.name"]!!.version)
    }
}