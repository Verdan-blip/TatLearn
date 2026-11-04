package ru.tatarcha.tatlearn.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

@Composable
internal fun ProfileToggleRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TamyrSizes.lessonRowMinHeight)
            .toggleable(
                value = enabled,
                role = Role.Switch,
                onValueChange = onToggle,
            )
            .padding(
                horizontal = TamyrTheme.spacing.l,
                vertical = TamyrTheme.spacing.m,
            ),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.blockGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileIconTile(icon = icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs),
        ) {
            Text(
                text = title,
                style = TamyrTheme.typography.body16Bold,
                color = TamyrTheme.colors.textPrimary,
            )
            Text(
                text = subtitle,
                style = TamyrTheme.typography.caption13,
                color = TamyrTheme.colors.textSecondary,
            )
        }
        // Нажатие обрабатывает вся строка (toggleable), сам переключатель без реакции: так цель нажатия крупнее.
        Switch(
            checked = enabled,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TamyrTheme.colors.onPrimary,
                checkedTrackColor = TamyrTheme.colors.primary,
                checkedBorderColor = TamyrTheme.colors.primary,
                uncheckedThumbColor = TamyrTheme.colors.textSecondary,
                uncheckedTrackColor = TamyrTheme.colors.surfaceMuted,
                uncheckedBorderColor = TamyrTheme.colors.textSecondary,
            ),
        )
    }
}
