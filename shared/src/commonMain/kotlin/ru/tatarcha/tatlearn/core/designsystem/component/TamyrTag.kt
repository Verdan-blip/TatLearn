package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

enum class TamyrTagKind { Success, Error, Gold }

/** Небольшая подпись заглавными буквами над заданием упражнения. */
@Composable
fun TamyrTag(text: String, kind: TamyrTagKind, modifier: Modifier = Modifier) {
    val colors = TamyrTheme.colors
    val (container, content) = when (kind) {
        TamyrTagKind.Success -> colors.successContainer to colors.primary
        TamyrTagKind.Error -> colors.errorContainer to colors.accent
        TamyrTagKind.Gold -> colors.goldContainer to colors.goldText
    }
    Text(
        text = text.toUpperCase(Locale.current),
        color = content,
        style = TamyrTheme.typography.tag,
        modifier = modifier
            .clip(TamyrTheme.shapes.tag)
            .background(container)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}
