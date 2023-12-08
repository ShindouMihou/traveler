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
        if (event.messageAuthor.isBotUser) return
        if (event.messageContent.isEmpty()) return

        val options = mutableListOf<String>()
        var commandName: String? = null
        OptionSeparator.stream(event.messageContent.trim()) { arg: String ->
            if (options.isEmpty()) {
                // first arg, otherwise the analysis section.
                val (min, max) = Traveler.`commands$minsize` to Traveler.`commands$maxsize`
                if (arg.length !in min..max) {
                    return@stream false
                }

                commandName = arg
                return@stream true
            }

            options += arg
            true
        }
        if (commandName == null) return

        var name = commandName as String
        val server = event.server

        val prefix = Traveler.configuration.prefix.loader(server.map { it.id }.getOrNull())
        if (!name.startsWith(prefix)) {
            return
        }
        name = name.removePrefix(prefix)

        val command = Traveler.commands.firstOrNull { it.name == name } ?: return
        val slicedOptions =
            if (options.size == 1) mutableListOf()
            else options.subList(1, options.size)

        val commandEvent = MessageCommandEvent(slicedOptions.map { MessageCommandOption(event.api, it) }, event, command)

        val middlewares = Traveler.middlewares + command.middlewares
        if (middlewares.isNotEmpty()) {
            var stopped = false
            val endExecution: EndExecution = { stopped = true }

            for (middleware in middlewares) {
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

        val afterwares = Traveler.afterwares + command.afterwares
        for (afterware in afterwares) {
            afterware.on(commandEvent)
        }
    }
}