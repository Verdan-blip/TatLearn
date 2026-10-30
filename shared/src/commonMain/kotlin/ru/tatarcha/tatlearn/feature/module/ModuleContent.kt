package ru.tatarcha.tatlearn.feature.module

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.designsystem.component.LoadStateContent
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.SkeletonBlock
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrConfirmSheet
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrProgressBar
import ru.tatarcha.tatlearn.core.designsystem.component.icon.CheckIcon
import ru.tatarcha.tatlearn.core.designsystem.component.icon.ChevronBackIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.LessonRowState
import ru.tatarcha.tatlearn.domain.model.moduleTitle
import ru.tatarcha.tatlearn.domain.model.ModuleDetails
import ru.tatarcha.tatlearn.domain.model.ModuleLessonRow
import ru.tatarcha.tatlearn.domain.model.RequiredModule
import ru.tatarcha.tatlearn.feature.home.LockIcon
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.home_lesson_button
import tatlearn.shared.generated.resources.module_back_description
import tatlearn.shared.generated.resources.module_continue
import tatlearn.shared.generated.resources.module_done_subtitle
import tatlearn.shared.generated.resources.module_done_title
import tatlearn.shared.generated.resources.module_empty_home
import tatlearn.shared.generated.resources.module_empty_message
import tatlearn.shared.generated.resources.module_empty_title
import tatlearn.shared.generated.resources.module_lesson_score
import tatlearn.shared.generated.resources.module_locked_hint
import tatlearn.shared.generated.resources.module_locked_title
import tatlearn.shared.generated.resources.module_next_module
import tatlearn.shared.generated.resources.module_progress_label
import tatlearn.shared.generated.resources.module_progress_value
import tatlearn.shared.generated.resources.module_sheet_confirm
import tatlearn.shared.generated.resources.module_sheet_dismiss
import tatlearn.shared.generated.resources.module_sheet_message
import tatlearn.shared.generated.resources.module_sheet_title
import tatlearn.shared.generated.resources.module_status_done
import tatlearn.shared.generated.resources.module_status_locked
import tatlearn.shared.generated.resources.module_status_next

/** Экраны 18–23: уроки модуля со статусами из локального прогресса; ошибка загрузки — общее состояние (экран 08). */
@Composable
fun ModuleContent(
    state: ModuleUiState,
    onEvent: (ModuleUiEvent) -> Unit,
) {
    val details = (state.content as? LoadState.Content)?.data
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(TamyrTheme.colors.background)
                .statusBarsPadding(),
        ) {
            ModuleTopBar(title = details?.module?.let(::moduleTitle).orEmpty(), onBack = { onEvent(ModuleUiEvent.BackClicked) })
            LoadStateContent(
                state = state.content,
                onRetry = { onEvent(ModuleUiEvent.RetryClicked) },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                loading = { ModuleSkeleton() },
            ) { data ->
                if (data.lessons.isEmpty()) {
                    EmptyModule(onHome = { onEvent(ModuleUiEvent.BackClicked) })
                } else {
                    ModuleLoaded(data, onEvent)
                }
            }
        }
        val required = state.lockedSheet
        TamyrConfirmSheet(
            visible = required != null,
            title = stringResource(Res.string.module_sheet_title),
            message = required?.let {
                stringResource(Res.string.module_sheet_message, it.titleRu, it.completedLessons, it.totalLessons)
            }.orEmpty(),
            confirmText = required?.let { stringResource(Res.string.module_sheet_confirm, it.titleRu) }.orEmpty(),
            dismissText = stringResource(Res.string.module_sheet_dismiss),
            onConfirm = { onEvent(ModuleUiEvent.LockedSheetConfirmed) },
            onDismiss = { onEvent(ModuleUiEvent.LockedSheetDismissed) },
        )
    }
}

private fun moduleTitle(module: CourseModule) =
    if (module.titleTt == module.titleRu) module.titleTt else "${module.titleTt} · ${module.titleRu}"

