package ru.tatarcha.tatlearn.feature.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import ru.tatarcha.tatlearn.feature.lesson.dismissKeyboard
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTextField
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.NameError
import ru.tatarcha.tatlearn.domain.model.ProfileRules
import ru.tatarcha.tatlearn.feature.profile.AvatarPicker
import ru.tatarcha.tatlearn.feature.profile.ProfileAvatar
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.intro_continue
import tatlearn.shared.generated.resources.intro_counter
import tatlearn.shared.generated.resources.intro_name_hint
import tatlearn.shared.generated.resources.intro_name_label
import tatlearn.shared.generated.resources.intro_name_too_long
import tatlearn.shared.generated.resources.intro_skip
import tatlearn.shared.generated.resources.intro_subtitle
import tatlearn.shared.generated.resources.intro_title

private val MascotSize = 120.dp
private val PreviewAvatarSize = 72.dp

/** Экраны 25, 28, 29: знакомство в гостевом режиме (имя и цвет аватара, «Пропустить» ставит имя по умолчанию). */
@Composable
fun IntroContent(
    state: IntroUiState,
    onEvent: (IntroUiEvent) -> Unit,
) {
    val spacing = TamyrTheme.spacing
    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    Column(
        Modifier
            .fillMaxSize()
            .background(TamyrTheme.colors.background)
            .statusBarsPadding()
            .imePadding(),
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                // Тап по свободному месту закрывает клавиатуру, не переходя дальше.
                .pointerInput(Unit) { detectTapGestures(onTap = { dismissKeyboard(focusManager, keyboard) }) }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screen, vertical = spacing.s),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                // При открытой клавиатуре маскот прячется, чтобы поле, цвета и кнопка уместились (макет 29).
                if (!keyboardOpen) TamyrMascot(MascotPose.Greeting, MascotSize)
                Text(stringResource(Res.string.intro_title), style = TamyrTheme.typography.headline26, color = TamyrTheme.colors.textPrimary, textAlign = TextAlign.Center)
                if (!keyboardOpen) {
                    Text(stringResource(Res.string.intro_subtitle), style = TamyrTheme.typography.body15, color = TamyrTheme.colors.textSecondary, textAlign = TextAlign.Center)
                }
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ProfileAvatar(state.avatar, state.name, PreviewAvatarSize)
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                TamyrTextField(
                    value = state.name,
                    onValueChange = { onEvent(IntroUiEvent.NameChanged(it)) },
                    label = stringResource(Res.string.intro_name_label),
                    placeholder = ProfileRules.DEFAULT_NAME,
                    counter = state.counter?.let { (length, max) -> stringResource(Res.string.intro_counter, length, max) },
                    errorText = if (state.nameError == NameError.TooLong) {
                        stringResource(Res.string.intro_name_too_long, ProfileRules.MAX_NAME_LENGTH)
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    // «Готово» только закрывает клавиатуру: человек мог ещё не выбрать цвет, переход вперёд только кнопкой «Продолжить».
                    keyboardActions = KeyboardActions(onDone = { dismissKeyboard(focusManager, keyboard) }),
                )
                if (state.nameError == null) {
                    Text(stringResource(Res.string.intro_name_hint), style = TamyrTheme.typography.caption13, color = TamyrTheme.colors.textSecondary)
                }
            }
            AvatarPicker(
                selected = state.avatar,
                name = state.name,
                onSelect = { onEvent(IntroUiEvent.ColorSelected(it)) },
                showPoses = false,
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = spacing.screen, end = spacing.screen, top = spacing.m, bottom = spacing.l),
            verticalArrangement = Arrangement.spacedBy(spacing.s),
        ) {
            TamyrButton(stringResource(Res.string.intro_continue), onClick = { onEvent(IntroUiEvent.ContinueClicked) }, enabled = state.canContinue)
            Box(
                Modifier.fillMaxWidth().height(SkipHeight).clickable(role = Role.Button) { onEvent(IntroUiEvent.SkipClicked) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(Res.string.intro_skip),
                    style = TamyrTheme.typography.body16Bold,
                    color = TamyrTheme.colors.textSecondary,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
    }
}

private val SkipHeight = 48.dp

@Preview(name = "Intro", widthDp = 390, heightDp = 844)
@Composable
private fun IntroPreview() = PreviewFrame { IntroContent(IntroUiState(name = "Алия"), onEvent = {}) }

@Preview(name = "Intro · empty", widthDp = 390, heightDp = 844)
@Composable
private fun IntroEmptyPreview() = PreviewFrame { IntroContent(IntroUiState(), onEvent = {}) }

@Preview(name = "Intro · too long", widthDp = 390, heightDp = 844)
@Composable
private fun IntroTooLongPreview() = PreviewFrame {
    IntroContent(IntroUiState(name = "Александра-Мөхәммәдия-Фатыйх", avatar = Avatar.Color(2)), onEvent = {})
}

@Preview(name = "Intro · small phone", widthDp = 320, heightDp = 568)
@Composable
private fun IntroSmallPreview() = PreviewFrame { IntroContent(IntroUiState(name = "Алия"), onEvent = {}) }
