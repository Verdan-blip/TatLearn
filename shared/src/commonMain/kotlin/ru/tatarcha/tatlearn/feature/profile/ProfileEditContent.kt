package ru.tatarcha.tatlearn.feature.profile

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrConfirmSheet
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTextField
import ru.tatarcha.tatlearn.core.designsystem.component.icon.ChevronBackIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.AvatarPose
import ru.tatarcha.tatlearn.domain.model.NameError
import ru.tatarcha.tatlearn.domain.model.ProfileRules
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.intro_counter
import tatlearn.shared.generated.resources.intro_name_label
import tatlearn.shared.generated.resources.intro_name_too_long
import tatlearn.shared.generated.resources.profile_edit_empty_name
import tatlearn.shared.generated.resources.profile_edit_exit_message
import tatlearn.shared.generated.resources.profile_edit_exit_stay
import tatlearn.shared.generated.resources.profile_edit_exit_title
import tatlearn.shared.generated.resources.profile_edit_leave
import tatlearn.shared.generated.resources.profile_edit_save
import tatlearn.shared.generated.resources.profile_edit_title
import tatlearn.shared.generated.resources.profile_edit_back_description

private val PreviewAvatarSize = 96.dp

/** Экраны 27, 30, 31, 32: имя и аватар (цвет или поза), сохранение кнопкой, подтверждение выхода с несохранёнными изменениями. */
@Composable
fun ProfileEditContent(
    state: ProfileEditUiState,
    onEvent: (ProfileEditUiEvent) -> Unit,
) {
    val spacing = TamyrTheme.spacing
    val backDescription = stringResource(Res.string.profile_edit_back_description)
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(TamyrTheme.colors.background)
                .statusBarsPadding()
                .imePadding(),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = spacing.m, end = spacing.screen, top = spacing.s),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
            ) {
                Box(
                    Modifier
                        .size(TamyrSizes.topBarAction)
                        .clip(CircleShape)
                        .clickable(role = Role.Button) { onEvent(ProfileEditUiEvent.BackClicked) }
                        .semantics { contentDescription = backDescription },
                    contentAlignment = Alignment.Center,
                ) { ChevronBackIcon() }
                Text(
                    stringResource(Res.string.profile_edit_title),
                    style = TamyrTheme.typography.title22,
                    color = TamyrTheme.colors.textPrimary,
                    modifier = Modifier.semantics { heading() },
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) { detectTapGestures(onTap = { dismissKeyboard(focusManager, keyboard) }) }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.screen, vertical = spacing.l),
                verticalArrangement = Arrangement.spacedBy(spacing.xl),
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ProfileAvatar(state.avatar, state.name, PreviewAvatarSize)
                }
                TamyrTextField(
                    value = state.name,
                    onValueChange = { onEvent(ProfileEditUiEvent.NameChanged(it)) },
                    label = stringResource(Res.string.intro_name_label),
                    counter = state.counter?.let { (length, max) -> stringResource(Res.string.intro_counter, length, max) },
                    errorText = when (state.nameError) {
                        NameError.Empty -> stringResource(Res.string.profile_edit_empty_name)
                        NameError.TooLong -> stringResource(Res.string.intro_name_too_long, ProfileRules.MAX_NAME_LENGTH)
                        null -> null
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    // «Готово» только закрывает клавиатуру; сохранение отдельной кнопкой.
                    keyboardActions = KeyboardActions(onDone = { dismissKeyboard(focusManager, keyboard) }),
                )
                AvatarPicker(
                    selected = state.avatar,
                    name = state.name,
                    onSelect = { onEvent(ProfileEditUiEvent.AvatarSelected(it)) },
                    showPoses = true,
                )
            }
            Box(
                Modifier
                    .navigationBarsPadding()
                    .padding(start = spacing.screen, end = spacing.screen, top = spacing.s, bottom = spacing.l),
            ) {
                TamyrButton(stringResource(Res.string.profile_edit_save), onClick = { onEvent(ProfileEditUiEvent.SaveClicked) }, enabled = state.canSave)
            }
        }
        TamyrConfirmSheet(
            visible = state.exitSheet,
            title = stringResource(Res.string.profile_edit_exit_title),
            message = stringResource(Res.string.profile_edit_exit_message),
            confirmText = stringResource(Res.string.profile_edit_exit_stay),
            dismissText = stringResource(Res.string.profile_edit_leave),
            onConfirm = { onEvent(ProfileEditUiEvent.ExitDismissed) },
            onDismiss = { onEvent(ProfileEditUiEvent.ExitConfirmed) },
        )
    }
}

@Preview(name = "Edit", widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditPreview() = PreviewFrame {
    ProfileEditContent(ProfileEditUiState("Алия", Avatar.Color(1), "Алия", Avatar.Color(0)), onEvent = {})
}

@Preview(name = "Edit · pose", widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditPosePreview() = PreviewFrame {
    ProfileEditContent(ProfileEditUiState("Алия", Avatar.Pose(AvatarPose.Searching), "Алия", Avatar.Color(0)), onEvent = {})
}

@Preview(name = "Edit · empty name", widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditEmptyPreview() = PreviewFrame {
    ProfileEditContent(ProfileEditUiState("", Avatar.Color(3), "Алия", Avatar.Color(3)), onEvent = {})
}

@Preview(name = "Edit · exit sheet", widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditSheetPreview() = PreviewFrame {
    ProfileEditContent(ProfileEditUiState("Алия", Avatar.Color(1), "Алия", Avatar.Color(0), exitSheet = true), onEvent = {})
}
