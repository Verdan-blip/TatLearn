package ru.tatarcha.tatlearn.core.designsystem.component.exercise

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButtonStyle
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.exercise_check
import tatlearn.shared.generated.resources.exercise_continue
import tatlearn.shared.generated.resources.exercise_correct
import tatlearn.shared.generated.resources.exercise_understood
import tatlearn.shared.generated.resources.exercise_wrong

enum class AnswerFeedback { None, Correct, Wrong }

private val MascotSize = 48.dp

/**
 * Нижняя область, общая для всех упражнений: «Проверить» до ответа, затем зелёная или терракотовая
 * панель с результатом и кнопкой «продолжить».
 */
@Composable
fun ExerciseFooter(
    feedback: AnswerFeedback,
    checkEnabled: Boolean,
    detail: String?,
    onCheck: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TamyrTheme.colors
    val spacing = TamyrTheme.spacing
    AnimatedContent(
        targetState = feedback,
        modifier = modifier,
        transitionSpec = {
            // Панель результата выезжает снизу; возврат к «Проверить» просто плавно проявляется.
            val enter = if (targetState == AnswerFeedback.None) {
                fadeIn(tween(TamyrMotion.FAST_MS))
            } else {
                slideInVertically(tween(TamyrMotion.STEP_MS)) { it } + fadeIn(tween(TamyrMotion.FAST_MS))
            }
            enter togetherWith fadeOut(tween(TamyrMotion.FAST_MS)) using SizeTransform(clip = false)
        },
        label = "exerciseFooter",
    ) { target ->
    when (target) {
        AnswerFeedback.None -> Box(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(start = spacing.screen, end = spacing.screen, bottom = spacing.l),
        ) {
            TamyrButton(stringResource(Res.string.exercise_check), onCheck, enabled = checkEnabled)
        }
        AnswerFeedback.Correct -> FeedbackPanel(
            container = colors.successContainer,
            borderColor = colors.successBorder,
            pose = MascotPose.Correct,
            title = stringResource(Res.string.exercise_correct),
            titleColor = colors.primary,
            detail = detail,
            detailColor = colors.primary,
            buttonText = stringResource(Res.string.exercise_continue),
            buttonStyle = TamyrButtonStyle.Primary,
            onContinue = onContinue,
        )
        AnswerFeedback.Wrong -> FeedbackPanel(
            container = colors.errorContainer,
            borderColor = colors.errorBorder,
            pose = MascotPose.Wrong,
            title = stringResource(Res.string.exercise_wrong),
            titleColor = colors.accent,
            detail = detail,
            detailColor = colors.accentShadow,
            buttonText = stringResource(Res.string.exercise_understood),
            buttonStyle = TamyrButtonStyle.Accent,
            onContinue = onContinue,
        )
    }
    }
}

@Composable
private fun FeedbackPanel(
    container: Color,
    borderColor: Color,
    pose: MascotPose,
    title: String,
    titleColor: Color,
    detail: String?,
    detailColor: Color,
    buttonText: String,
    buttonStyle: TamyrButtonStyle,
    onContinue: () -> Unit,
) {
    val spacing = TamyrTheme.spacing
    Column(
        Modifier
            .fillMaxWidth()
            .background(container)
            .border(TamyrSizes.borderThin, borderColor)
            .navigationBarsPadding()
            .padding(start = spacing.screen, end = spacing.screen, top = spacing.xl, bottom = spacing.l),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.m), verticalAlignment = Alignment.CenterVertically) {
            TamyrMascot(pose, MascotSize)
            Column {
                Text(title, style = TamyrTheme.typography.title18, color = titleColor)
                if (detail != null) Text(detail, style = TamyrTheme.typography.caption13, color = detailColor)
            }
        }
        TamyrButton(buttonText, onContinue, style = buttonStyle)
    }
}
