package com.xuorig.chromatophore.autoconfig

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

/**
 * Configuration properties for Chromatophore Spring Boot Integration.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = ChromatophoreConfigurationProperties.PREFIX)
@Suppress("ConfigurationProperties")
class ChromatophoreConfigurationProperties(
    @DefaultValue(DEFAULT_CLIENT_ID_CONTEXT_KEY) val clientIdContextKey: String,
    @DefaultValue(DEFAULT_CLIENT_ID_HEADER_NAME) val clientIdHeaderName: String
) {
    companion object {
        const val PREFIX: String = "chromatophore"

        const val DEFAULT_CLIENT_ID_CONTEXT_KEY = "chromatophore.clientId"
        const val DEFAULT_CLIENT_ID_HEADER_NAME = "Chromatophore-Client-Id"
    }
}