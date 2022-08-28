package com.xuorig.chromatophore.example.chromatophore

import com.xuorig.chromatophore.ChromatophoreStore
import com.xuorig.chromatophore.FieldVersionInfo
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * TODO: this is a very naive implementation of storing chromatophore field info.
 * Only for demo purposes.
 */
@Component
class ChromatophoreMysqlStore(
    val clientRepository: ChromatophoreClientRepository,
    val fieldTransformRepository: ChromatophoreFieldTransformRepository
): ChromatophoreStore {
    override fun persistClientIndex(clientId: String, index: Map<String, FieldVersionInfo>) {
        var client = clientRepository.findByName(clientId)

        if (client == null) {
           client = clientRepository.save(ChromatophoreClient(name = clientId))
        }

        val existingFields = fieldTransformRepository.findAllByClientId(client.id!!).map { it.fieldName }.toSet()

        val fieldTransforms = index.filter { entry -> entry.key !in existingFields }.map { (fieldName, version) ->
            ChromatophoreFieldTransform(
                fieldName = fieldName,
                version = version.version,
                client = client
            )
        }

        fieldTransformRepository.saveAll(fieldTransforms)
    }

    override fun getClientIndex(clientId: String): Map<String, FieldVersionInfo>? {
        var client = clientRepository.findByName(clientId) ?: return null
        val existingFields = fieldTransformRepository.findAllByClientId(client.id!!)

        val index = mutableMapOf<String, FieldVersionInfo>()

        for (field in existingFields) {
            // TODO: Persist first requested
            index[field.fieldName] = FieldVersionInfo(field.version, firstRequested = Instant.now())
        }

        return index
    }
}