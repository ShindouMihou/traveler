package pw.mihou.traveler.features.commands.dispatcher

import org.javacord.api.entity.intent.Intent
import org.javacord.api.event.message.MessageCreateEvent
import pw.mihou.traveler.Traveler
import pw.mihou.traveler.features.commands.MessageCommandEvent
import pw.mihou.traveler.features.commands.interceptors.EndExecution
import pw.mihou.traveler.features.commands.options.MessageCommandOption
import pw.mihou.traveler.features.commands.separator.OptionSeparator
import kotlin.jvm.optionals.getOrNull

class TravelerMessageDispatcher internal constructor() {
    suspend fun dispatch(event: MessageCreateEvent) {
        if (!event.api.intents.contains(Intent.MESSAGE_CONTENT)) {
            Traveler.logger.warn("Message content intent is not enabled. Please enable it for Traveler to work.")
            return
        }
        if (event.messageContent.isEmpty()) return

        val options = OptionSeparator.separate(event.messageContent)
        if (options.isEmpty()) {
            // How would this happen???
            throw IllegalStateException("No content found during separation, this is likely a bug.")
        }

        val server = event.server

        val prefix = Traveler.configuration.prefix.loader(server.map { it.id }.getOrNull())

        var name = options[0]
        if (!name.startsWith(prefix)) {
            return
        }
        name = name.removePrefix(prefix)

        val command = Traveler.commands.firstOrNull { it.name == name } ?: return
        val slicedOptions =
            if (options.size == 1) mutableListOf()
            else options.subList(1, options.size)

        val commandEvent = MessageCommandEvent(slicedOptions.map { MessageCommandOption(event.api, it) }, event, command)

        if (command.middlewares.isNotEmpty()) {
            var stopped = false
            val endExecution: EndExecution = { stopped = true }

            for (middleware in command.middlewares) {
                middleware.on(commandEvent, endExecution)
                if (stopped) {
                    break
                }
            }

            if (stopped) {
                return
            }
        }
        command.on(commandEvent)
        for (afterware in command.afterwares) {
            afterware.on(commandEvent)
        }
    }
}