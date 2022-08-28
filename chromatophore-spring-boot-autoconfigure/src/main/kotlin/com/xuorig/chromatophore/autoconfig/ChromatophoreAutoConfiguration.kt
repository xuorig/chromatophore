package com.xuorig.chromatophore.autoconfig

import com.xuorig.chromatophore.ChromatophoreStore
import com.xuorig.chromatophore.InMemoryStore
import com.xuorig.chromatophore.instrumentation.ClientIdContextExtractor
import com.xuorig.chromatophore.instrumentation.SchemaTransformInstrumentation
import com.xuorig.chromatophore.instrumentation.VersionCollectionInstrumentation
import graphql.execution.instrumentation.Instrumentation
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.server.WebGraphQlInterceptor

@Configuration
@EnableConfigurationProperties(ChromatophoreConfigurationProperties::class)
open class ChromatophoreAutoConfiguration(
    private val configProps: ChromatophoreConfigurationProperties
) {
    @Bean
    @ConditionalOnMissingBean
    open fun clientIdContextExtrator(): ClientIdContextExtractor {
        return ClientIdContextExtractor { context -> context[configProps.clientIdContextKey] }

    }

    @Bean
    @ConditionalOnMissingBean
    open fun chromatophoreStore(): ChromatophoreStore {
        return InMemoryStore()
    }

    @Bean
    open fun schemaInstrumentation(store: ChromatophoreStore, clientIdFromContext: ClientIdContextExtractor): Instrumentation {
        return SchemaTransformInstrumentation(store, clientIdFromContext)
    }

    @Bean
    open fun collectorInstrumentation(store: ChromatophoreStore, clientIdFromContext: ClientIdContextExtractor): Instrumentation {
        return VersionCollectionInstrumentation(store, clientIdFromContext)
    }

    @Bean
    open fun clientIdHeaderInterceptor(): WebGraphQlInterceptor {
        return ClientIdHeaderInterceptor(configProps.clientIdHeaderName, configProps.clientIdContextKey)
    }
}