package com.xuorig.chromatophore.example

import org.springframework.graphql.server.WebGraphQlInterceptor
import org.springframework.graphql.server.WebGraphQlRequest
import org.springframework.graphql.server.WebGraphQlResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ClientIdInterceptor: WebGraphQlInterceptor {
    override fun intercept(request: WebGraphQlRequest, chain: WebGraphQlInterceptor.Chain): Mono<WebGraphQlResponse> {
        val clientId = request.headers["chromatophore-client-id"]?.first()

        if (clientId !== null) {
            request.configureExecutionInput { _, builder ->
                builder.graphQLContext(mapOf("chromatophore.clientId" to clientId)).build()
            }
        }

        return chain.next(request)
    }
}