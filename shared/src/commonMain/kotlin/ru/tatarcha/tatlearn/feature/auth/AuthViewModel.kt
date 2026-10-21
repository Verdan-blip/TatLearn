package ru.tatarcha.tatlearn.feature.auth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.common.errorCode
import ru.tatarcha.tatlearn.domain.repository.AuthRepository

class AuthViewModel(
    initialMode: AuthMode,
    private val auth: AuthRepository,
) : MviViewModel<AuthUiState, AuthUiEvent, AuthUiEffect>(AuthUiState(mode = initialMode)) {

    override fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.ModeChanged -> setState { copy(mode = event.mode, emailError = null, passwordError = null, requestErrorCode = null) }
            is AuthUiEvent.EmailChanged -> setState { copy(email = event.value, emailError = null, requestErrorCode = null) }
            is AuthUiEvent.PasswordChanged -> setState { copy(password = event.value, passwordError = null, requestErrorCode = null) }
            AuthUiEvent.TogglePasswordVisibility -> setState { copy(passwordVisible = !passwordVisible) }
            AuthUiEvent.SubmitClicked -> submit()
            // TODO(backend): настоящий OAuth. Пока кнопки соцсетей входят под фейковым аккаунтом.
            is AuthUiEvent.SocialClicked -> signIn(SOCIAL_EMAIL[event.provider].orEmpty(), SOCIAL_PASSWORD)
            AuthUiEvent.BackClicked -> sendEffect(AuthUiEffect.NavigateBack)
        }
    }

    private fun submit() {
        val current = state.value
        if (current.isSubmitting) return
        val emailError = AuthValidator.email(current.email)
        val passwordError = AuthValidator.password(current.password)
        if (emailError != null || passwordError != null) {
            setState { copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        signIn(current.email.trim(), current.password, current.mode)
    }

    private fun signIn(email: String, password: String, mode: AuthMode = AuthMode.SignIn) {
        viewModelScope.launch {
            setState { copy(isSubmitting = true, requestErrorCode = null) }
            val result = if (mode == AuthMode.SignIn) auth.signIn(email, password) else auth.signUp(email, password)
            result.fold(
                onSuccess = {
                    setState { copy(isSubmitting = false) }
                    sendEffect(AuthUiEffect.NavigateToHome)
                },
                onFailure = { error -> setState { copy(isSubmitting = false, requestErrorCode = error.errorCode()) } },
            )
        }
    }

    private companion object {
        val SOCIAL_EMAIL = mapOf(
            SocialProvider.Google to "aliya@gmail.example",
            SocialProvider.Vk to "aliya@vk.example",
        )
        const val SOCIAL_PASSWORD = "social-login"
    }
}
