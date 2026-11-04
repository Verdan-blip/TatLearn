package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.CurrentLesson
import ru.tatarcha.tatlearn.domain.model.HomeCardKind
import ru.tatarcha.tatlearn.domain.model.LearningModule
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.ModuleState
import ru.tatarcha.tatlearn.domain.model.RoadmapEntry
import ru.tatarcha.tatlearn.domain.model.moduleTitle
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.home_lesson_button
import tatlearn.shared.generated.resources.home_lesson_restart_hint
import tatlearn.shared.generated.resources.roadmap_fraction
import tatlearn.shared.generated.resources.roadmap_lessons_unit
import tatlearn.shared.generated.resources.roadmap_module_current_description
import tatlearn.shared.generated.resources.roadmap_tag_continue
import tatlearn.shared.generated.resources.roadmap_tag_start

private val CardShadow = 5.dp
private val IllustrationShift = (-27).dp
private val BadgeSize = 28.dp

/** Карточка текущего модуля: иллюстрация, Мыраужан, название, эчпочмаки, кнопка следующего урока (макет 03). */
@Composable
fun RoadmapCurrentCard(
    entry: RoadmapEntry,
    current: CurrentLesson,
    totalModules: Int,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    animation: RoadmapAnimation? = null,
) {
    val module = entry.module
    val lit = animation?.takeIf { it.isLessonGain }
    val litFrom = lit?.gain?.card?.fromCompleted
    val shownDone = litFrom ?: module.completedLessons
    val shownCounter = if (lit != null && !lit.counterReached) shownDone else module.completedLessons
    val title = moduleTitle(module.titleTt, module.titleRu)
    val description = stringResource(
        Res.string.roadmap_module_current_description,
        title,
        entry.number,
        totalModules,
        module.completedLessons,
        module.totalLessons,
    )
    val isStart = module.completedLessons == 0
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (animation != null && (animation.isLessonGain || animation.isModuleDone)) Modifier.gainRing(animation, TamyrTheme.shapes.roadmapCard) else Modifier)
                .solidShadow(TamyrTheme.colors.gold, CardShadow, TamyrTheme.shapes.roadmapCard)
                .clip(TamyrTheme.shapes.roadmapCard)
                .background(TamyrTheme.colors.surface)
                .border(TamyrSizes.borderAccent, TamyrTheme.colors.gold, TamyrTheme.shapes.roadmapCard)
                .semantics { contentDescription = description },
        ) {
            CardBand(entry = entry)
            Column(
                modifier = Modifier.padding(
                    start = TamyrTheme.spacing.l,
                    end = TamyrTheme.spacing.l,
                    top = TamyrTheme.spacing.m,
                    bottom = TamyrTheme.spacing.l,
                ),
                verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = TamyrTheme.typography.title22,
                        color = TamyrTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(if (isStart) Res.string.roadmap_tag_start else Res.string.roadmap_tag_continue),
                        style = TamyrTheme.typography.tag,
                        color = TamyrTheme.colors.onPrimary,
                        modifier = Modifier
                            .padding(start = TamyrTheme.spacing.s)
                            .clip(TamyrTheme.shapes.tag)
                            .background(TamyrTheme.colors.primary)
                            .padding(horizontal = TamyrTheme.spacing.s, vertical = TamyrTheme.spacing.xxs / 2),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EchpochmakRow(
                        done = shownDone,
                        total = module.totalLessons,
                        iconSize = TamyrSizes.echpochmakCard,
                        litIndex = litFrom,
                        animation = lit,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
                        Text(
                            text = stringResource(Res.string.roadmap_fraction, shownCounter, module.totalLessons),
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
                if (current.restartsLesson) {
                    Text(
                        text = stringResource(Res.string.home_lesson_restart_hint),
                        style = TamyrTheme.typography.caption13,
                        color = TamyrTheme.colors.textSecondary,
                    )
                }
                TamyrButton(
                    text = stringResource(Res.string.home_lesson_button, current.lesson.number, current.lesson.titleTt),
                    onClick = onStart,
                )
            }
        }
        if (lit != null) {
            RoadmapGainChip(
                animation = lit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = TamyrSizes.roadmapBand - TamyrTheme.spacing.s, end = TamyrTheme.spacing.l),
            )
        }
    }
}

@Composable
private fun CardBand(entry: RoadmapEntry) {
    val halo = TamyrTheme.colors.background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TamyrSizes.roadmapBand)
            .clipToBounds()
            .background(TamyrTheme.colors.goldContainer)
            .drawBehind {
                val center = Offset(size.width * 0.42f, size.height * 1.45f)
                drawCircle(color = halo.copy(alpha = 0.6f), radius = size.height * 1.3f, center = center)
                drawCircle(color = halo, radius = size.height * 0.96f, center = center)
            },
    ) {
        ModuleIllustration(
            icon = entry.module.icon,
            locked = false,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = IllustrationShift, y = TamyrTheme.spacing.xs)
                .size(width = TamyrSizes.roadmapCardIllustrationWidth, height = TamyrSizes.roadmapCardIllustrationHeight),
        )
        TamyrMascot(
            pose = MascotPose.Greeting,
            size = TamyrSizes.roadmapCat,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = -TamyrTheme.spacing.s, y = TamyrTheme.spacing.m),
        )
        Box(
            modifier = Modifier
                .padding(TamyrTheme.spacing.m)
                .size(BadgeSize)
                .clip(TamyrTheme.shapes.circle)
                .background(TamyrTheme.colors.gold)
                .border(TamyrSizes.borderAccent, TamyrTheme.colors.surface, TamyrTheme.shapes.circle),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = entry.number.toString(),
                style = TamyrTheme.typography.body15Bold,
                color = TamyrTheme.colors.textPrimary,
            )
        }
    }
}

private fun previewEntry(done: Int) = RoadmapEntry(
    module = LearningModule(
        id = "family",
        titleTt = "Гаилә",
        titleRu = "Семья",
        icon = "family",
        progressPercent = done * 20,
        lockedBy = null,
        lockedByTitleTt = null,
        lockedByTitleRu = null,
        completedLessons = done,
        totalLessons = 5,
    ),
    number = 3,
    state = ModuleState.Current,
)

private fun previewCurrent(done: Int, restarts: Boolean = false) = CurrentLesson(
    lesson = Lesson("lesson_4", 4, "Туганнар", "Родственники"),
    topicTt = "Гаилә",
    topicRu = "Семья",
    completedLessons = done,
    totalLessons = 5,
    kind = if (done == 0) HomeCardKind.Start else HomeCardKind.Continue,
    restartsLesson = restarts,
)

@Preview(name = "Continue")
@Composable
private fun RoadmapCurrentCardContinuePreview() = PreviewFrame {
    RoadmapCurrentCard(
        entry = previewEntry(done = 3),
        current = previewCurrent(done = 3),
        totalModules = 9,
        onStart = {},
        modifier = Modifier.padding(TamyrTheme.spacing.screen),
    )
}

@Preview(name = "Start of a module, lesson restarts")
@Composable
private fun RoadmapCurrentCardStartPreview() = PreviewFrame {
    RoadmapCurrentCard(
        entry = previewEntry(done = 0),
        current = previewCurrent(done = 0, restarts = true),
        totalModules = 9,
        onStart = {},
        modifier = Modifier.padding(TamyrTheme.spacing.screen),
    )
}
