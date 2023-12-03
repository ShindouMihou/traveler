package pw.mihou.traveler.features.commands.interceptors

import pw.mihou.traveler.features.commands.MessageCommandEvent

fun interface Afterware {
    suspend fun on(event: MessageCommandEvent)
}