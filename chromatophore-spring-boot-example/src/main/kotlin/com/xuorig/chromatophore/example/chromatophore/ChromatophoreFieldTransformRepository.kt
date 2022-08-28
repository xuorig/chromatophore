package com.xuorig.chromatophore.example.chromatophore

import org.springframework.data.repository.CrudRepository

interface ChromatophoreFieldTransformRepository: CrudRepository<ChromatophoreFieldTransform, Int> {
    fun findAllByClientId(clientId: Int): List<ChromatophoreFieldTransform>
}
