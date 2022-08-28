package com.xuorig.chromatophore.example.chromatophore

import org.springframework.data.repository.CrudRepository

interface ChromatophoreClientRepository: CrudRepository<ChromatophoreClient, Int> {
    fun findByName(name: String): ChromatophoreClient?
}