@Composable
private fun ModuleTopBar(title: String, onBack: () -> Unit) {
    val description = stringResource(Res.string.module_back_description)
    Row(
        Modifier.fillMaxWidth().padding(start = TamyrTheme.spacing.m, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs),
    ) {
        Box(
            Modifier
                .size(TamyrSizes.topBarAction)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) { ChevronBackIcon() }
        Text(
            text = title,
            style = TamyrTheme.typography.title22,
            color = TamyrTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun ModuleLoaded(details: ModuleDetails, onEvent: (ModuleUiEvent) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TamyrTheme.spacing.screen, vertical = TamyrTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
        ) {
            when {
                details.isLocked -> LockedBanner(details)
                details.isCompleted -> DoneBanner(details)
                else -> ProgressCard(details)
            }
            details.lessons.forEach { row ->
                LessonRow(row = row, onClick = { onEvent(ModuleUiEvent.LessonClicked(row.lesson.id)) })
            }
        }
        ModuleFooter(details, onEvent)
    }
}

@Composable
private fun ProgressCard(details: ModuleDetails) {
    TamyrCard(Modifier.fillMaxWidth(), shadowColor = TamyrTheme.colors.border) {
        Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.module_progress_label), style = TamyrTheme.typography.body14, color = TamyrTheme.colors.textSecondary)
                Text(
                    stringResource(Res.string.module_progress_value, details.completedLessons, details.lessons.size),
                    style = TamyrTheme.typography.body14Bold,
                    color = TamyrTheme.colors.textPrimary,
                )
            }
            TamyrProgressBar(progress = details.progress)
        }
    }
}

