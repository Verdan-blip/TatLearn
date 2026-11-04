package ru.tatarcha.tatlearn.core.designsystem.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier

private const val PRESSED_ALPHA = 0.5f

/** Вместо квадратной волны Material: содержимое (текст, иконка) на нажатии мягко бледнеет и возвращается. */
object TamyrPressIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = PressNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === TamyrPressIndication

    override fun hashCode(): Int = -1

    private class PressNode(private val interactionSource: InteractionSource) : Modifier.Node(), DrawModifierNode {
        private val alpha = Animatable(1f)

        override fun onAttach() {
            coroutineScope.launch {
                interactionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> alpha.animateTo(PRESSED_ALPHA, tween(TamyrMotion.FAST_MS / 2))
                        is PressInteraction.Release, is PressInteraction.Cancel -> alpha.animateTo(1f, tween(TamyrMotion.FAST_MS))
                    }
                }
            }
        }

        override fun ContentDrawScope.draw() {
            val current = alpha.value
            if (current >= 1f) {
                drawContent()
                return
            }
            drawIntoCanvas { canvas ->
                val bounds = Rect(0f, 0f, size.width, size.height)
                canvas.saveLayer(bounds, Paint().apply { this.alpha = current })
                drawContent()
                canvas.restore()
            }
        }
    }
}
