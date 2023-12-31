@file:Suppress("LocalVariableName")

package pw.mihou.traveler.features.commands.schema

import pw.mihou.traveler.coroutines.coroutine
import pw.mihou.traveler.features.commands.MessageCommand
import pw.mihou.traveler.features.commands.MessageCommandEvent
import pw.mihou.traveler.features.commands.options.MessageCommandOption
import java.lang.RuntimeException
import kotlin.text.StringBuilder

typealias SchemaOptions = Map<String, Any?>
typealias SchemaDefinition = List<SchemaDefinitionOption>

object SchemaDecoder {

    private val cache = mutableMapOf<String, SchemaDefinition>()

    fun load(command: MessageCommand): Boolean {
        var ok = true
        command.schemas?.forEach { schema ->
            coroutine {
                try {
                    if (cache.containsKey(schema)) return@coroutine
                    val definition = decode(command.name, schema)
                    cache[schema] = definition
                } catch (ex: SchemaDecodeException) {
                    ex.printStackTrace()
                    ok = false
                }
            }
        }
        return ok
    }

    fun scan(ev: MessageCommandEvent, schema: String): SchemaResult {
        val definitions = cache.computeIfAbsent(schema) {
            decode(ev.command.name, schema)
        }
        if (definitions.size != ev.options.size) {
            return SchemaResult(false, emptyMap())
        }
        val options = mutableMapOf<String, Any?>()

        for ((index, option) in ev.options.withIndex()) {
            val definition = definitions[index]
            val `$cache` = ev.`$schema$cache`?.get(definition.name)
            if (`$cache` != null) {
                val (cached, cachedType) = `$cache`
                if (definition.type != cachedType) {
                    return SchemaResult(false, emptyMap())
                }

                options[definition.name] = cached
                continue
            }
            val value: Any = get(option, definition.type) ?: return SchemaResult(false, emptyMap())
            if (ev.`$schema$cache` == null) {
                throw IllegalStateException("Message ${ev.messageId}'s schema cache is null even though schema scanning isn't complete yet.")
            }
            ev.`$schema$cache`!![definition.name] = value to definition.type
            options[definition.name] = value
        }

        return SchemaResult(true, options)
    }

    private fun get(option: MessageCommandOption, type: SchemaOptionTypes): Any? {
        return when(type) {
            SchemaOptionTypes.Identifier -> Identifier
            SchemaOptionTypes.Double -> option.double
            SchemaOptionTypes.Boolean -> option.boolean
            SchemaOptionTypes.Float -> option.float
            SchemaOptionTypes.Integer -> option.int
            SchemaOptionTypes.Long -> option.long
            SchemaOptionTypes.Channel -> option.channelId
            SchemaOptionTypes.Role -> option.roleId
            SchemaOptionTypes.Text -> option.textRepresentation
            SchemaOptionTypes.User -> option.userId
            SchemaOptionTypes.Message -> option.messageLink
            SchemaOptionTypes.CustomEmoji -> option.customEmojiId
        }
    }

    fun decode(command: String, schema: String): SchemaDefinition {
        val blocks = mutableListOf<SchemaDefinitionOption>()

        val currentName = StringBuilder()
        val currentType = StringBuilder()

        var inEnclosure = false
        var destination = SchemaDefinitionDestination.Type

        for ((index, char) in schema.withIndex()) {
            if (char == '[') {
                if (inEnclosure) {
                    throw SchemaDecodeException(command, "Use of '[' key is disallowed for any other purposes other than marking the start of an option.")
                }

                inEnclosure = true
                continue
            }

            if (char == ']') {
                if (!inEnclosure) {
                    throw SchemaDecodeException(command, "Use of ']' key is disallowed for any other purpose other than marking the end of an option.")
                }

                destination = SchemaDefinitionDestination.Type
                inEnclosure = false

                if (index != (schema.length - 1)) {
                    continue
                }
            }

            if (char == ':') {
                if (!inEnclosure || destination == SchemaDefinitionDestination.Name) {
                    throw SchemaDecodeException(command, "Use of ':' is disallowed for any other purpose other than marking the separation between an option's name and type.")
                }

                destination = SchemaDefinitionDestination.Name
                continue
            }

            if (char == ' ' || index == (schema.length - 1)) {
                if (!inEnclosure) {
                    // We are dealing with an identifier.
                    if (currentName.isNotEmpty() && currentType.isEmpty()) {
                        blocks += SchemaDefinitionOption(name = "identifier:$currentName", type = SchemaOptionTypes.Identifier)

                        currentName.clear()
                        currentType.clear()
                        continue
                    }

                    if (currentName.isNotEmpty() && currentType.isNotEmpty()) {
                        val constructedName = currentName.toString()
                        val constructedType = currentType.toString().lowercase()

                        val identifiedType = SchemaOptionTypes.find(constructedType)
                            ?: throw SchemaDecodeException(command, "Unknown type for option $constructedName, there is no such type as '$constructedType'.")

                        // Strictly do not allow the use of :logical_identifier, this allows us to be more strict with the syntax appearance.
                        if (identifiedType == SchemaOptionTypes.Identifier) {
                            throw SchemaDecodeException(command, "Option $constructedName cannot use the type $constructedType as it's a special type.")
                        }


                        blocks += SchemaDefinitionOption(name = constructedName, type = identifiedType)

                        currentName.clear()
                        currentType.clear()
                        continue
                    }

                    // Strictly do not allow double-spacing.
                    throw SchemaDecodeException(command, "Double-space is not allowed in a schema definition.")
                }
                continue
            }

            when(destination) {
                SchemaDefinitionDestination.Name -> currentName.append(char)
                SchemaDefinitionDestination.Type -> currentType.append(char)
            }
        }

        if (blocks.isEmpty()) {
            throw SchemaDecodeException(command, "No property in the schema was decoded properly.")
        }
        return blocks
    }
}

class SchemaDecodeException(command: String, message: String): RuntimeException("Cannot decode schema for command $command: $message")
data class SchemaResult(val matches: Boolean, val options: SchemaOptions)
data class SchemaDefinitionOption(val name: String, val type: SchemaOptionTypes)

enum class SchemaOptionTypes(val key: String) {
    User("user"), Channel("channel"), Role("role"), Message("message"), CustomEmoji("emoji"),
    Text("string"), Integer("int"),  Long("long"), Boolean("boolean"), Float("float"), Double("double"),
    Identifier(":logical_identifier");

    companion object {
        private val `enum$cachedEntries` = entries
        fun find(name: String) = `enum$cachedEntries`.firstOrNull { it.key == name }
    }
}

enum class SchemaDefinitionDestination {
    Name, Type
}

object Identifier