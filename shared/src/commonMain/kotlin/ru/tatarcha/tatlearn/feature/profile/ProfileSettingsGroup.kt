package ru.tatarcha.tatlearn.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.icon.SoundIcon
import ru.tatarcha.tatlearn.core.designsystem.component.icon.VibrationIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.NarratorVoice
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.profile_haptics_subtitle
import tatlearn.shared.generated.resources.profile_haptics_title
import tatlearn.shared.generated.resources.profile_settings_title
import tatlearn.shared.generated.resources.profile_sounds_subtitle
import tatlearn.shared.generated.resources.profile_sounds_title

@Composable
internal fun ProfileSettingsGroup(
    hapticsEnabled: Boolean,
    soundsEnabled: Boolean,
    narratorVoice: NarratorVoice,
    onHapticsToggle: (Boolean) -> Unit,
    onSoundsToggle: (Boolean) -> Unit,
    onNarratorSelect: (NarratorVoice) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m)) {
        Text(
            text = stringResource(Res.string.profile_settings_title),
            style = TamyrTheme.typography.title18,
            color = TamyrTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        TamyrCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 0.dp,
        ) {
            Column {
                ProfileToggleRow(
                    icon = { VibrationIcon() },
                    title = stringResource(Res.string.profile_haptics_title),
                    subtitle = stringResource(Res.string.profile_haptics_subtitle),
                    enabled = hapticsEnabled,
                    onToggle = onHapticsToggle,
                )
                SettingsDivider()
                ProfileToggleRow(
                    icon = { SoundIcon() },
                    title = stringResource(Res.string.profile_sounds_title),
                    subtitle = stringResource(Res.string.profile_sounds_subtitle),
                    enabled = soundsEnabled,
                    onToggle = onSoundsToggle,
                )
                SettingsDivider()
                ProfileNarratorBlock(
                    voice = narratorVoice,
                    onSelect = onNarratorSelect,
                )
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = TamyrTheme.spacing.l),
        thickness = TamyrSizes.borderThin,
        color = TamyrTheme.colors.border,
    )
}
