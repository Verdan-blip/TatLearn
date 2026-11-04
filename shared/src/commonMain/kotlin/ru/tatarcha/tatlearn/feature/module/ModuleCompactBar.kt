package ru.tatarcha.tatlearn.feature.module

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Компактная строка «назад и название»: проявляется, когда баннер ушёл при прокрутке. [alpha] от 0 до 1. */
@Composable
fun ModuleCompactBar(
    title: String,
    onBack: () -> Unit,
    alpha: () -> Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha() }
            .background(TamyrTheme.colors.background),
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(TamyrSizes.topBarAction + TamyrTheme.spacing.s)
                .padding(start = TamyrTheme.spacing.m, end = TamyrTheme.spacing.screen),
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            Text(
                text = title,
                style = TamyrTheme.typography.title22,
                color = TamyrTheme.colors.textPrimary,
                maxLines = 1,
            )
        }
    }
}

@Preview
@Composable
private fun ModuleCompactBarPreview() = PreviewFrame {
    ModuleCompactBar(
        title = "Ашлар · Еда",
        onBack = {},
        alpha = { 1f },
    )
}
