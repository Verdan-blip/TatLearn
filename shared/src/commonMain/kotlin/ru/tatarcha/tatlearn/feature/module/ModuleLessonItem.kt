package ru.tatarcha.tatlearn.feature.module

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.dashedBorder
import ru.tatarcha.tatlearn.core.designsystem.component.icon.ChevronBackIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.LessonRowState
import ru.tatarcha.tatlearn.domain.model.ModuleLessonRow
import ru.tatarcha.tatlearn.feature.home.roadmap.drawTrailDiamond
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.home_lesson_button
import tatlearn.shared.generated.resources.module_lesson_score
import tatlearn.shared.generated.resources.module_status_done
import tatlearn.shared.generated.resources.module_status_locked
import tatlearn.shared.generated.resources.module_status_next

internal val LessonGap = 16.dp
private val NodeColumn = 64.dp
private val TrailStep = 13.dp
private val TrailDiamond = 3.5.dp

/** Строка урока на ленте модуля: узел-эчпочмак с тропой к следующему уроку и карточка урока. Нажимается целиком. */
@Composable
fun ModuleLessonItem(
    row: ModuleLessonRow,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TamyrTheme.colors
    val isNext = row.state == LessonRowState.Next
    val isLocked = row.state == LessonRowState.Locked
    val title = stringResource(Res.string.home_lesson_button, row.lesson.number, row.lesson.titleTt)
    val statusText = when (row.state) {
        LessonRowState.Done -> stringResource(Res.string.module_status_done, row.bestScore ?: 0)
        LessonRowState.Next -> stringResource(Res.string.module_status_next)
        LessonRowState.Locked -> stringResource(Res.string.module_status_locked)
        LessonRowState.Available -> null
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.tight),
    ) {
        NodeColumn(row = row, isLast = isLast)
        TamyrCard(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = TamyrSizes.lessonRowMinHeight)
                .then(if (isLocked) Modifier.dashedBorder(colors.contour, TamyrTheme.shapes.card) else Modifier)
                .semantics(mergeDescendants = true) { statusText?.let { stateDescription = it } },
            onClick = onClick,
            containerColor = if (isLocked) colors.background else colors.surface,
            borderColor = if (isNext) colors.primary else if (isLocked) colors.background else colors.border,
            borderWidth = if (isNext) TamyrSizes.borderAccent else TamyrSizes.borderThin,
            shadowColor = if (isNext) colors.successBorder else if (isLocked) null else colors.border,
            contentPadding = TamyrTheme.spacing.m,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isNext) {
                        Text(
                            text = stringResource(Res.string.module_status_next),
                            style = TamyrTheme.typography.label12Bold,
                            color = colors.primaryShadow,
                            modifier = Modifier
                                .padding(bottom = TamyrTheme.spacing.xxs / 2)
                                .background(colors.successContainer, TamyrTheme.shapes.tag)
                                .padding(horizontal = TamyrTheme.spacing.s),
                        )
                    }
                    Text(title, style = TamyrTheme.typography.body16Bold, color = colors.textPrimary)
                    Text(row.lesson.titleRu, style = TamyrTheme.typography.body14, color = colors.textSecondary)
                }
                if (row.state == LessonRowState.Done && row.bestScore != null) {
                    Text(
                        text = stringResource(Res.string.module_lesson_score, row.bestScore),
                        style = TamyrTheme.typography.body15Bold,
                        color = colors.primary,
                    )
                }
                if (!isLocked) {
                    ChevronBackIcon(
                        modifier = Modifier.rotate(180f),
                        size = TamyrSizes.chevron,
                        tint = if (isNext) colors.primary else colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun NodeColumn(
    row: ModuleLessonRow,
    isLast: Boolean,
) {
    val passed = row.state == LessonRowState.Done
    val gold = TamyrTheme.colors.gold
    val outline = TamyrTheme.colors.contour
    val nodeHalf = if (row.state == LessonRowState.Next) NextLessonNodeSize / 2 else LessonNodeSize / 2
    Box(
        modifier = Modifier
            .width(NodeColumn)
            .fillMaxHeight()
            .drawBehind {
                if (!isLast) {
                    val x = size.width / 2
                    val step = TrailStep.toPx()
                    var y = size.height / 2 + nodeHalf.toPx() + step / 2
                    val end = size.height + LessonGap.toPx() - step / 2
                    while (y < end) {
                        drawTrailDiamond(
                            center = Offset(x, y),
                            half = TrailDiamond.toPx(),
                            passed = passed,
                            filledColor = gold,
                            outlineColor = outline,
                        )
                        y += step
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        LessonNode(state = row.state, number = row.lesson.number)
    }
}

private fun previewRow(state: LessonRowState, number: Int = 3, score: Int? = null) = ModuleLessonRow(
    lesson = Lesson("lesson_$number", number, "Ит, балык", "Мясо и рыба"),
    state = state,
    bestScore = score,
)

@Preview(name = "Done and next")
@Composable
private fun ModuleLessonItemPreview() = PreviewFrame {
    Column(
        modifier = Modifier.padding(TamyrTheme.spacing.screen),
        verticalArrangement = Arrangement.spacedBy(LessonGap),
    ) {
        ModuleLessonItem(previewRow(LessonRowState.Done, number = 2, score = 92), isLast = false, onClick = {})
        ModuleLessonItem(previewRow(LessonRowState.Next), isLast = false, onClick = {})
        ModuleLessonItem(previewRow(LessonRowState.Available, number = 4), isLast = false, onClick = {})
    }
}

@Preview(name = "Locked")
@Composable
private fun ModuleLessonItemLockedPreview() = PreviewFrame {
    Column(
        modifier = Modifier.padding(TamyrTheme.spacing.screen),
        verticalArrangement = Arrangement.spacedBy(LessonGap),
    ) {
        ModuleLessonItem(previewRow(LessonRowState.Locked, number = 1), isLast = false, onClick = {})
        ModuleLessonItem(previewRow(LessonRowState.Locked, number = 2), isLast = true, onClick = {})
    }
}
