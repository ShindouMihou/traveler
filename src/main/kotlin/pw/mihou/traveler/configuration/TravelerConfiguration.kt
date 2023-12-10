package pw.mihou.traveler.configuration

class TravelerConfiguration internal constructor() {
    val prefix = TravelerPrefixConfiguration()
    val dispatcher = TravelerDispatchConfiguration()
}

typealias PrefixLoader = suspend (server: Long?) -> String
class TravelerPrefixConfiguration internal constructor() {
    var loader: PrefixLoader = { "%" }
}

class TravelerDispatchConfiguration internal constructor() {
    /**
     * Servers enlisted in this will no longer have any commands dispatched to them. This is used
     * to disable message commands entirely for that given server at a dispatcher level, which means
     * compute and resource consumption is significantly decreased compared to a middleware level block.
     */
    @Volatile var ignoredServers: MutableSet<Long> = mutableSetOf()

    /**
     * Users enlisted in this will no longer have any commands dispatched to them. This is used to disable
     * message commands entirely for that user, whether in private messages or server messages at a dispatcher level,
     * which means compute and resource consumption is significantly decreased compared to a middleware level block.
     */
    @Volatile var ignoredUsers: MutableSet<Long> = mutableSetOf()

    /**
     * Enabling or disabling this option determines whether the framework should work with
     * command executions in private messages in a global scale, meaning, all commands will no longer
     * listen to private messages when this is enabled.
     */
    @Volatile var ignoreDms = false

    /**
     * Enabling or disabling this option determines whether the framework should work with
     * command executions in server messages in a global scale, meaning, all commands will no longer
     * listen to server messages when this is enabled.
     */
    @Volatile var ignoreServers = false
}