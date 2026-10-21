package ru.tatarcha.tatlearn.data.fake

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.tatarcha.tatlearn.core.common.AppException
import ru.tatarcha.tatlearn.core.common.ErrorCodes
import kotlin.random.Random

/** Управляет фейковым бэкендом: задержка 300-800 мс и необязательный переключатель «всегда падать» для экранов 07/08. */
class FakeBackendConfig {
    private val _simulateErrors = MutableStateFlow(false)
    val simulateErrors: StateFlow<Boolean> = _simulateErrors.asStateFlow()

    fun setSimulateErrors(enabled: Boolean) {
        _simulateErrors.value = enabled
    }

    suspend fun simulateNetwork() {
        delay(Random.nextLong(MIN_DELAY_MS, MAX_DELAY_MS + 1))
        if (_simulateErrors.value) throw AppException(ErrorCodes.NET_TIMEOUT)
    }

    private companion object {
        const val MIN_DELAY_MS = 300L
        const val MAX_DELAY_MS = 800L
    }
}
