package com.xuorig.chromatophore.example

import com.xuorig.chromatophore.InMemoryStore
import com.xuorig.chromatophore.SchemaVersionTransformer
import com.xuorig.chromatophore.VersionCollector
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.ExecutionGraphQlRequest
import org.springframework.graphql.ExecutionGraphQlResponse
import org.springframework.graphql.ExecutionGraphQlService
import org.springframework.graphql.execution.BatchLoaderRegistry
import org.springframework.graphql.execution.DefaultExecutionGraphQlService
import org.springframework.graphql.execution.GraphQlSource
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ChromatophoreExecutionService(
    @Autowired val chromatophoreInMemoryStore: InMemoryStore,
    @Autowired val graphQlSource: GraphQlSource,
    @Autowired val batchLoaderRegistry: BatchLoaderRegistry
) : ExecutionGraphQlService {
    private val schemaTransformer = SchemaVersionTransformer(chromatophoreInMemoryStore)

    override fun execute(request: ExecutionGraphQlRequest): Mono<ExecutionGraphQlResponse> {
        val input = request.toExecutionInput()
        val clientId: String = input.graphQLContext["chromatophore.clientId"]
        val transformed = schemaTransformer.versionSchema(graphQlSource.schema(), clientId)

        val wrappedGraphQL = graphQlSource.graphQl().transform { builder ->
            builder.schema(transformed)
            builder.instrumentation(VersionCollector(chromatophoreInMemoryStore) {
                it["chromatophore.clientId"]
            })
        }

        val chromatophoreGraphQLSource = ChromatophoreGraphQLSource(wrappedGraphQL)

        val default = DefaultExecutionGraphQlService(chromatophoreGraphQLSource)
        default.addDataLoaderRegistrar(batchLoaderRegistry);
        return default.execute(request)
    }
}

class ChromatophoreGraphQLSource(private val graphQL: GraphQL) : GraphQlSource {
    override fun graphQl(): GraphQL {
        return graphQL
    }

    override fun schema(): GraphQLSchema {
        return graphQL.graphQLSchema
    }

}