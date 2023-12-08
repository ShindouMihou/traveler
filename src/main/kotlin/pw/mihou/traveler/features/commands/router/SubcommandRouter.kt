package pw.mihou.traveler.features.commands.router

import pw.mihou.traveler.features.commands.MessageCommand
import pw.mihou.traveler.features.commands.MessageCommandEvent
import pw.mihou.traveler.features.commands.MessageCommandEventSchema

suspend fun MessageCommandEvent.withRouter(builder: SubcommandRouter.Builder.() -> Unit) {
    val router = SubcommandRouter.cache.computeIfAbsent(command) {
        SubcommandRouter.create(builder)
    }
    router.dispatch(this)
}

class SubcommandRouter internal constructor() {
    internal val routes = mutableMapOf<String, AsyncRoute>()
    inner class Builder {
        fun route(name: String, route: AsyncRoute) {
            routes[name] = route
        }
    }

    companion object {
        internal val cache = mutableMapOf<MessageCommand, SubcommandRouter>()
        fun create(builder: Builder.() -> Unit): SubcommandRouter {
            val router = SubcommandRouter()
            builder(router.Builder())
            return router
        }
    }

    suspend fun dispatch(ev: MessageCommandEvent) {
        if (ev.options.isEmpty()) return
        if (routes.isEmpty()) return

        val schema = if (ev.command.schemas.isNullOrEmpty()) null else ev.schema
        for ((route, handler) in routes) {
            val tokens = route.split(" ")
            if (tokens.size == 1) {
                val token = tokens[0]
                if (token.equals(ev.options[0].textRepresentation, ignoreCase = true)) {
                    handler.on(ev, schema)
                    break
                }
                continue
            }

            var match = true
            var index = 0

            for (token in tokens) {
                if (token.equals(ev.options[index].textRepresentation, ignoreCase = true)) {
                    index++
                    continue
                }

                match = false
                break
            }

            if (match) {
                handler.on(ev, schema)
                break
            }
        }
    }
}

fun interface AsyncRoute {
    suspend fun on(ev: MessageCommandEvent, schema: MessageCommandEventSchema?)
}