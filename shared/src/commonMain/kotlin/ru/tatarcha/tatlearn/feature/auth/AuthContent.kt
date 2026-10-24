package ru.tatarcha.tatlearn.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.common.ErrorCodes
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrSegmentedTabs
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTextField
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastData
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastHost
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastKind
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.auth_email_label
import tatlearn.shared.generated.resources.auth_error_email_empty
import tatlearn.shared.generated.resources.auth_error_email_invalid
import tatlearn.shared.generated.resources.auth_error_password_empty
import tatlearn.shared.generated.resources.auth_error_password_short
import tatlearn.shared.generated.resources.auth_error_sign_in_code
import tatlearn.shared.generated.resources.auth_error_sign_in_network
import tatlearn.shared.generated.resources.auth_error_sign_up_code
import tatlearn.shared.generated.resources.auth_error_sign_up_network
import tatlearn.shared.generated.resources.auth_forgot_password
import tatlearn.shared.generated.resources.auth_forgot_password_unavailable
import tatlearn.shared.generated.resources.auth_google
import tatlearn.shared.generated.resources.auth_hide_password
import tatlearn.shared.generated.resources.auth_legal
import tatlearn.shared.generated.resources.auth_or
import tatlearn.shared.generated.resources.auth_password_label
import tatlearn.shared.generated.resources.auth_show_password
import tatlearn.shared.generated.resources.auth_social_unavailable
import tatlearn.shared.generated.resources.auth_submit_sign_in
import tatlearn.shared.generated.resources.auth_submit_sign_up
import tatlearn.shared.generated.resources.auth_tab_sign_in
import tatlearn.shared.generated.resources.auth_tab_sign_up
import tatlearn.shared.generated.resources.auth_title_sign_in
import tatlearn.shared.generated.resources.auth_title_sign_up
import tatlearn.shared.generated.resources.auth_vk

private const val PASSWORD_MASK = '•'

