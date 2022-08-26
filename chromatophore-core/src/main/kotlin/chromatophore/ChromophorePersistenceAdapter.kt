package chromatophore

/**
 * Chromophore needs to persist state about the field versions a client expects to see.
 *
 */
interface ChromophorePersistenceAdapter {
    fun persistClientIndex(clientId: String, index: Map<String, FieldVersionInfo>)
    fun getClientIndex(clientId: String): Map<String, FieldVersionInfo>?
}