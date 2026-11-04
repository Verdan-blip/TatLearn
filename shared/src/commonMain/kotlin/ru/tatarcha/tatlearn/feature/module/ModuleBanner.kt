package ru.tatarcha.tatlearn.feature.module

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.icon.CheckIcon
import ru.tatarcha.tatlearn.core.designsystem.component.icon.ChevronBackIcon
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.LessonRowState
import ru.tatarcha.tatlearn.domain.model.ModuleDetails
import ru.tatarcha.tatlearn.domain.model.ModuleLessonRow
import ru.tatarcha.tatlearn.domain.model.RequiredModule
import ru.tatarcha.tatlearn.domain.model.moduleTitle
import ru.tatarcha.tatlearn.feature.home.LockIcon
import ru.tatarcha.tatlearn.feature.home.roadmap.EchpochmakRow
import ru.tatarcha.tatlearn.feature.home.roadmap.ModuleIllustration
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.module_back_description
import tatlearn.shared.generated.resources.module_banner_done
import tatlearn.shared.generated.resources.module_plate
import tatlearn.shared.generated.resources.module_progress_value
import tatlearn.shared.generated.resources.module_stamp
import tatlearn.shared.generated.resources.roadmap_lessons_unit

private val IllustrationBox = 176.dp
private val IllustrationHeight = 146.dp
private val PlatformHeight = 34.dp
private val ScallopRadius = 10.dp
private val BackSize = TamyrSizes.topBarAction
private val StampSize = 60.dp

/**
 * Баннер экрана модуля (макеты 18–23): иллюстрация на «кочке» с резным краем, название, эчпочмаки и счётчик уроков.
 * Прокручивается вместе с лентой; когда он уходит, сверху остаётся компактная строка ([ModuleCompactBar]).
 * [showProgress] выключает эчпочмаки и счётчик (пустой модуль).
 */
@Composable
fun ModuleBanner(
    details: ModuleDetails,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showProgress: Boolean = true,
) {
    val module = details.module
    val locked = details.isLocked
    val halo = TamyrTheme.colors.background
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TamyrTheme.colors.goldContainer)
                .drawBehind {
                    val center = Offset(size.width / 2, 150.dp.toPx())
                    drawCircle(color = halo.copy(alpha = 0.4f), radius = 150.dp.toPx(), center = center)
                    drawCircle(color = halo.copy(alpha = 0.7f), radius = 104.dp.toPx(), center = center)
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = TamyrTheme.spacing.xl, start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, bottom = TamyrTheme.spacing.section),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ModuleHeroPlatform(module = module, locked = locked, completed = details.isCompleted)
                Text(
                    text = moduleTitle(module.titleTt, module.titleRu),
                    style = TamyrTheme.typography.headline24,
                    color = if (locked) TamyrTheme.colors.textSecondary else TamyrTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = TamyrTheme.spacing.s)
                        .semantics { heading() },
                )
                if (showProgress) {
                    BannerProgress(details = details)
                }
            }
            BackButton(
                onBack = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = TamyrTheme.spacing.m, top = TamyrTheme.spacing.xxs),
            )
            Text(
                text = stringResource(Res.string.module_plate, details.number, details.totalModules),
                style = TamyrTheme.typography.caption13Bold,
                color = TamyrTheme.colors.textSecondary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = TamyrTheme.spacing.m, end = TamyrTheme.spacing.l)
                    .clip(TamyrTheme.shapes.tag)
                    .background(TamyrTheme.colors.surface)
                    .padding(horizontal = TamyrTheme.spacing.tight, vertical = TamyrTheme.spacing.xxs),
            )
        }
        ScallopEdge()
    }
}

@Composable
private fun ModuleHeroPlatform(
    module: CourseModule,
    locked: Boolean,
    completed: Boolean,
) {
    Box(modifier = Modifier.size(width = IllustrationBox, height = IllustrationHeight)) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = TamyrTheme.spacing.xs)
                .fillMaxWidth()
                .height(PlatformHeight)
                .solidShadow(TamyrTheme.colors.borderStrong, 5.dp, TamyrTheme.shapes.circle)
                .clip(TamyrTheme.shapes.circle)
                .background(if (completed) TamyrTheme.colors.successContainer else TamyrTheme.colors.surface)
                .border(TamyrSizes.borderThin, TamyrTheme.colors.borderStrong, TamyrTheme.shapes.circle),
        )
        ModuleIllustration(
            icon = module.icon,
            locked = locked,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 160.dp, height = 140.dp),
        )
        if (locked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = TamyrTheme.spacing.s, bottom = TamyrTheme.spacing.xs)
                    .size(TamyrSizes.moduleSeal)
                    .clip(TamyrTheme.shapes.circle)
                    .background(TamyrTheme.colors.surface)
                    .border(1.5.dp, TamyrTheme.colors.contour, TamyrTheme.shapes.circle),
                contentAlignment = Alignment.Center,
            ) {
                LockIcon(tint = TamyrTheme.colors.textSecondary)
            }
        }
        if (completed) ModuleStamp(modifier = Modifier.align(Alignment.TopStart).offset(x = (-TamyrTheme.spacing.m), y = TamyrTheme.spacing.xxs))
    }
}

