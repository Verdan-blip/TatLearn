package ru.tatarcha.tatlearn.core.common

/** Общая модель загрузки для экранов на репозиториях. Повтор — это UiEvent, который обрабатывает ViewModel. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Content<T>(val data: T) : LoadState<T>

    /** [code] — короткий технический код, показываемый пользователю, например NET_TIMEOUT. */
    data class Error(val code: String? = null) : LoadState<Nothing>
}
