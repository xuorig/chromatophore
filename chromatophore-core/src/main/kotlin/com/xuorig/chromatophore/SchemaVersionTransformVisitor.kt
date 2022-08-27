package com.xuorig.chromatophore

import graphql.Scalars
import graphql.language.IntValue
import graphql.schema.*
import graphql.util.TraversalControl
import graphql.util.TraverserContext

const val CHROMATOPHORE_SUPERSEDES_FIELD_DIRECTIVE = "supersedesField"
const val CHROMATOPHORE_VERSION_DIRECTIVE = "chromatophoreVersion"

class SchemaVersionTransformVisitor(private val clientIndex: ClientVersionIndex) : GraphQLTypeVisitorStub() {
    override fun visitGraphQLObjectType(
        node: GraphQLObjectType,
        context: TraverserContext<GraphQLSchemaElement>
    ): TraversalControl {
        val supersedingFields = mutableMapOf<String, MutableSet<SupersedingField>>()

        // Collect all possible superseding fields
        node.fields.forEach { field ->
            val dir = field.appliedDirectives.find { it.name == CHROMATOPHORE_SUPERSEDES_FIELD_DIRECTIVE }

            if (dir != null) {
                val fieldToReplace = dir.getArgument("field").getValue<String>()
                val version = dir.getArgument("version").getValue<Int>()
                supersedingFields.putIfAbsent(fieldToReplace, mutableSetOf())
                supersedingFields[fieldToReplace]!!.add(SupersedingField(field, version))
            }
        }

        // First, remove all fields that are going to be replaced, or are replacing fields
        val willReplaceNames = supersedingFields.values.flatMap { it.map { it.fieldDefinition.name } }
        val filteredFields = node.fields.filter { it.name !in supersedingFields && it.name !in willReplaceNames }

        // Then we build the new fields matching for the right requested version.
        // When there is no requested version, we take the most recent version.
        val newFields = supersedingFields.map { (replacedName, potentialFields) ->
            val fieldKey = "${node.name}.${replacedName}"
            val clientVersion = clientIndex.getField(fieldKey)

            val matchingField = if (clientVersion != null) {
                potentialFields.find { it.version == clientVersion.version }
            } else {
                potentialFields.maxByOrNull { it.version }
            }

            matchingField?.fieldDefinition?.transform {
                it.name(replacedName).withAppliedDirective(versionDirective(matchingField.version))
            }
        }.filterNotNull()

        return changeNode(context, node.transform { it.replaceFields(filteredFields + newFields) })
    }

    private fun versionDirective(version: Int): GraphQLAppliedDirective {
        return GraphQLAppliedDirective.newDirective().name(CHROMATOPHORE_VERSION_DIRECTIVE).argument {
            it.name("number").type(Scalars.GraphQLInt).valueLiteral(IntValue(version.toBigInteger()))
        }.build()
    }

    internal data class SupersedingField(
        val fieldDefinition: GraphQLFieldDefinition,
        val version: Int
    )
}