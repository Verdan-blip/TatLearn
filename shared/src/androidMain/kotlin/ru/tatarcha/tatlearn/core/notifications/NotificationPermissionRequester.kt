package ru.tatarcha.tatlearn.core.notifications

import kotlinx.coroutines.CompletableDeferred

/**
 * Мост между корутинами и диалогом разрешения: `MainActivity` регистрирует запуск диалога, а [request] ждёт ответа.
 * Глобальный объект, потому что запросить разрешение может только живая activity.
 */
object NotificationPermissionRequester {
    private var launcher: (() -> Unit)? = null
    private var pending: CompletableDeferred<Boolean>? = null

    fun attach(launch: () -> Unit) {
        launcher = launch
    }

    fun detach() {
        launcher = null
        pending?.complete(false)
        pending = null
    }

    fun onResult(granted: Boolean) {
        pending?.complete(granted)
        pending = null
    }

    suspend fun request(): Boolean {
        val launch = launcher ?: return false
        pending?.complete(false)
        val answer = CompletableDeferred<Boolean>()
        pending = answer
        launch()
        return answer.await()
    }
}
