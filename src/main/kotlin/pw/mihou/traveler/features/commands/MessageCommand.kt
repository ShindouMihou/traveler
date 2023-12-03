package pw.mihou.traveler.features.commands

import pw.mihou.traveler.features.commands.interceptors.Afterware
import pw.mihou.traveler.features.commands.interceptors.Middleware

interface MessageCommand {
    val name: String
    val description: String

    val middlewares: MutableList<Middleware> get() = mutableListOf()
    val afterwares: MutableList<Afterware> get() = mutableListOf()

    val schemas: List<String>? get() = null
    val store: Map<String, Any> get() = emptyMap()

    suspend fun on(event: MessageCommandEvent)
}