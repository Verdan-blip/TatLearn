package ru.tatarcha.tatlearn.core.designsystem.component.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

enum class WordChipState {
    /** В банке слов, можно нажать. */
    Available,

    /** Уже перенесено в ответ: серая заглушка в банке. */
    Used,

    /** В строке ответа. */
    Placed,
}

private val ChipHeight = 44.dp

@Composable
fun WordChip(text: String, state: WordChipState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TamyrTheme.colors
    val shape = TamyrTheme.shapes.chip
    val base = modifier.height(ChipHeight)
    val styled = when (state) {
        WordChipState.Available -> base
            .solidShadow(colors.border, TamyrSizes.buttonShadow, shape)
            .clip(shape)
            .background(colors.surface)
            .border(TamyrSizes.borderThin, colors.border, shape)
            .clickable(role = Role.Button, onClick = onClick)
        WordChipState.Used -> base.clip(shape).background(colors.surfaceMuted)
        WordChipState.Placed -> base
            .clip(shape)
            .background(colors.successContainer)
            .border(TamyrSizes.borderThin, colors.successBorder, shape)
            .clickable(role = Role.Button, onClick = onClick)
    }
    Box(styled.padding(horizontal = TamyrTheme.spacing.l), contentAlignment = Alignment.Center) {
        Text(
            text,
            style = if (state == WordChipState.Used) TamyrTheme.typography.body17 else TamyrTheme.typography.body17Bold,
            color = when (state) {
                WordChipState.Available -> colors.textPrimary
                WordChipState.Used -> colors.textDisabled
                WordChipState.Placed -> colors.primary
            },
        )
    }
}
