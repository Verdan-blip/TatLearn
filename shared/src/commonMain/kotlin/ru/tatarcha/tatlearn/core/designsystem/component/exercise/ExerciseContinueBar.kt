package ru.tatarcha.tatlearn.core.designsystem.component.exercise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Нижняя кнопка урока без проверки ответа: карточки новых слов и правила, где нужно только «Дальше». */
@Composable
fun ExerciseContinueBar(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, bottom = TamyrTheme.spacing.l),
    ) {
        TamyrButton(
            text = text,
            onClick = onClick,
        )
    }
}

@Preview
@Composable
private fun ExerciseContinueBarPreview() = PreviewFrame {
    ExerciseContinueBar(
        text = "Дәвам · Дальше",
        onClick = {},
    )
}
