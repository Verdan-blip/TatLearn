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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButtonStyle
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrSegmentedTabs
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTextField
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.auth_back
import tatlearn.shared.generated.resources.auth_email_label
import tatlearn.shared.generated.resources.auth_error_email_empty
import tatlearn.shared.generated.resources.auth_error_email_invalid
import tatlearn.shared.generated.resources.auth_error_password_short
import tatlearn.shared.generated.resources.auth_error_request
import tatlearn.shared.generated.resources.auth_forgot_password
import tatlearn.shared.generated.resources.auth_google
import tatlearn.shared.generated.resources.auth_hide_password
import tatlearn.shared.generated.resources.auth_legal
import tatlearn.shared.generated.resources.auth_or
import tatlearn.shared.generated.resources.auth_password_label
import tatlearn.shared.generated.resources.auth_show_password
import tatlearn.shared.generated.resources.auth_submit_sign_in
import tatlearn.shared.generated.resources.auth_submit_sign_up
import tatlearn.shared.generated.resources.auth_tab_sign_in
import tatlearn.shared.generated.resources.auth_tab_sign_up
import tatlearn.shared.generated.resources.auth_title_sign_in
import tatlearn.shared.generated.resources.auth_title_sign_up
import tatlearn.shared.generated.resources.auth_vk

private val BackButtonSize = 40.dp
private val BackButtonShape = RoundedCornerShape(12.dp)
private val SocialHeight = 50.dp
private val ProviderMarkSize = 18.dp
private const val PASSWORD_MASK = '•'

@Composable
fun AuthRoute(
    mode: AuthMode,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(parameters = { parametersOf(mode) }),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AuthUiEffect.NavigateToHome -> onNavigateToHome()
                AuthUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }
    val state by viewModel.state.collectAsState()
    AuthScreen(state, viewModel::onEvent)
}

