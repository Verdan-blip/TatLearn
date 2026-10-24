package ru.tatarcha.tatlearn.feature.auth

import kotlinx.serialization.Serializable
import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState

@Serializable
enum class AuthMode { SignIn, SignUp }

enum class EmailError { Empty, Invalid }
enum class PasswordError { TooShort }
enum class SocialProvider { Google, Vk }
enum class AuthToast { SocialUnavailable }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val isSubmitting: Boolean = false,
    /** Короткий код последнего неудачного запроса, показывается под кнопкой. */
    val requestErrorCode: String? = null,
    val toast: AuthToast? = null,
) : UiState

sealed interface AuthUiEvent : UiEvent {
    data class ModeChanged(val mode: AuthMode) : AuthUiEvent
    data class EmailChanged(val value: String) : AuthUiEvent
    data class PasswordChanged(val value: String) : AuthUiEvent
    data object TogglePasswordVisibility : AuthUiEvent
    data object SubmitClicked : AuthUiEvent
    data class SocialClicked(val provider: SocialProvider) : AuthUiEvent
    data object ToastDismissed : AuthUiEvent
    data object BackClicked : AuthUiEvent
}

sealed interface AuthUiEffect : UiEffect {
    data object NavigateToHome : AuthUiEffect
    data object NavigateBack : AuthUiEffect
}