@Composable
private fun LockedBanner(details: ModuleDetails) {
    val colors = TamyrTheme.colors
    val required = details.lockedBy ?: return
    TamyrCard(
        Modifier.fillMaxWidth(),
        containerColor = colors.goldContainer,
        borderColor = colors.gold,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m), verticalAlignment = Alignment.CenterVertically) {
            LockIcon(colors.goldText)
            Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
                Text(stringResource(Res.string.module_locked_title), style = TamyrTheme.typography.body16Bold, color = colors.goldText)
                Text(
                    stringResource(Res.string.module_locked_hint, moduleTitle(required.titleTt, required.titleRu)),
                    style = TamyrTheme.typography.body14,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun DoneBanner(details: ModuleDetails) {
    val colors = TamyrTheme.colors
    TamyrCard(Modifier.fillMaxWidth(), containerColor = colors.successContainer, borderColor = colors.successBorder) {
        Row(horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(DoneBadgeSize).clip(CircleShape).background(colors.primary), contentAlignment = Alignment.Center) {
                CheckIcon(size = 20.dp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
                Text(stringResource(Res.string.module_done_title), style = TamyrTheme.typography.body16Bold, color = colors.textPrimary)
                Text(
                    stringResource(Res.string.module_done_subtitle, details.completedLessons, details.lessons.size),
                    style = TamyrTheme.typography.body14,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

private val DoneBadgeSize = 32.dp

@Composable
private fun LessonRow(row: ModuleLessonRow, onClick: () -> Unit) {
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
    TamyrCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TamyrSizes.lessonRowMinHeight)
            .semantics(mergeDescendants = true) { statusText?.let { stateDescription = it } },
        onClick = onClick,
        containerColor = if (isLocked) colors.background else colors.surface,
        borderColor = if (isNext) colors.primary else colors.border,
        borderWidth = if (isNext) TamyrSizes.borderAccent else TamyrSizes.borderThin,
        shadowColor = if (isNext) colors.successBorder else colors.border,
        contentPadding = TamyrTheme.spacing.m,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m + 2.dp), verticalAlignment = Alignment.CenterVertically) {
            LessonStatusMark(row)
            Column(Modifier.weight(1f)) {
                Text(title, style = TamyrTheme.typography.body16Bold, color = colors.textPrimary)
                Text(row.lesson.titleRu, style = TamyrTheme.typography.body14, color = colors.textSecondary)
            }
            if (row.state == LessonRowState.Done && row.bestScore != null) {
                Text(
                    stringResource(Res.string.module_lesson_score, row.bestScore),
                    style = TamyrTheme.typography.body14Bold,
                    color = colors.primary,
                )
            }
            if (!isLocked) {
                ChevronBackIcon(Modifier.rotate(180f), size = 20.dp, tint = if (isNext) colors.primary else colors.textSecondary)
            }
        }
    }
}

@Composable
private fun LessonStatusMark(row: ModuleLessonRow) {
    val colors = TamyrTheme.colors
    val background = when (row.state) {
        LessonRowState.Done -> colors.primary
        LessonRowState.Next -> colors.gold
        LessonRowState.Available, LessonRowState.Locked -> colors.surfaceMuted
    }
    Box(Modifier.size(TamyrSizes.lessonStatus).clip(CircleShape).background(background), contentAlignment = Alignment.Center) {
        when (row.state) {
            LessonRowState.Done -> CheckIcon(size = TamyrSizes.checkIcon)
            LessonRowState.Locked -> LockIcon(colors.textSecondary)
            LessonRowState.Next -> Text(row.lesson.number.toString(), style = TamyrTheme.typography.body17Bold, color = colors.textPrimary)
            LessonRowState.Available -> Text(row.lesson.number.toString(), style = TamyrTheme.typography.body17Bold, color = colors.textSecondary)
        }
    }
}

/** Закреплённая кнопка внизу: «Продолжить» с рекомендуемым уроком или переход к следующему модулю, когда этот пройден. */
@Composable
private fun ModuleFooter(details: ModuleDetails, onEvent: (ModuleUiEvent) -> Unit) {
    val colors = TamyrTheme.colors
    val next = details.nextLesson
    val nextModule = details.nextModule
    val (text, event) = when {
        next != null -> stringResource(Res.string.module_continue, next.number) to ModuleUiEvent.ContinueClicked
        details.isCompleted && nextModule != null -> stringResource(Res.string.module_next_module, nextModule.titleTt) to ModuleUiEvent.NextModuleClicked
        else -> return
    }
    Column(Modifier.fillMaxWidth().background(colors.background)) {
        Box(Modifier.fillMaxWidth().height(TamyrSizes.borderThin).background(colors.border))
        Box(
            Modifier
                .navigationBarsPadding()
                .padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.m, bottom = TamyrTheme.spacing.l),
        ) {
            TamyrButton(text, onClick = { onEvent(event) })
        }
    }
}

@Composable
private fun EmptyModule(onHome: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = TamyrTheme.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TamyrMascot(MascotPose.Sleeping, 130.dp)
            Text(stringResource(Res.string.module_empty_title), style = TamyrTheme.typography.headline24, color = TamyrTheme.colors.textPrimary, textAlign = TextAlign.Center)
            Text(stringResource(Res.string.module_empty_message), style = TamyrTheme.typography.body16, color = TamyrTheme.colors.textSecondary, textAlign = TextAlign.Center)
        }
        Box(Modifier.navigationBarsPadding().padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, bottom = TamyrTheme.spacing.l)) {
            TamyrButton(stringResource(Res.string.module_empty_home), onHome)
        }
    }
}

/** Экран 21: скелет по форме списка (карточка прогресса и четыре строки). */
@Composable
private fun ModuleSkeleton() {
    Column(
        Modifier.fillMaxSize().padding(horizontal = TamyrTheme.spacing.screen, vertical = TamyrTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
    ) {
        TamyrCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m)) {
                SkeletonBlock(Modifier.width(120.dp).height(12.dp))
                SkeletonBlock(Modifier.fillMaxWidth().height(10.dp), radius = 5.dp)
            }
        }
        repeat(SKELETON_ROWS) {
            TamyrCard(Modifier.fillMaxWidth().heightIn(min = TamyrSizes.lessonRowMinHeight), contentPadding = TamyrTheme.spacing.m) {
                Row(horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m + 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    SkeletonBlock(Modifier.size(TamyrSizes.lessonStatus), radius = 20.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
                        SkeletonBlock(Modifier.width(140.dp).height(12.dp))
                        SkeletonBlock(Modifier.width(90.dp).height(10.dp), radius = 5.dp)
                    }
                }
            }
        }
    }
}

private const val SKELETON_ROWS = 4

// region Превью

private const val PREVIEW_GROUP = "Module"

