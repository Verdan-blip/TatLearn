package ru.tatarcha.tatlearn.core.designsystem.component.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrProgressBar
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.exercise_close

/** Верхняя панель каждого упражнения: закрыть и прогресс урока. */
@Composable
fun ExerciseHeader(
    progress: Float,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.l),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(Res.string.exercise_close),
            style = TamyrTheme.typography.title20.copy(fontWeight = FontWeight.Normal),
            color = TamyrTheme.colors.textSecondary,
            modifier = Modifier.clickable(role = Role.Button, onClick = onClose),
        )
        TamyrProgressBar(progress, Modifier.weight(1f))
    }
}
