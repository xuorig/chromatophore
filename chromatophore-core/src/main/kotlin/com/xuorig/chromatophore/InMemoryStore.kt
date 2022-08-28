package com.xuorig.chromatophore

class InMemoryStore(): ChromatophoreStore {
    val store = mutableMapOf<String, Map<String, FieldVersionInfo>>()

    override fun persistClientIndex(clientId: String, index: Map<String, FieldVersionInfo>) {
        store.compute(clientId) { _, existingIndex ->
            if (existingIndex == null) {
                index
            } else {
                existingIndex + index
            }
        }
    }

    override fun getClientIndex(clientId: String): Map<String, FieldVersionInfo>? {
        return store[clientId]
    }
}