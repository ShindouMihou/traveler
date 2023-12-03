package pw.mihou.traveler.features.commands.interceptors

import pw.mihou.traveler.features.commands.MessageCommandEvent

typealias EndExecution = () -> Unit
fun interface Middleware {
    suspend fun on(event: MessageCommandEvent, stop: EndExecution)
}