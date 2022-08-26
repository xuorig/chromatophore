package chromatophore

import graphql.schema.GraphQLSchema
import graphql.schema.idl.*

fun buildSchema(sdl: String, runtimeWiring: RuntimeWiring): GraphQLSchema {
    val schemaParser = SchemaParser()
    val typeDefinitionRegistry: TypeDefinitionRegistry = schemaParser.parse(sdl)
    val schemaGenerator = SchemaGenerator()
    return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)
}