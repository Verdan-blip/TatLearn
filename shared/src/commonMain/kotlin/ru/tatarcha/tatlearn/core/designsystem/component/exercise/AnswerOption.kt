package ru.tatarcha.tatlearn.core.designsystem.component.exercise

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.answer_mark_wrong
import tatlearn.shared.generated.resources.answer_state_matched
import tatlearn.shared.generated.resources.answer_state_wrong

enum class AnswerOptionState {
    Neutral,

    /** Выбран, ещё не проверен: рамка primary на белом (терракота зарезервирована за ошибкой). */
    Selected,

    /** Проверен и верный. */
    Correct,

    /** Проверен и неверный: терракотовая рамка и значок ✕, чтобы ошибка читалась не только цветом. TODO(design): нет в макете. */
    Wrong,

    /**
     * Готовая пара в сопоставлении: плоская (без тени) светло-зелёная плитка с рамкой 1dp и приглушённым текстом, без значка.
     * Смысл читается формой и недоступностью нажатия, не только цветом; скринридер получает «Пара найдена».
     */
    Matched,
}

private val BadgeSize = 22.dp
private const val SHAKE_AMPLITUDE = 4f
private const val SHAKE_CYCLES = 2
private const val SHAKE_STEPS = SHAKE_CYCLES * 2 + 1
private val DefaultHeight = 56.dp

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
    /** Ещё не найденная плитка, пока выбрано слово напротив: тень на 1dp глубже, подсказывает, куда нажимать. */
    lifted: Boolean = false,
    /** Неверная пара в сопоставлении: короткое покачивание по горизонтали (сигнал без цвета). */
    shakeOnWrong: Boolean = false,
) {
    val colors = TamyrTheme.colors
    val shape = TamyrTheme.shapes.answer
    val container = when (state) {
        AnswerOptionState.Correct, AnswerOptionState.Matched -> colors.successContainer
        AnswerOptionState.Wrong -> colors.errorContainer
        else -> colors.surface
    }
    val borderColor = when (state) {
        AnswerOptionState.Wrong -> colors.accent
        AnswerOptionState.Selected, AnswerOptionState.Correct -> colors.primary
        AnswerOptionState.Matched -> colors.successBorder
        AnswerOptionState.Neutral -> colors.border
    }
    val borderWidth = when (state) {
        AnswerOptionState.Neutral, AnswerOptionState.Matched -> TamyrSizes.borderThin
        else -> TamyrSizes.borderAccent
    }
    val shadow = when (state) {
        AnswerOptionState.Neutral -> colors.border
        AnswerOptionState.Wrong -> colors.accentSoftShadow
        AnswerOptionState.Selected, AnswerOptionState.Correct -> colors.successBorder
        AnswerOptionState.Matched -> null
    }
    val textColor = when (state) {
        AnswerOptionState.Correct -> colors.primary
        AnswerOptionState.Matched -> colors.textSecondary
        else -> colors.textPrimary
    }
    val strong = (bold && state != AnswerOptionState.Matched) || state == AnswerOptionState.Correct || state == AnswerOptionState.Selected
    val stateText = when (state) {
        AnswerOptionState.Matched -> stringResource(Res.string.answer_state_matched)
        AnswerOptionState.Wrong -> stringResource(Res.string.answer_state_wrong)
        else -> null
    }
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state, shakeOnWrong) {
        if (shakeOnWrong && state == AnswerOptionState.Wrong) {
            repeat(SHAKE_CYCLES) {
                shake.animateTo(SHAKE_AMPLITUDE, tween(TamyrMotion.SHAKE_MS / SHAKE_STEPS))
                shake.animateTo(-SHAKE_AMPLITUDE, tween(TamyrMotion.SHAKE_MS / SHAKE_STEPS))
            }
            shake.animateTo(0f, tween(TamyrMotion.SHAKE_MS / SHAKE_STEPS))
        } else {
            shake.snapTo(0f)
        }
    }
    val density = LocalDensity.current

    Row(
        modifier
            .fillMaxWidth()
            .offset { IntOffset(with(density) { shake.value.dp.roundToPx() }, 0) }
            .then(if (shadow != null) Modifier.solidShadow(shadow, if (lifted) TamyrSizes.tileLiftShadow else TamyrSizes.buttonShadow, shape) else Modifier)
            .height(height)
            .clip(shape)
            .background(container)
            .border(borderWidth, borderColor, shape)
            .clickable(enabled = enabled && state != AnswerOptionState.Matched, role = Role.Button, onClick = onClick)
            .semantics { stateText?.let { stateDescription = it } }
            .padding(horizontal = 18.dp),
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state == AnswerOptionState.Wrong && badge == null) {
                Text(
                    text = stringResource(Res.string.answer_mark_wrong),
                    style = TamyrTheme.typography.body17Bold,
                    color = colors.accentText,
                )
            }
            Text(
                text = text,
                style = if (strong) TamyrTheme.typography.body17Bold else TamyrTheme.typography.body17,
                color = textColor,
            )
        }
        if (badge != null) {
            val filled = state == AnswerOptionState.Correct || state == AnswerOptionState.Wrong
            Box(
                Modifier
                    .size(BadgeSize)
                    .clip(TamyrTheme.shapes.circle)
                    .then(
                        when (state) {
                            AnswerOptionState.Correct -> Modifier.background(colors.primary)
                            AnswerOptionState.Wrong -> Modifier.background(colors.accent)
                            else -> Modifier.border(TamyrSizes.borderThin, colors.border, TamyrTheme.shapes.circle)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (state == AnswerOptionState.Wrong) stringResource(Res.string.answer_mark_wrong) else badge,
                    style = TamyrTheme.typography.caption13,
                    color = if (filled) colors.onPrimary else colors.textSecondary,
                )
            }
        }
    }
}
