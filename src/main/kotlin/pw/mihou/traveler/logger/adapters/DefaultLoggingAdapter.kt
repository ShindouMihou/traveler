package pw.mihou.traveler.logger.adapters

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pw.mihou.traveler.Traveler
import pw.mihou.traveler.logger.LoggingAdapter


class DefaultLoggingAdapter : LoggingAdapter {
    private val logger: Logger = LoggerFactory.getLogger(Traveler::class.java)
    override fun info(message: String, vararg values: Any) {
        logger.info(message, values)
    }

    override fun error(message: String, vararg values: Any) {
        logger.error(message, values)
    }

    override fun warn(message: String, vararg values: Any) {
        logger.warn(message, values)
    }

    override fun debug(message: String, vararg values: Any) {
        logger.debug(message, values)
    }
}

