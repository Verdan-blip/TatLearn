package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTag
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTagKind
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Блок заголовка вверху упражнения: необязательный тег, заголовок, необязательная строка-подсказка. */
@Composable
fun ExerciseTitle(
    title: String,
    modifier: Modifier = Modifier,
    tag: String? = null,
    tagKind: TamyrTagKind = TamyrTagKind.Success,
    subtitle: String? = null,
) {
    Column(modifier.padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.screen), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (tag != null) TamyrTag(tag, tagKind)
        Text(title, style = TamyrTheme.typography.title22, color = TamyrTheme.colors.textPrimary)
        if (subtitle != null) Text(subtitle, style = TamyrTheme.typography.body15, color = TamyrTheme.colors.textSecondary)
    }
}