@Composable
private fun ModuleStamp(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .size(StampSize)
            .graphicsLayer { rotationZ = -12f }
            .clip(TamyrTheme.shapes.circle)
            .background(TamyrTheme.colors.gold)
            .border(TamyrSizes.borderAccent, TamyrTheme.colors.surface, TamyrTheme.shapes.circle),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CheckIcon(size = 16.dp, tint = TamyrTheme.colors.textPrimary)
        Text(
            text = stringResource(Res.string.module_stamp),
            style = TamyrTheme.typography.label12Bold,
            color = TamyrTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun BannerProgress(details: ModuleDetails) {
    Row(
        modifier = Modifier.padding(top = TamyrTheme.spacing.s),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.tight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EchpochmakRow(
            done = details.completedLessons,
            total = details.lessons.size,
            iconSize = TamyrSizes.echpochmakCard,
        )
        if (details.isCompleted) {
            Text(
                text = stringResource(Res.string.module_banner_done, details.completedLessons, details.lessons.size),
                style = TamyrTheme.typography.body14Bold,
                color = TamyrTheme.colors.primaryShadow,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
                Text(
                    text = stringResource(Res.string.module_progress_value, details.completedLessons, details.lessons.size),
                    style = TamyrTheme.typography.body15Bold,
                    color = TamyrTheme.colors.textPrimary,
                )
                Text(
                    text = stringResource(Res.string.roadmap_lessons_unit),
                    style = TamyrTheme.typography.body15,
                    color = TamyrTheme.colors.textSecondary,
                )
            }
        }
    }
}

/** Резной край баннера: полукруги по низу, как кромка наличника. */
@Composable
private fun ScallopEdge() {
    val color = TamyrTheme.colors.goldContainer
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(ScallopRadius),
    ) {
        val diameter = ScallopRadius.toPx() * 2
        var x = 0f
        while (x < size.width) {
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(x, -diameter / 2),
                size = Size(diameter, diameter),
            )
            x += diameter
        }
    }
}

@Composable
internal fun BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(Res.string.module_back_description)
    Box(
        modifier = modifier
            .size(BackSize)
            .solidShadow(TamyrTheme.colors.borderStrong, TamyrSizes.buttonShadow, TamyrTheme.shapes.circle)
            .clip(TamyrTheme.shapes.circle)
            .background(TamyrTheme.colors.surface)
            .border(TamyrSizes.borderThin, TamyrTheme.colors.border, TamyrTheme.shapes.circle)
            .clickable(role = Role.Button, onClick = onBack)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        ChevronBackIcon(tint = TamyrTheme.colors.textPrimary)
    }
}

private val previewModule = CourseModule(
    id = "food",
    titleTt = "Ашлар",
    titleRu = "Еда",
    icon = "food",
    lessons = List(5) { Lesson("lesson_${it + 1}", it + 1, "Урок", "Урок") },
)

private fun previewDetails(done: Int, locked: Boolean = false) = ModuleDetails(
    module = previewModule,
    lessons = previewModule.lessons.mapIndexed { index, lesson ->
        ModuleLessonRow(lesson, if (locked) LessonRowState.Locked else if (index < done) LessonRowState.Done else LessonRowState.Available, null)
    },
    completedLessons = done,
    lockedBy = if (locked) RequiredModule("prev", "Кибет", "Магазин и кафе", 1, 4) else null,
    nextLesson = null,
    nextModule = null,
    number = 5,
    totalModules = 9,
)

@Preview(name = "In progress")
@Composable
private fun ModuleBannerPreview() = PreviewFrame {
    ModuleBanner(details = previewDetails(done = 2), onBack = {})
}

@Preview(name = "Completed")
@Composable
private fun ModuleBannerCompletedPreview() = PreviewFrame {
    ModuleBanner(details = previewDetails(done = 5), onBack = {})
}

@Preview(name = "Locked")
@Composable
private fun ModuleBannerLockedPreview() = PreviewFrame {
    ModuleBanner(details = previewDetails(done = 0, locked = true), onBack = {})
}
