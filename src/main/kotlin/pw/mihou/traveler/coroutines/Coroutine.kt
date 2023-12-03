package pw.mihou.traveler.coroutines

import kotlinx.coroutines.*
import pw.mihou.traveler.Traveler

private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
fun coroutine(task: suspend () -> Unit) =
    scope.launch {
        try {
            task()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Traveler.logger.error("An uncaught exception was captured by the coroutine launcher.", exception)
        }
    }