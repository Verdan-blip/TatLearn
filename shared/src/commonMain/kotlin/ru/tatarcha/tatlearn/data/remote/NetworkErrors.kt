package ru.tatarcha.tatlearn.data.remote

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import ru.tatarcha.tatlearn.core.common.AppException
import ru.tatarcha.tatlearn.core.common.ErrorCodes
import ru.tatarcha.tatlearn.core.common.runCatchingApp

/** Превращает сбои транспорта в коды [AppException], которые может показать UI. */
suspend fun <T> remoteCall(block: suspend () -> T): Result<T> {
    val result = runCatchingApp { block() }
    return result.fold(
        onSuccess = { Result.success(it) },
        onFailure = { error ->
            val code = when (error) {
                is AppException -> return@fold Result.failure(error)
                is HttpRequestTimeoutException -> ErrorCodes.NET_TIMEOUT
                is ResponseException -> "HTTP_${error.response.status.value}"
                else -> ErrorCodes.NET_ERROR
            }
            Result.failure(AppException(code, error))
        },
    )
}
