package pw.mihou.traveler.features.commands.options

import kotlinx.coroutines.future.await
import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.Channel
import org.javacord.api.entity.emoji.KnownCustomEmoji
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.util.DiscordRegexPattern
import java.lang.IllegalArgumentException
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.jvm.optionals.getOrNull

data class MessageCommandOption(private val api: DiscordApi, val textRepresentation: String) {
    val int: Int? get() = textRepresentation.toIntOrNull()

    val long: Long? get() = textRepresentation.toLongOrNull()
    val double: Double? get() = textRepresentation.toDoubleOrNull()
    val boolean: Boolean? get() = textRepresentation.toBooleanStrictOrNull()
    val float: Float? get() = textRepresentation.toFloatOrNull()

    private fun id(pattern: Pattern): Long? {
        val match = pattern.matcher(textRepresentation)
        if (!match.matches()) {
            return null
        }
        return match.group("id").toLong()
    }

    val userId: Long? get() = id(DiscordRegexPattern.USER_MENTION)
    val cachedUser: User? get() = userId?.run { api.getCachedUserById(this).getOrNull() }

    suspend fun requestUser(): User? {
        return cachedUser ?: userId?.run { api.getUserById(this).await() }
    }

    val roleId: Long? get() = id(DiscordRegexPattern.ROLE_MENTION)
    val role: Role? get() = roleId?.run { api.getRoleById(this).getOrNull() }

    val channelId: Long? get() = id(DiscordRegexPattern.CHANNEL_MENTION)
    val channel: Channel? get() = channelId?.run { api.getChannelById(this).getOrNull() }

    val slashCommandId: Long? get() = id(DiscordRegexPattern.SLASH_COMMAND_MENTION)

    val customEmojiId: Long? get() = id(DiscordRegexPattern.CUSTOM_EMOJI)
    val customEmoji: KnownCustomEmoji? get() = customEmojiId?.run { api.getCustomEmojiById(this).getOrNull() }

    val messageLink: MessageParameters? get() {
        val match = DiscordRegexPattern.MESSAGE_LINK.matcher(textRepresentation)
        if (!match.matches()) {
            return null
        }
        val server = match.groupOrNull("server")?.toLongOrNull()
        val channel = match.group("channel").toLong()
        val id = match.group("message").toLong()
        return MessageParameters(channel, server, id)
    }
    val cachedMessage: Message? get() = messageLink?.run { api.getCachedMessageById(id).getOrNull() }

    suspend fun requestMessage(): Message? {
        return cachedMessage ?: messageLink?.run {
            val channel = api.getTextChannelById(channel).getOrNull() ?: return null
            return api.getMessageById(id, channel).await()
        }
    }

    val snowflake: Long? get() = id(DiscordRegexPattern.SNOWFLAKE)

    override fun toString(): String {
        return textRepresentation
    }
}

private fun Matcher.groupOrNull(name: String): String? {
    return try {
        group(name)
    } catch (ex: IllegalArgumentException) {
        null
    }
}

data class MessageParameters(val channel: Long, val server: Long?, val id: Long)