private fun lesson(n: Int, tt: String, ru: String) = Lesson("lesson_$n", n, tt, ru)

private val previewModule = CourseModule(
    "food", "Ашлар", "Еда", "food",
    lessons = listOf(lesson(1, "Ашлар", "Еда, часть 1"), lesson(2, "Туганнар", "Семья"), lesson(3, "Саннар", "Числа"), lesson(4, "Табигать", "Природа")),
)

private fun previewRows(vararg states: LessonRowState) =
    previewModule.lessons.zip(states).map { (lesson, state) -> ModuleLessonRow(lesson, state, if (state == LessonRowState.Done) 90 else null) }

private fun previewDetails(rows: List<ModuleLessonRow>, lockedBy: RequiredModule? = null, completed: Int = rows.count { it.state == LessonRowState.Done }) =
    ModuleDetails(
        module = previewModule,
        lessons = rows,
        completedLessons = completed,
        lockedBy = lockedBy,
        nextLesson = rows.firstOrNull { it.state == LessonRowState.Next }?.lesson,
        nextModule = CourseModule("numbers", "Саннар", "Числа", "numbers", emptyList()),
    )

@Composable
private fun ModulePreview(state: ModuleUiState) = PreviewFrame { ModuleContent(state = state, onEvent = {}) }

@Preview(name = "Module", group = PREVIEW_GROUP, widthDp = 390, heightDp = 844)
@Composable
private fun ModuleMainPreview() = ModulePreview(
    ModuleUiState(LoadState.Content(previewDetails(previewRows(LessonRowState.Done, LessonRowState.Done, LessonRowState.Next, LessonRowState.Available)))),
)

@Preview(name = "Module locked", group = PREVIEW_GROUP, widthDp = 390, heightDp = 844)
@Composable
private fun ModuleLockedPreview() = ModulePreview(
    ModuleUiState(
        LoadState.Content(
            previewDetails(previewRows(LessonRowState.Locked, LessonRowState.Locked), lockedBy = RequiredModule("alphabet", "Алфавит", "Алфавит", 1, 2)),
        ),
    ),
)

@Preview(name = "Module locked · sheet", group = PREVIEW_GROUP, widthDp = 390, heightDp = 844)
@Composable
private fun ModuleLockedSheetPreview() {
    val required = RequiredModule("alphabet", "Алфавит", "Алфавит", 1, 2)
    ModulePreview(
        ModuleUiState(LoadState.Content(previewDetails(previewRows(LessonRowState.Locked, LessonRowState.Locked), lockedBy = required)), lockedSheet = required),
    )
}

@Preview(name = "Module done", group = PREVIEW_GROUP, widthDp = 390, heightDp = 844)
@Composable
private fun ModuleDonePreview() = ModulePreview(
    ModuleUiState(LoadState.Content(previewDetails(previewRows(LessonRowState.Done, LessonRowState.Done, LessonRowState.Done, LessonRowState.Done)))),
)

@Preview(name = "Module loading", group = PREVIEW_GROUP, widthDp = 390, heightDp = 844)
@Composable
private fun ModuleLoadingPreview() = ModulePreview(ModuleUiState())

@Preview(name = "Module error", group = PREVIEW_GROUP, widthDp = 390, heightDp = 844)
@Composable
private fun ModuleErrorPreview() = ModulePreview(ModuleUiState(LoadState.Error("NET_TIMEOUT")))

@Preview(name = "Module empty", group = PREVIEW_GROUP, widthDp = 390, heightDp = 844)
@Composable
private fun ModuleEmptyPreview() = ModulePreview(ModuleUiState(LoadState.Content(previewDetails(emptyList()))))

@Preview(name = "Module · long titles", group = PREVIEW_GROUP, widthDp = 320, heightDp = 568)
@Composable
private fun ModuleLongPreview() = ModulePreview(
    ModuleUiState(
        LoadState.Content(
            previewDetails(
                listOf(ModuleLessonRow(lesson(1, "Бик озын исемле дәрес", "Очень длинное название урока для проверки переноса"), LessonRowState.Next, null)),
            ),
        ),
    ),
)

// endregion
