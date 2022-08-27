package com.xuorig.chromatophore

import java.time.Instant

typealias FieldCoordinates = String

data class FieldVersionInfo(
    val version: Int,
    val firstRequested: Instant
)

interface ClientVersionIndex {
    fun getField(coordinates: FieldCoordinates): FieldVersionInfo?
}

class DefaultClientVersionIndex(private val fieldMapping: Map<String, FieldVersionInfo>): ClientVersionIndex {
    override fun getField(coordinates: FieldCoordinates): FieldVersionInfo? {
        return fieldMapping[coordinates]
    }
}

class NullClientVersionIndex(): ClientVersionIndex {
    override fun getField(coordinates: FieldCoordinates): FieldVersionInfo? {
        return null
    }
}