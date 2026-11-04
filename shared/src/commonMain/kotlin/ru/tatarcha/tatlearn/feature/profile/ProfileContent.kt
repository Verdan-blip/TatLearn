package ru.tatarcha.tatlearn.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButtonStyle
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrProgressBar
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastData
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastHost
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastKind
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.AvatarPose
import ru.tatarcha.tatlearn.domain.model.UserStats
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.debug_tools_open
import tatlearn.shared.generated.resources.profile_edit
import tatlearn.shared.generated.resources.reminder_permission_denied
import tatlearn.shared.generated.resources.profile_level
import tatlearn.shared.generated.resources.profile_level_value
import tatlearn.shared.generated.resources.profile_local_note
import tatlearn.shared.generated.resources.profile_next_level
import tatlearn.shared.generated.resources.profile_saved
import tatlearn.shared.generated.resources.profile_streak
import tatlearn.shared.generated.resources.profile_streak_value
import tatlearn.shared.generated.resources.profile_title
import tatlearn.shared.generated.resources.profile_xp
import tatlearn.shared.generated.resources.profile_xp_to_go

private val BigAvatarSize = 96.dp

/**
 * Вкладка «Профиль» (макеты 26 и 33). [onOpenDebug] не null только в отладочной сборке: кнопка входа в инструменты отладки.
 * Нижнее меню рисует главная, поэтому здесь только содержимое вкладки.
 */
@Composable
fun ProfileContent(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDebug: (() -> Unit)? = null,
) {
    val spacing = TamyrTheme.spacing
    Box(modifier.fillMaxSize().background(TamyrTheme.colors.background)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = spacing.screen, vertical = spacing.l),
            verticalArrangement = Arrangement.spacedBy(spacing.xl),
        ) {
            Text(
                stringResource(Res.string.profile_title),
                style = TamyrTheme.typography.title22,
                color = TamyrTheme.colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                ProfileAvatar(state.avatar, state.name, BigAvatarSize)
                Text(state.name, style = TamyrTheme.typography.headline24, color = TamyrTheme.colors.textPrimary, textAlign = TextAlign.Center)
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing.l)) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.m)) {
                    StatTile(stringResource(Res.string.profile_streak_value, state.stats.streak), stringResource(Res.string.profile_streak), Modifier.weight(1f))
                    StatTile(state.stats.xp.toString(), stringResource(Res.string.profile_xp), Modifier.weight(1f))
                    StatTile(stringResource(Res.string.profile_level_value, state.stats.level), stringResource(Res.string.profile_level), Modifier.weight(1f))
                }
                NextLevel(state.stats)
            }
            ProfileSettingsGroup(
                hapticsEnabled = state.hapticsEnabled,
                soundsEnabled = state.soundsEnabled,
                narratorVoice = state.narratorVoice,
                onHapticsToggle = { onEvent(ProfileUiEvent.HapticsToggled(it)) },
                onSoundsToggle = { onEvent(ProfileUiEvent.SoundsToggled(it)) },
                onNarratorSelect = { onEvent(ProfileUiEvent.NarratorVoiceSelected(it)) },
            )
            ProfileReminderCard(
                enabled = state.reminderEnabled,
                hour = state.reminderHour,
                onToggle = { onEvent(ProfileUiEvent.ReminderToggled(it)) },
                onHourSelected = { onEvent(ProfileUiEvent.ReminderHourSelected(it)) },
            )
            TamyrButton(
                text = stringResource(Res.string.profile_edit),
                onClick = { onEvent(ProfileUiEvent.EditClicked) },
                style = TamyrButtonStyle.Secondary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                ProfileAboutRow(onClick = { onEvent(ProfileUiEvent.AboutClicked) })
                Text(
                    text = stringResource(Res.string.profile_local_note),
                    style = TamyrTheme.typography.body14,
                    color = TamyrTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (onOpenDebug != null) {
                TamyrButton(stringResource(Res.string.debug_tools_open), onClick = onOpenDebug, style = TamyrButtonStyle.SecondaryAccent)
            }
        }
        TamyrToastHost(
            toast = when {
                state.savedToast -> TamyrToastData(stringResource(Res.string.profile_saved), TamyrToastKind.Success)
                state.reminderDeniedToast -> TamyrToastData(stringResource(Res.string.reminder_permission_denied), TamyrToastKind.Error)
                else -> null
            },
            onDismiss = { onEvent(ProfileUiEvent.ToastDismissed) },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

/** Плоская плитка статистики: без нижней тени, чтобы не выглядела как кнопка. */
@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    TamyrCard(modifier, contentPadding = TamyrTheme.spacing.m) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
            Text(value, style = TamyrTheme.typography.title20, color = TamyrTheme.colors.textPrimary, textAlign = TextAlign.Center)
            Text(label, style = TamyrTheme.typography.caption13, color = TamyrTheme.colors.textSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun NextLevel(stats: UserStats) {
    Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(Res.string.profile_next_level, stats.level + 1), style = TamyrTheme.typography.caption13, color = TamyrTheme.colors.textSecondary)
            Text(stringResource(Res.string.profile_xp_to_go, stats.xpToNextLevel), style = TamyrTheme.typography.caption13Bold, color = TamyrTheme.colors.textSecondary)
        }
        TamyrProgressBar(
            progress = (UserStats.XP_PER_LEVEL - stats.xpToNextLevel) / UserStats.XP_PER_LEVEL.toFloat(),
            height = TamyrSizes.lessonProgress,
            color = TamyrTheme.colors.gold,
        )
    }
}

@Preview(name = "Profile", widthDp = 390, heightDp = 844)
@Composable
private fun ProfilePreview() = PreviewFrame {
    ProfileContent(ProfileUiState("Алия", Avatar.Color(0), UserStats(xp = 340, streak = 12)), onEvent = {})
}

@Preview(name = "Profile · pose, saved, debug", widthDp = 390, heightDp = 844)
@Composable
private fun ProfilePosePreview() = PreviewFrame {
    ProfileContent(
        ProfileUiState("Алия", Avatar.Pose(AvatarPose.Searching), UserStats(xp = 340, streak = 12), savedToast = true),
        onEvent = {},
        onOpenDebug = {},
    )
}

@Preview(name = "Profile · haptics off", widthDp = 390, heightDp = 844)
@Composable
private fun ProfileHapticsOffPreview() = PreviewFrame {
    ProfileContent(ProfileUiState("Алия", Avatar.Color(0), UserStats(xp = 340, streak = 12), hapticsEnabled = false), onEvent = {})
}

@Preview(name = "Profile · reminder on", widthDp = 390, heightDp = 844)
@Composable
private fun ProfileReminderOnPreview() = PreviewFrame {
    ProfileContent(
        ProfileUiState("Алия", Avatar.Color(0), UserStats(xp = 340, streak = 12), reminderEnabled = true, reminderHour = 19),
        onEvent = {},
    )
}

@Preview(name = "Profile · new user", widthDp = 390, heightDp = 844)
@Composable
private fun ProfileNewPreview() = PreviewFrame { ProfileContent(ProfileUiState("Друг", Avatar.Color(2)), onEvent = {}) }

@Preview(name = "Profile · small phone", widthDp = 320, heightDp = 568)
@Composable
private fun ProfileSmallPreview() = PreviewFrame {
    ProfileContent(ProfileUiState("Александра-Мөхәммәдия", Avatar.Color(1), UserStats(xp = 1990, streak = 365)), onEvent = {})
}
