package ru.tatarcha.tatlearn.core.designsystem.component.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButtonStyle
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** TODO(design): диалога нет в макете; собран из существующих стилей карточки и кнопки. */
@Composable
fun TamyrConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        TamyrCard(contentPadding = 20.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m)) {
                Text(title, style = TamyrTheme.typography.title20, color = TamyrTheme.colors.textPrimary)
                Text(message, style = TamyrTheme.typography.body15, color = TamyrTheme.colors.textSecondary)
                Column(Modifier.padding(top = TamyrTheme.spacing.xs), verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m)) {
                    TamyrButton(dismissText, onDismiss)
                    TamyrButton(confirmText, onConfirm, style = TamyrButtonStyle.SecondaryAccent)
                }
            }
        }
    }
}
