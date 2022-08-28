package com.xuorig.chromatophore.autoconfig

import org.springframework.graphql.server.WebGraphQlInterceptor
import org.springframework.graphql.server.WebGraphQlRequest
import org.springframework.graphql.server.WebGraphQlResponse
import reactor.core.publisher.Mono

class ClientIdHeaderInterceptor(private val clientIdHeaderName: String, private val clientIdContextKey: String) : WebGraphQlInterceptor {
    override fun intercept(request: WebGraphQlRequest, chain: WebGraphQlInterceptor.Chain): Mono<WebGraphQlResponse> {
        val clientId = request.headers[clientIdHeaderName]?.first()

        if (clientId !== null) {
            request.configureExecutionInput { _, builder ->
                builder.graphQLContext(mapOf(clientIdContextKey to clientId)).build()
            }
        }

        return chain.next(request)
    }
}