@Composable
fun AuthContent(
    state: AuthUiState,
    onEvent: (AuthUiEvent) -> Unit,
) {
    val signIn = state.mode == AuthMode.SignIn
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TamyrTheme.colors.background)
                .safeDrawingPadding()
                .imePadding(),
        ) {
            Text(
                text = stringResource(if (signIn) Res.string.auth_title_sign_in else Res.string.auth_title_sign_up),
                style = TamyrTheme.typography.title22,
                color = TamyrTheme.colors.textPrimary,
                modifier = Modifier.padding(
                    start = TamyrTheme.spacing.screen,
                    end = TamyrTheme.spacing.screen,
                    top = TamyrTheme.spacing.section,
                ),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = TamyrTheme.spacing.screen,
                        vertical = TamyrTheme.spacing.sectionInset,
                    ),
                verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.section),
            ) {
                TamyrSegmentedTabs(
                    options = listOf(
                        stringResource(Res.string.auth_tab_sign_in),
                        stringResource(Res.string.auth_tab_sign_up),
                    ),
                    selectedIndex = if (signIn) 0 else 1,
                    onSelect = { index ->
                        onEvent(AuthUiEvent.ModeChanged(if (index == 0) AuthMode.SignIn else AuthMode.SignUp))
                    },
                )
                TamyrTextField(
                    value = state.email,
                    onValueChange = { onEvent(AuthUiEvent.EmailChanged(it)) },
                    label = stringResource(Res.string.auth_email_label),
                    errorText = state.emailError?.let { error ->
                        stringResource(
                            if (error == EmailError.Empty) Res.string.auth_error_email_empty else Res.string.auth_error_email_invalid,
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )
                TamyrTextField(
                    value = state.password,
                    onValueChange = { onEvent(AuthUiEvent.PasswordChanged(it)) },
                    label = stringResource(Res.string.auth_password_label),
                    errorText = state.passwordError?.let { error ->
                        stringResource(
                            if (error == PasswordError.Empty) Res.string.auth_error_password_empty else Res.string.auth_error_password_short,
                        )
                    },
                    visualTransformation = if (state.passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation(PASSWORD_MASK)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onEvent(AuthUiEvent.SubmitClicked)
                        },
                    ),
                    trailing = {
                        Text(
                            text = stringResource(
                                if (state.passwordVisible) Res.string.auth_hide_password else Res.string.auth_show_password,
                            ),
                            style = TamyrTheme.typography.caption13Bold,
                            color = TamyrTheme.colors.accent,
                            modifier = Modifier.clickable(role = Role.Button) {
                                onEvent(AuthUiEvent.TogglePasswordVisibility)
                            },
                        )
                    },
                )
                if (signIn) {
                    // TODO(design): у «Забыли пароль» пока нет целевого экрана.
                    Text(
                        text = stringResource(Res.string.auth_forgot_password),
                        style = TamyrTheme.typography.body14Bold,
                        color = TamyrTheme.colors.accent,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) { onEvent(AuthUiEvent.ForgotPasswordClicked) },
                    )
                }
                TamyrButton(
                    text = stringResource(
                        if (signIn) Res.string.auth_submit_sign_in else Res.string.auth_submit_sign_up,
                    ),
                    onClick = { onEvent(AuthUiEvent.SubmitClicked) },
                    loading = state.isSubmitting,
                )
                OrDivider()
                SocialButtons(onEvent = onEvent)
            }
            Text(
                text = stringResource(Res.string.auth_legal),
                style = TamyrTheme.typography.caption13.copy(
                    lineHeight = TamyrTheme.typography.caption13.fontSize * LEGAL_LINE_HEIGHT_FACTOR,
                ),
                color = TamyrTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TamyrTheme.spacing.xxl,
                        end = TamyrTheme.spacing.xxl,
                        bottom = TamyrTheme.spacing.xxl,
                    ),
            )
        }
        TamyrToastHost(
            toast = state.toast?.let { toast -> authToastData(toast) },
            onDismiss = { onEvent(AuthUiEvent.ToastDismissed) },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

private const val LEGAL_LINE_HEIGHT_FACTOR = 1.5f

@Composable
private fun OrDivider() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(TamyrSizes.borderThin)
                .background(TamyrTheme.colors.border),
        )
        Text(
            text = stringResource(Res.string.auth_or),
            style = TamyrTheme.typography.caption13,
            color = TamyrTheme.colors.textSecondary,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(TamyrSizes.borderThin)
                .background(TamyrTheme.colors.border),
        )
    }
}

@Composable
private fun SocialButtons(onEvent: (AuthUiEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.tight)) {
        SocialButton(
            text = stringResource(Res.string.auth_google),
            markColor = TamyrTheme.colors.gold,
            markShape = TamyrTheme.shapes.circle,
            onClick = { onEvent(AuthUiEvent.SocialClicked(SocialProvider.Google)) },
        )
        SocialButton(
            text = stringResource(Res.string.auth_vk),
            markColor = TamyrTheme.colors.primary,
            markShape = TamyrTheme.shapes.mark,
            onClick = { onEvent(AuthUiEvent.SocialClicked(SocialProvider.Vk)) },
        )
    }
}

