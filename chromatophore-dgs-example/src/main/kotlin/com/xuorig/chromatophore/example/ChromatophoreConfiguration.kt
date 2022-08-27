package com.xuorig.chromatophore.example

import com.xuorig.chromatophore.InMemoryStore
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class ChromatophoreConfiguration {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    fun chromatophoreInMemoryStore(): InMemoryStore {
        return InMemoryStore()
    }
}