@Composable
fun AuthScreen(state: AuthUiState, onEvent: (AuthUiEvent) -> Unit) {
    val colors = TamyrTheme.colors
    val spacing = TamyrTheme.spacing
    val signIn = state.mode == AuthMode.SignIn

    Column(Modifier.fillMaxSize().background(colors.background).safeDrawingPadding().imePadding()) {
        Row(
            Modifier.padding(start = spacing.screen, end = spacing.screen, top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(BackButtonSize)
                    .clip(BackButtonShape)
                    .background(colors.surface)
                    .border(TamyrSizes.borderThin, colors.border, BackButtonShape)
                    .clickable(role = Role.Button) { onEvent(AuthUiEvent.BackClicked) },
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(Res.string.auth_back), style = TamyrTheme.typography.title18, color = colors.textSecondary)
            }
            Text(
                stringResource(if (signIn) Res.string.auth_title_sign_in else Res.string.auth_title_sign_up),
                style = TamyrTheme.typography.title22,
                color = colors.textPrimary,
            )
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = spacing.screen, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            TamyrSegmentedTabs(
                options = listOf(stringResource(Res.string.auth_tab_sign_in), stringResource(Res.string.auth_tab_sign_up)),
                selectedIndex = if (signIn) 0 else 1,
                onSelect = { onEvent(AuthUiEvent.ModeChanged(if (it == 0) AuthMode.SignIn else AuthMode.SignUp)) },
            )
            TamyrTextField(
                value = state.email,
                onValueChange = { onEvent(AuthUiEvent.EmailChanged(it)) },
                label = stringResource(Res.string.auth_email_label),
                errorText = state.emailError?.let {
                    stringResource(if (it == EmailError.Empty) Res.string.auth_error_email_empty else Res.string.auth_error_email_invalid)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            TamyrTextField(
                value = state.password,
                onValueChange = { onEvent(AuthUiEvent.PasswordChanged(it)) },
                label = stringResource(Res.string.auth_password_label),
                errorText = state.passwordError?.let { stringResource(Res.string.auth_error_password_short) },
                visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(PASSWORD_MASK),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailing = {
                    Text(
                        stringResource(if (state.passwordVisible) Res.string.auth_hide_password else Res.string.auth_show_password),
                        style = TamyrTheme.typography.caption13Bold,
                        color = colors.accent,
                        modifier = Modifier.clickable(role = Role.Button) { onEvent(AuthUiEvent.TogglePasswordVisibility) },
                    )
                },
            )
            if (signIn) {
                // TODO(design): у «Забыли пароль» пока нет целевого экрана.
                Text(
                    stringResource(Res.string.auth_forgot_password),
                    style = TamyrTheme.typography.body14Bold,
                    color = colors.accent,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            TamyrButton(
                text = stringResource(if (signIn) Res.string.auth_submit_sign_in else Res.string.auth_submit_sign_up),
                onClick = { onEvent(AuthUiEvent.SubmitClicked) },
                enabled = !state.isSubmitting,
            )
            state.requestErrorCode?.let {
                Text(stringResource(Res.string.auth_error_request, it), style = TamyrTheme.typography.caption13, color = colors.accent)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.m), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(TamyrSizes.borderThin).background(colors.border))
                Text(stringResource(Res.string.auth_or), style = TamyrTheme.typography.caption13, color = colors.textSecondary)
                Box(Modifier.weight(1f).height(TamyrSizes.borderThin).background(colors.border))
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SocialButton(stringResource(Res.string.auth_google), colors.gold, CircleShape) { onEvent(AuthUiEvent.SocialClicked(SocialProvider.Google)) }
                SocialButton(stringResource(Res.string.auth_vk), colors.primary, RoundedCornerShape(5.dp)) { onEvent(AuthUiEvent.SocialClicked(SocialProvider.Vk)) }
            }
        }
        Text(
            stringResource(Res.string.auth_legal),
            style = TamyrTheme.typography.caption13.copy(lineHeight = TamyrTheme.typography.caption13.fontSize * 1.5f),
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(start = spacing.xxl, end = spacing.xxl, bottom = spacing.xxl),
        )
    }
}

/** TODO(design): логотипы провайдеров в макете тоже просто цветные метки. */
@Composable
private fun SocialButton(text: String, markColor: Color, markShape: Shape, onClick: () -> Unit) {
    val colors = TamyrTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(SocialHeight)
            .clip(TamyrTheme.shapes.answer)
            .background(colors.surface)
            .border(TamyrSizes.borderThin, colors.border, TamyrTheme.shapes.answer)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(ProviderMarkSize).clip(markShape).background(markColor))
        Text(text, style = TamyrTheme.typography.body15Bold, color = colors.textPrimary)
    }
}

// region Превью

private const val Group = "Auth"

@Composable
private fun AuthPreview(state: AuthUiState) = PreviewFrame { AuthScreen(state) {} }

@Preview(name = "Sign in · empty", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun AuthSignInPreview() = AuthPreview(AuthUiState())

@Preview(name = "Sign in · filled, password shown", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun AuthSignInFilledPreview() = AuthPreview(AuthUiState(email = "aliya@example.com", password = "secret123", passwordVisible = true))

@Preview(name = "Sign in · field errors", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun AuthFieldErrorsPreview() = AuthPreview(
    AuthUiState(email = "aliya@", password = "123", emailError = EmailError.Invalid, passwordError = PasswordError.TooShort),
)

@Preview(name = "Sign in · empty email", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun AuthEmptyEmailPreview() = AuthPreview(AuthUiState(password = "secret123", emailError = EmailError.Empty))

@Preview(name = "Sign in · submitting", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun AuthSubmittingPreview() = AuthPreview(AuthUiState(email = "aliya@example.com", password = "secret123", isSubmitting = true))

@Preview(name = "Sign in · request error", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun AuthRequestErrorPreview() = AuthPreview(
    AuthUiState(email = "aliya@example.com", password = "secret123", requestErrorCode = "NET_TIMEOUT"),
)

@Preview(name = "Sign up · empty", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun AuthSignUpPreview() = AuthPreview(AuthUiState(mode = AuthMode.SignUp))

@Preview(name = "Sign up · field errors", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun AuthSignUpErrorsPreview() = AuthPreview(
    AuthUiState(mode = AuthMode.SignUp, email = "no-at-sign", password = "12", emailError = EmailError.Invalid, passwordError = PasswordError.TooShort),
)

// endregion

