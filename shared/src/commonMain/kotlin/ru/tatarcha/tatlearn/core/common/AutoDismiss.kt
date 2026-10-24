package ru.tatarcha.tatlearn.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Однократный таймер: новый запуск отменяет предыдущий. Нужен, чтобы уведомление пропадало само. */
class AutoDismiss(
    private val scope: CoroutineScope,
    private val delayMillis: Long,
) {
    private var job: Job? = null

    fun start(onElapsed: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(delayMillis)
            onElapsed()
        }
    }

    fun cancel() {
        job?.cancel()
    }
}
