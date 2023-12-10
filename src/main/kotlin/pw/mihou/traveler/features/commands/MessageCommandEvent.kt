package pw.mihou.traveler.features.commands

import kotlinx.coroutines.future.await
import org.javacord.api.DiscordApi
import org.javacord.api.event.message.MessageCreateEvent
import pw.mihou.traveler.features.commands.options.MessageCommandOption
import pw.mihou.traveler.features.commands.options.MessageParameters
import pw.mihou.traveler.features.commands.schema.Identifier
import pw.mihou.traveler.features.commands.schema.SchemaDecoder
import pw.mihou.traveler.features.commands.schema.SchemaOptionTypes
import pw.mihou.traveler.features.commands.schema.SchemaOptions
import java.lang.IllegalArgumentException
import kotlin.jvm.optionals.getOrNull

class MessageCommandEvent(
    val options: List<MessageCommandOption>,
    val originalEvent: MessageCreateEvent,
    val command: MessageCommand
) {
    val channel get() = originalEvent.channel
    val serverTextChannel get() = originalEvent.serverTextChannel.getOrNull()
    val privateChannel get() = originalEvent.privateChannel.getOrNull()
    val serverVoiceChannel get() = originalEvent.serverVoiceChannel.getOrNull()
    val serverThreadChannel get() = originalEvent.serverThreadChannel.getOrNull()

    val readableMessageContent get() = originalEvent.readableMessageContent

    val isServerMessage get() = originalEvent.isServerMessage
    val isPrivateMessage get() = originalEvent.isPrivateMessage

    val api get() = originalEvent.api
    val author get() = originalEvent.messageAuthor
    val content get() = originalEvent.messageContent
    val message get() = originalEvent.message
    val messageLink get() = originalEvent.messageLink
    val messageId get() = originalEvent.messageId

    val server get() = originalEvent.server.getOrNull()

    // Speed up scanning through schema by not doing a ton of regex each schema.
    internal var `$schema$cache`: MutableMap<String, Pair<Any, SchemaOptionTypes>>? = mutableMapOf()

    private var `$schema`: MessageCommandEventSchema? = null
    val store = command.store.toMutableMap()

    val schema: MessageCommandEventSchema? get() {
        if (`$schema` == null) {
            val schemas = command.schemas
                ?: throw IllegalArgumentException("Command ${command.name} does not have any schema definitions.")
            for (schema in schemas) {
                val result = SchemaDecoder.scan(this, schema)
                if (result.matches) {
                    `$schema` = MessageCommandEventSchema(api, schema, result.options)
                    `$schema$cache` = null
                    break
                }
            }
        }

        return `$schema`
    }

    /**
     * Gets the value of the given key from the [MessageCommandEvent.store] and maps it into the type given
     * if possible, otherwise returns null.
     *
     * @param key   The key to get from the [MessageCommandEvent.store].
     * @param type  The type expected of the value.
     * @param <T>   The type expected of the value.
     *
     * @return The value mapped with the key in [MessageCommandEvent.store] mapped to the type, otherwise null.
    </T> */
    inline fun <reified T> get(key: String): T? {
        if (!store.containsKey(key)) return null
        val `object` = store[key]
        return if (`object` is T) `object` else null
    }

    fun getOption(index: Int) = options.getOrNull(index)
}

data class MessageCommandEventSchema(private val api: DiscordApi, val matched: String, val options: SchemaOptions) {
    private inline fun <reified T> Any.cast(): T? {
        return if (this is T) this else null
    }

    fun isIdentifierPresent(name: String) = options[name]?.cast<Identifier>() != null

    fun getArgumentStringByName(name: String) = options[name]?.cast<String>()
    fun getArgumentLongByName(name: String) = options[name]?.cast<Long>()
    fun getArgumentIntegerByName(name: String) = options[name]?.cast<Int>()
    fun getArgumentBooleanByName(name: String) = options[name]?.cast<Boolean>()
    fun getArgumentDoubleByName(name: String) = options[name]?.cast<Double>()
    fun getArgumentFloatByName(name: String) = options[name]?.cast<Float>()

    fun getArgumentUserByName(name: String) = getArgumentLongByName(name)?.run { api.getCachedUserById(this).getOrNull() }
    suspend fun requestArgumentUserByName(name: String) = getArgumentLongByName(name)?.run {
        api.getCachedUserById(this).getOrNull() ?: api.getUserById(this).await()
    }

    fun getArgumentRoleByName(name: String) = getArgumentLongByName(name)?.run { api.getRoleById(this).getOrNull() }
    fun getArgumentChannelByName(name: String) = getArgumentLongByName(name)?.run { api.getChannelById(this).getOrNull() }
    fun getArgumentVoiceChannelByName(name: String) = getArgumentLongByName(name)?.run { api.getVoiceChannelById(this).getOrNull() }
    fun getArgumentTextChannelByName(name: String) = getArgumentLongByName(name)?.run { api.getTextChannelById(this).getOrNull() }
    fun getArgumentCustomEmojiByName(name: String) = getArgumentLongByName(name)?.run { api.getCustomEmojiById(this).getOrNull() }

    fun getArgumentMessageByName(name: String) = options[name]?.cast<MessageParameters>()
    suspend fun requestArgumentMessageByName(name: String) = getArgumentMessageByName(name)?.run {
        val channel = api.getTextChannelById(channel).getOrNull() ?: return null
        api.getMessageById(id, channel).await()
    }
}

