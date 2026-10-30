package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Ряд равных кнопок-таблеток для выбора одного значения (например, времени напоминания). */
@Composable
fun TamyrChoiceChips(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = TamyrSizes.topBarAction)
                    .clip(TamyrTheme.shapes.chip)
                    .background(if (selected) TamyrTheme.colors.primary else TamyrTheme.colors.surface)
                    .border(
                        width = TamyrSizes.borderThin,
                        color = if (selected) TamyrTheme.colors.primary else TamyrTheme.colors.borderStrong,
                        shape = TamyrTheme.shapes.chip,
                    )
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onSelect(index) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    style = TamyrTheme.typography.body15Bold,
                    color = if (selected) TamyrTheme.colors.onPrimary else TamyrTheme.colors.textPrimary,
                )
            }
        }
    }
}

@Preview(name = "Choice chips")
@Composable
private fun TamyrChoiceChipsPreview() = PreviewFrame {
    TamyrChoiceChips(
        options = listOf("08:00", "13:00", "19:00", "21:00"),
        selectedIndex = 2,
        onSelect = {},
    )
}
