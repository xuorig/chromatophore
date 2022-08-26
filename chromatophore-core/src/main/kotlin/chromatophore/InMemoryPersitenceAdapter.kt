package chromatophore

class InMemoryPersitenceAdapter(): ChromophorePersistenceAdapter {
    val store = mutableMapOf<String, Map<String, FieldVersionInfo>>()

    override fun persistClientIndex(clientId: String, index: Map<String, FieldVersionInfo>) {
        store[clientId] = index
    }

    override fun getClientIndex(clientId: String): Map<String, FieldVersionInfo>? {
        return store[clientId]
    }
}