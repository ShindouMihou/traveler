package pw.mihou.traveler

import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.helpers.NOPLogger
import pw.mihou.traveler.configuration.TravelerConfiguration
import pw.mihou.traveler.coroutines.coroutine
import pw.mihou.traveler.features.commands.MessageCommand
import pw.mihou.traveler.features.commands.dispatcher.TravelerMessageDispatcher
import pw.mihou.traveler.features.commands.interceptors.Afterware
import pw.mihou.traveler.features.commands.interceptors.Middleware
import pw.mihou.traveler.features.commands.schema.SchemaDecoder
import pw.mihou.traveler.logger.LoggingAdapter
import pw.mihou.traveler.logger.adapters.DefaultLoggingAdapter
import pw.mihou.traveler.logger.adapters.FastLoggingAdapter
import java.lang.IllegalArgumentException

object Traveler: MessageCreateListener {
    private val dispatcher = TravelerMessageDispatcher()
    internal val commands = mutableListOf<MessageCommand>()

    val configuration = TravelerConfiguration()

    //@desc global copy to middlewares
    var middlewares = mutableListOf<Middleware>()
    var afterwares = mutableListOf<Afterware>()

    @Volatile var logger: LoggingAdapter =
        if (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) == null || LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) is NOPLogger) FastLoggingAdapter
        else DefaultLoggingAdapter()

    fun remove(vararg commands: MessageCommand) {
        for (command in commands) {
            this.commands.remove(command)
        }
    }

    fun add(vararg commands: MessageCommand) {
        for (command in commands) {
            val ok = SchemaDecoder.load(command)
            if (!ok) {
                throw IllegalArgumentException("${command.name} has an invalid schema, and cannot be added as a command.")
            }

            this.commands.add(command)
        }
    }

    override fun onMessageCreate(event: MessageCreateEvent) {
        coroutine { dispatcher.dispatch(event) }
    }
}