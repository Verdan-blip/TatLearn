package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Переключатель на два и более варианта, например «Вход / Регистрация». */
@Composable
fun TamyrSegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TamyrTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .background(colors.surfaceMuted, TamyrTheme.shapes.button)
            .padding(TabsPadding),
        horizontalArrangement = Arrangement.spacedBy(TabsPadding),
    ) {
        options.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .height(TabHeight)
                    .clip(TamyrTheme.shapes.chip)
                    .then(
                        if (selected) {
                            Modifier
                                .background(colors.surface)
                                .border(TamyrSizes.borderThin, colors.border, TamyrTheme.shapes.chip)
                        } else {
                            Modifier
                        },
                    )
                    .clickable(role = Role.Tab) { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    title,
                    style = if (selected) TamyrTheme.typography.body15Bold else TamyrTheme.typography.body15,
                    color = if (selected) colors.textPrimary else colors.textSecondary,
                )
            }
        }
    }
}

private val TabsPadding = 4.dp
private val TabHeight = 42.dp
