package ru.tatarcha.tatlearn.core.common

import kotlin.coroutines.cancellation.CancellationException

/** Ошибка с коротким машинным кодом, который можно показать пользователю (например, NET_TIMEOUT). */
class AppException(val code: String, cause: Throwable? = null) : Exception(code, cause)

object ErrorCodes {
    const val NET_TIMEOUT = "NET_TIMEOUT"
    const val NET_ERROR = "NET_ERROR"
    const val NOT_FOUND = "NOT_FOUND"
    const val UNKNOWN = "UNKNOWN"
}

/** Как runCatching, но не глотает отмену корутины. */
inline fun <T> runCatchingApp(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

fun Throwable.errorCode(): String = (this as? AppException)?.code ?: ErrorCodes.UNKNOWN
