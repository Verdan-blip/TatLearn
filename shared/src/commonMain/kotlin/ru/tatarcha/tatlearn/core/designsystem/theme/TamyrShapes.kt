package ru.tatarcha.tatlearn.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class TamyrShapes(
    val tag: Shape = RoundedCornerShape(8.dp),
    val chip: Shape = RoundedCornerShape(11.dp),
    val answer: Shape = RoundedCornerShape(13.dp),
    val button: Shape = RoundedCornerShape(14.dp),
    val card: Shape = RoundedCornerShape(16.dp),
    val tile: Shape = RoundedCornerShape(14.dp),
    val progress: Shape = RoundedCornerShape(5.dp),
    val sheet: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    val circle: Shape = CircleShape,
    val roadmapCard: Shape = RoundedCornerShape(22.dp),
    val moduleBase: Shape = RoundedCornerShape(18.dp),
)
