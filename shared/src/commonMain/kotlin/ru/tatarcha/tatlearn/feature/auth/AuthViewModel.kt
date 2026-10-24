package ru.tatarcha.tatlearn.feature.auth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.common.errorCode
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.domain.repository.AuthRepository

class AuthViewModel(
    initialMode: AuthMode,
    private val auth: AuthRepository,
) : MviViewModel<AuthUiState, AuthUiEvent, AuthUiEffect>(AuthUiState(mode = initialMode)) {

    private var toastJob: Job? = null

    override fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.ModeChanged -> setState { copy(mode = event.mode, emailError = null, passwordError = null) }
            is AuthUiEvent.EmailChanged -> setState { copy(email = event.value, emailError = null) }
            is AuthUiEvent.PasswordChanged -> setState { copy(password = event.value, passwordError = null) }
            AuthUiEvent.TogglePasswordVisibility -> setState { copy(passwordVisible = !passwordVisible) }
            AuthUiEvent.SubmitClicked -> submit()
            // TODO(backend): настоящий OAuth. Пока кнопки соцсетей только сообщают, что вход недоступен.
            is AuthUiEvent.SocialClicked -> showToast(AuthToast.SocialUnavailable)
            AuthUiEvent.ToastDismissed -> hideToast()
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
        authenticate(current.email.trim(), current.password, current.mode)
    }

    private fun authenticate(email: String, password: String, mode: AuthMode) {
        viewModelScope.launch {
            hideToast()
            setState { copy(isSubmitting = true) }
            val result = if (mode == AuthMode.SignIn) auth.signIn(email, password) else auth.signUp(email, password)
            result.fold(
                onSuccess = {
                    setState { copy(isSubmitting = false) }
                    sendEffect(AuthUiEffect.NavigateToHome)
                },
                onFailure = { error ->
                    setState { copy(isSubmitting = false) }
                    showToast(AuthToast.RequestFailed(mode, error.errorCode()))
                },
            )
        }
    }

    private fun showToast(toast: AuthToast) {
        toastJob?.cancel()
        setState { copy(toast = toast) }
        toastJob = viewModelScope.launch {
            delay(TamyrMotion.TOAST_VISIBLE_MS)
            setState { copy(toast = null) }
        }
    }

    private fun hideToast() {
        toastJob?.cancel()
        setState { copy(toast = null) }
    }
}
