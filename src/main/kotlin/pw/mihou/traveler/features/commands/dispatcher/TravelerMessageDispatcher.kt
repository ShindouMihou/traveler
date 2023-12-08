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

        val server = event.server
        if (Traveler.configuration.dispatcher.ignoreDms && server.isEmpty) return
        if (Traveler.configuration.dispatcher.ignoreServers && server.isPresent) return

        if (server.isPresent && Traveler.configuration.dispatcher.ignoredServers.contains(server.get().id)) return
        if (Traveler.configuration.dispatcher.ignoredUsers.contains(event.messageAuthor.id)) return

        val prefix = Traveler.configuration.prefix.loader(server.map { it.id }.getOrNull())

        val options = mutableListOf<String>()
        var commandName: String? = null
        OptionSeparator.stream(event.messageContent.trim()) { arg: String ->
            if (commandName == null) {
                // first arg, otherwise the analysis section.
                val (min, max) = (Traveler.`commands$minsize` + prefix.length) to (Traveler.`commands$maxsize` + prefix.length)
                if (arg.length !in min..max) {
                    return@stream false
                }

                if (!arg.startsWith(prefix)) {
                    return@stream false
                }

                commandName = arg.removePrefix(prefix)
                return@stream true
            }

            options += arg
            true
        }
        if (commandName == null) return
        val name = commandName as String

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