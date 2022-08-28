package com.xuorig.chromatophore.instrumentation

import graphql.GraphQLContext

fun interface ClientIdContextExtractor {
    fun extract(ctx: GraphQLContext): String?
}