package ru.tatarcha.tatlearn.feature.auth

sealed interface AuthToast {
    data object SocialUnavailable : AuthToast
    data object ForgotPasswordUnavailable : AuthToast
    data class RequestFailed(val mode: AuthMode, val code: String) : AuthToast
}
