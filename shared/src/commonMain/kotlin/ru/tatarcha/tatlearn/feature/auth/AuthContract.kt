package ru.tatarcha.tatlearn.feature.auth

import kotlinx.serialization.Serializable
import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState

@Serializable
enum class AuthMode { SignIn, SignUp }

enum class EmailError { Empty, Invalid }
enum class PasswordError { Empty, TooShort }
enum class SocialProvider { Google, Vk }
sealed interface AuthToast {
    data object SocialUnavailable : AuthToast
    data object ForgotPasswordUnavailable : AuthToast
    data class RequestFailed(val mode: AuthMode, val code: String) : AuthToast
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val isSubmitting: Boolean = false,
    val toast: AuthToast? = null,
) : UiState

sealed interface AuthUiEvent : UiEvent {
    data class ModeChanged(val mode: AuthMode) : AuthUiEvent
    data class EmailChanged(val value: String) : AuthUiEvent
    data class PasswordChanged(val value: String) : AuthUiEvent
    data object TogglePasswordVisibility : AuthUiEvent
    data object ForgotPasswordClicked : AuthUiEvent
    data object SubmitClicked : AuthUiEvent
    data class SocialClicked(val provider: SocialProvider) : AuthUiEvent
    data object ToastDismissed : AuthUiEvent
}

sealed interface AuthUiEffect : UiEffect {
    data object NavigateToHome : AuthUiEffect
}
