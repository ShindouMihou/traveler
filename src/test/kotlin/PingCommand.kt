import pw.mihou.traveler.features.commands.MessageCommand
import pw.mihou.traveler.features.commands.MessageCommandEvent
import pw.mihou.traveler.features.commands.router.withRouter

object PingCommand: MessageCommand {
    override val name: String = "ping"
    override val description: String = "pings a user with a message"
    override val schemas: List<String> = listOf(
        "user [user:user] [*message:string]",
        "channel [channel:channel]"
    )
    override suspend fun on(event: MessageCommandEvent) = event.withRouter {
        route("user") { ev, schema  ->
            if (schema == null) return@route
        }
    }
}