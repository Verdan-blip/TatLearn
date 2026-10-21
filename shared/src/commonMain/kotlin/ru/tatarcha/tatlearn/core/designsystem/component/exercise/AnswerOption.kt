package ru.tatarcha.tatlearn.core.designsystem.component.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

enum class AnswerOptionState {
    Neutral,

    /** Выбран, ещё не проверен. */
    Selected,

    /** Проверен и верный. */
    Correct,

    /** Проверен и неверный. TODO(design): нет в макете; терракотовый вариант Correct. */
    Wrong,

    /** Готовая пара в сопоставлении: приглушённый зелёный. */
    Matched,
}

private val BadgeSize = 22.dp
private val DefaultHeight = 56.dp
private const val MATCHED_ALPHA = 0.6f

/** Выбираемая строка/плитка ответа в квизе, аудировании и сопоставлении. */
@Composable
fun AnswerOption(
    text: String,
    state: AnswerOptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    bold: Boolean = false,
    centered: Boolean = false,
    height: Dp = DefaultHeight,
    badge: String? = null,
) {
    val colors = TamyrTheme.colors
    val shape = TamyrTheme.shapes.answer
    val container = when (state) {
        AnswerOptionState.Correct, AnswerOptionState.Matched -> colors.successContainer
        AnswerOptionState.Wrong -> colors.errorContainer
        else -> colors.surface
    }
    val borderColor = when (state) {
        AnswerOptionState.Selected, AnswerOptionState.Wrong -> colors.accent
        AnswerOptionState.Correct -> colors.primary
        AnswerOptionState.Matched -> colors.successBorder
        AnswerOptionState.Neutral -> colors.border
    }
    val borderWidth = when (state) {
        AnswerOptionState.Neutral, AnswerOptionState.Matched -> TamyrSizes.borderThin
        else -> TamyrSizes.borderAccent
    }
    val shadow = when (state) {
        AnswerOptionState.Neutral -> colors.border
        AnswerOptionState.Selected, AnswerOptionState.Wrong -> colors.accentSoftShadow
        AnswerOptionState.Correct -> colors.successBorder
        AnswerOptionState.Matched -> null
    }
    val textColor = when (state) {
        AnswerOptionState.Correct, AnswerOptionState.Matched -> colors.primary
        else -> colors.textPrimary
    }
    val strong = bold || state == AnswerOptionState.Correct || state == AnswerOptionState.Selected

    Row(
        modifier
            .fillMaxWidth()
            .then(if (state == AnswerOptionState.Matched) Modifier.alpha(MATCHED_ALPHA) else Modifier)
            .then(if (shadow != null) Modifier.solidShadow(shadow, TamyrSizes.buttonShadow, shape) else Modifier)
            .height(height)
            .clip(shape)
            .background(container)
            .border(borderWidth, borderColor, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            style = if (strong) TamyrTheme.typography.body17Bold else TamyrTheme.typography.body17,
            color = textColor,
        )
        if (badge != null) {
            val correct = state == AnswerOptionState.Correct
            Box(
                Modifier
                    .size(BadgeSize)
                    .clip(TamyrTheme.shapes.circle)
                    .then(
                        if (correct) Modifier.background(colors.primary) else Modifier.border(TamyrSizes.borderThin, colors.border, TamyrTheme.shapes.circle),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    badge,
                    style = TamyrTheme.typography.caption13,
                    color = if (correct) colors.onPrimary else colors.textSecondary,
                )
            }
        }
    }
}
