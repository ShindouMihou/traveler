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

Remember that the use of this framework requires the `Message Content` intent, which can be added using the Javacord 
method: `.addIntent`:
```kotlin
val builder = DiscordApiBuilder()
    .setToken(...)
    .addListener(Traveler)
    .addIntent(Intent.MESSAGE_CONTENT)
```

If you want to add a command, simply use the `Traveler.add(commands)` method:
```kotlin
Traveler.add(PingCommand)
```

## Building Schema

A schema defines the different variations that the command accepts. It helps the framework understand your command, and 
map the options rightly to make things more convenient to the developer, making it feel similar to how you would grab a 
slash command in Javacord.

Generally, a schema compromises of only the options, this can be a subcommand, or an option itself. To exemplify this:
```text
subcommand [type:name]
```

A subcommand shouldn't be enclosed in a bracket, similarly, subcommands aren't retrievable, but it helps when having multiple schemas 
as you can simply check if the schema caught has this subcommand, for example:
```kotlin
val schema = event.schema ?: return
if (schema.matched.startsWith("subcommand")) {
    // do something
}
```

You can use such techniques to build a mini-router that will help you with large commands. As for schema options, an option must have a 
type and a name. An option's name cannot contain `:`, `[` and `]` which are treated special characters. Additionally, the schema decoder 
is incredibly strict and doesn't allow:
1. Double-spacing, you can't add two spaces between anything, for example: `hello  [string:world]`.
2. The use of the special type for identifiers, you can find this when looking into the `SchemaDecoder` code.

### Supported Schema Types
The schema mechanism supports any of the following types:
- `user` maps to a `User`
- `channel` maps to `Channel`
- `role` maps to a `Role`
- `message` maps to a `MessageParameters`. A `message` refers to a `message link` which contains the server, channel and the message identifier.
- `emoji` maps to a `KnownCustomEmoji`.
- `string` maps to any text. Currently, you need to wrap the text with quotations (`"`, `'`) to grab more than one text.
- `int` maps to an int32, otherwise known as `Int`.
- `long` maps to an int64, otherwise known as `Long`.
- `boolean` refers to a bool, otherwise known as a `Boolean`.
- `float` refers to a floating point, otherwise known as a `Float`
- `double` refers to a double.

### Understanding the Schema System

The schema system is incredibly simple. During `Traveler.add(commands)`, each command's schemas are decoded into these definitions 
that the framework can understand easier and faster, these definitions tells the framework what each option's position should be, what 
their type and name should be.

During a call to `event.schema`, the framework loads all the schemas of the command and goes to them one-by-one to see which schema 
matches the layout of the command. During this, it tries to parse the options based on what the schema's definition declared and if  
it fails then it will move to the next schema until a match hits or all attempts have been exhausted.

## License

Traveler is distributed under the Apache 2.0 license, the same one used by Javacord. See [LICENSE](LICENSE) for more information.