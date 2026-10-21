package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

@Immutable
class BottomBarItem(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
    /** Рисует иконку 20dp заданным цветом. */
    val icon: @Composable (tint: Color) -> Unit,
)

@Composable
fun TamyrBottomBar(items: List<BottomBarItem>, modifier: Modifier = Modifier) {
    val colors = TamyrTheme.colors
    Column(modifier.fillMaxWidth().background(colors.surface)) {
        Box(Modifier.fillMaxWidth().height(TamyrSizes.borderThin).background(colors.border))
        Row(
            Modifier.navigationBarsPadding().padding(horizontal = TamyrTheme.spacing.m, vertical = TamyrTheme.spacing.s),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEach { item ->
                val tint = if (item.selected) colors.primary else colors.textSecondary
                Column(
                    Modifier.weight(1f).clickable(onClick = item.onClick).padding(vertical = TamyrTheme.spacing.xxs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) { item.icon(tint) }
                    Text(
                        item.label,
                        style = if (item.selected) TamyrTheme.typography.micro11Bold else TamyrTheme.typography.label12.copy(fontSize = TamyrTheme.typography.micro11Bold.fontSize),
                        color = tint,
                    )
                }
            }
        }
    }
}
