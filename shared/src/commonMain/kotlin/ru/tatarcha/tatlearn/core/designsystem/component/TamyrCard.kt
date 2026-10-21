package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Кремовая карточка с рамкой 1dp. Передайте [shadowColor] для вида «0 3px 0» у ответов/плиток. */
@Composable
fun TamyrCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = TamyrTheme.shapes.card,
    containerColor: Color = TamyrTheme.colors.surface,
    borderColor: Color = TamyrTheme.colors.border,
    borderWidth: Dp = TamyrSizes.borderThin,
    shadowColor: Color? = null,
    contentPadding: Dp = TamyrTheme.spacing.l,
    content: @Composable ColumnScope.() -> Unit,
) {
    var chain = modifier
    if (shadowColor != null) chain = chain.solidShadow(shadowColor, TamyrSizes.buttonShadow, shape)
    chain = chain
        .clip(shape)
        .background(containerColor)
        .border(BorderStroke(borderWidth, borderColor), shape)
    if (onClick != null) chain = chain.clickable(onClick = onClick)
    Column(chain.padding(contentPadding), content = content)
}