/** TODO(design): логотипы провайдеров в макете тоже просто цветные метки. */
@Composable
private fun SocialButton(
    text: String,
    markColor: Color,
    markShape: Shape,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TamyrSizes.buttonHeightSecondary)
            .clip(TamyrTheme.shapes.answer)
            .background(TamyrTheme.colors.surface)
            .border(TamyrSizes.borderThin, TamyrTheme.colors.border, TamyrTheme.shapes.answer)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.tight, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(TamyrSizes.providerMark)
                .clip(markShape)
                .background(markColor),
        )
        Text(
            text = text,
            style = TamyrTheme.typography.body15Bold,
            color = TamyrTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun authToastData(toast: AuthToast): TamyrToastData = when (toast) {
    AuthToast.SocialUnavailable -> TamyrToastData(
        text = stringResource(Res.string.auth_social_unavailable),
        kind = TamyrToastKind.Info,
    )
    AuthToast.ForgotPasswordUnavailable -> TamyrToastData(
        text = stringResource(Res.string.auth_forgot_password_unavailable),
        kind = TamyrToastKind.Info,
    )
    is AuthToast.RequestFailed -> {
        val network = toast.code == ErrorCodes.NET_TIMEOUT || toast.code == ErrorCodes.NET_ERROR
        val signIn = toast.mode == AuthMode.SignIn
        TamyrToastData(
            text = when {
                network && signIn -> stringResource(Res.string.auth_error_sign_in_network)
                network -> stringResource(Res.string.auth_error_sign_up_network)
                signIn -> stringResource(Res.string.auth_error_sign_in_code, toast.code)
                else -> stringResource(Res.string.auth_error_sign_up_code, toast.code)
            },
            kind = TamyrToastKind.Error,
        )
    }
}

private const val PREVIEW_GROUP = "Auth"

@Composable
private fun AuthPreview(state: AuthUiState) = PreviewFrame {
    AuthContent(
        state = state,
        onEvent = {},
    )
}

@Preview(name = "Sign in · empty", group = PREVIEW_GROUP)
@Composable
private fun AuthSignInPreview() = AuthPreview(AuthUiState())

@Preview(name = "Sign in · filled, password shown", group = PREVIEW_GROUP)
@Composable
private fun AuthSignInFilledPreview() = AuthPreview(
    AuthUiState(email = "aliya@example.com", password = "secret123", passwordVisible = true),
)

@Preview(name = "Sign in · field errors", group = PREVIEW_GROUP)
@Composable
private fun AuthFieldErrorsPreview() = AuthPreview(
    AuthUiState(email = "aliya@", emailError = EmailError.Invalid, passwordError = PasswordError.Empty),
)

@Preview(name = "Sign in · empty password", group = PREVIEW_GROUP)
@Composable
private fun AuthEmptyPasswordPreview() = AuthPreview(
    AuthUiState(email = "aliya@example.com", passwordError = PasswordError.Empty),
)

@Preview(name = "Sign in · empty email", group = PREVIEW_GROUP)
@Composable
private fun AuthEmptyEmailPreview() = AuthPreview(
    AuthUiState(password = "secret123", emailError = EmailError.Empty),
)

@Preview(name = "Sign in · submitting", group = PREVIEW_GROUP)
@Composable
private fun AuthSubmittingPreview() = AuthPreview(
    AuthUiState(email = "aliya@example.com", password = "secret123", isSubmitting = true),
)

@Preview(name = "Sign in · request error", group = PREVIEW_GROUP)
@Composable
private fun AuthRequestErrorPreview() = AuthPreview(
    AuthUiState(
        email = "aliya@example.com",
        password = "secret123",
        toast = AuthToast.RequestFailed(AuthMode.SignIn, ErrorCodes.NET_TIMEOUT),
    ),
)

@Preview(name = "Sign in · social unavailable toast", group = PREVIEW_GROUP)
@Composable
private fun AuthSocialToastPreview() = AuthPreview(AuthUiState(toast = AuthToast.SocialUnavailable))

@Preview(name = "Sign in · forgot password toast", group = PREVIEW_GROUP)
@Composable
private fun AuthForgotPasswordToastPreview() = AuthPreview(
    AuthUiState(toast = AuthToast.ForgotPasswordUnavailable),
)

@Preview(name = "Sign up · empty", group = PREVIEW_GROUP)
@Composable
private fun AuthSignUpPreview() = AuthPreview(AuthUiState(mode = AuthMode.SignUp))

@Preview(name = "Sign up · field errors", group = PREVIEW_GROUP)
@Composable
private fun AuthSignUpErrorsPreview() = AuthPreview(
    AuthUiState(
        mode = AuthMode.SignUp,
        email = "no-at-sign",
        password = "12",
        emailError = EmailError.Invalid,
        passwordError = PasswordError.TooShort,
    ),
)

@Preview(name = "Sign up · request error with code", group = PREVIEW_GROUP)
@Composable
private fun AuthSignUpRequestErrorPreview() = AuthPreview(
    AuthUiState(
        mode = AuthMode.SignUp,
        email = "aliya@example.com",
        password = "secret123",
        toast = AuthToast.RequestFailed(AuthMode.SignUp, ErrorCodes.UNKNOWN),
    ),
)
