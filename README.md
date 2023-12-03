# Traveler

Traveler is a Kotlin-based coroutine-powered message command framework for Javacord Discord bots. It is the sister framework to 
[Nexus](https://github.com/ShindouMihou/Nexus) and shares a similar feel to Nexus had it been written as Kotlin 
framework.

## Overview

Unlike, other message command frameworks, Traveler uses a schema-iteration mechanism to mark and list options, allowing 
the framework to decode them appropriately. To understand this, here's a simple `ping` command.
```kotlin
object PingCommand: MessageCommand {
    override val name: String = "ping"
    override val description: String = "pings a user with a message"
    override val schemas: List<String> = listOf(
        "[user:user] [string:message]",
        "[user:user]"
    )
    override suspend fun on(event: MessageCommandEvent) {
        val schema = event.schema ?: return
        val user = schema.requestArgumentUserByName("user") ?: return
        val message = schema.getArgumentStringByName("message") ?: "someone wanted to tell you something."

        event.message.reply("${user.mentionTag}, $message")
    }
}
```

In addition, Traveler supports basic Nexus features such as middlewares, afterwares that are added in pretty much the 
same manner in Nexus.

## Installation

To install Traveler, you may use [Jitpack](https://jitpack.io/#pw.mihou/Traveler) and add Traveler as a listener in your 
Discord API builder:
```kotlin
val builder = DiscordApiBuilder()
    .setToken(...)
    .addListener(Traveler)
```

If you want to add a command, simply use the `Traveler.add(commands)` method:
```kotlin
Traveler.add(PingCommand)
```

## License

Traveler is distributed under the Apache 2.0 license, the same one used by Javacord. See [LICENSE](LICENSE) for more information.