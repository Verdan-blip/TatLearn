package ru.tatarcha.tatlearn.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrChoiceChips
import ru.tatarcha.tatlearn.core.designsystem.component.icon.BellIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.core.notifications.Reminders
import ru.tatarcha.tatlearn.core.notifications.reminderHourLabels
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.reminder_row_subtitle
import tatlearn.shared.generated.resources.reminder_row_title

@Composable
internal fun ProfileReminderCard(
    enabled: Boolean,
    hour: Int,
    onToggle: (Boolean) -> Unit,
    onHourSelected: (Int) -> Unit,
) {
    TamyrCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 0.dp,
    ) {
        Column {
            ProfileToggleRow(
                icon = { BellIcon() },
                title = stringResource(Res.string.reminder_row_title),
                subtitle = stringResource(Res.string.reminder_row_subtitle),
                enabled = enabled,
                onToggle = onToggle,
            )
            if (enabled) {
                TamyrChoiceChips(
                    options = reminderHourLabels(Reminders.AvailableHours),
                    selectedIndex = Reminders.AvailableHours.indexOf(hour),
                    onSelect = { index -> onHourSelected(Reminders.AvailableHours[index]) },
                    modifier = Modifier.padding(
                        start = TamyrTheme.spacing.l,
                        end = TamyrTheme.spacing.l,
                        bottom = TamyrTheme.spacing.l,
                    ),
                )
            }
        }
    }
}
