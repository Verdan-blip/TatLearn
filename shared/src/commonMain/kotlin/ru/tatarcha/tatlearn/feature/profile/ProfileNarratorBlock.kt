package ru.tatarcha.tatlearn.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrChoiceChips
import ru.tatarcha.tatlearn.core.designsystem.component.icon.SoundIcon
import ru.tatarcha.tatlearn.core.designsystem.component.icon.VoiceIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.NarratorVoice
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.profile_narrator_female
import tatlearn.shared.generated.resources.profile_narrator_female_description
import tatlearn.shared.generated.resources.profile_narrator_male
import tatlearn.shared.generated.resources.profile_narrator_male_description
import tatlearn.shared.generated.resources.profile_narrator_subtitle
import tatlearn.shared.generated.resources.profile_narrator_title

@Composable
internal fun ProfileNarratorBlock(
    voice: NarratorVoice,
    onSelect: (NarratorVoice) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(TamyrTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.l),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.blockGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileIconTile(icon = { VoiceIcon() })
            Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
                Text(
                    text = stringResource(Res.string.profile_narrator_title),
                    style = TamyrTheme.typography.body16Bold,
                    color = TamyrTheme.colors.textPrimary,
                )
                Text(
                    text = stringResource(Res.string.profile_narrator_subtitle),
                    style = TamyrTheme.typography.caption13,
                    color = TamyrTheme.colors.textSecondary,
                )
            }
        }
        TamyrChoiceChips(
            options = listOf(
                stringResource(Res.string.profile_narrator_male),
                stringResource(Res.string.profile_narrator_female),
            ),
            selectedIndex = NarratorVoice.entries.indexOf(voice),
            onSelect = { index -> onSelect(NarratorVoice.entries[index]) },
            descriptions = listOf(
                stringResource(Res.string.profile_narrator_male_description),
                stringResource(Res.string.profile_narrator_female_description),
            ),
            leadingIcon = { selected ->
                SoundIcon(
                    size = TamyrSizes.chipIcon,
                    tint = if (selected) TamyrTheme.colors.onPrimary else TamyrTheme.colors.primary,
                )
            },
        )
    }
}
