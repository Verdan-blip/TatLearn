package ru.tatarcha.tatlearn.feature.module

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.designsystem.component.LoadStateContent
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrConfirmSheet
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.LessonRowState
import ru.tatarcha.tatlearn.domain.model.ModuleDetails
import ru.tatarcha.tatlearn.domain.model.ModuleLessonRow
import ru.tatarcha.tatlearn.domain.model.RequiredModule
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.module_sheet_confirm
import tatlearn.shared.generated.resources.module_sheet_dismiss
import tatlearn.shared.generated.resources.module_sheet_message
import tatlearn.shared.generated.resources.module_sheet_title

/**
 * Экраны 18–23: баннер модуля, лента уроков со статусами из локального прогресса, лист закрытого урока.
 * Загрузка и ошибка показывают только «назад» (ошибка это общее состояние, экран 08).
 */
@Composable
fun ModuleContent(
    state: ModuleUiState,
    onEvent: (ModuleUiEvent) -> Unit,
) {
    val onBack = { onEvent(ModuleUiEvent.BackClicked) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TamyrTheme.colors.background),
    ) {
        when (val content = state.content) {
            is LoadState.Content -> if (content.data.lessons.isEmpty()) {
                ModuleEmptyState(details = content.data, onHome = onBack)
            } else {
                ModuleLoaded(details = content.data, onEvent = onEvent)
            }
            LoadState.Loading -> ModuleSkeleton(onBack = onBack)
            is LoadState.Error -> Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = TamyrTheme.spacing.m, top = TamyrTheme.spacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BackButton(onBack = onBack)
                }
                LoadStateContent(
                    state = state.content,
                    onRetry = { onEvent(ModuleUiEvent.RetryClicked) },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {}
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

@Preview(name = "Module", group = PREVIEW_GROUP)
@Composable
private fun ModuleMainPreview() = ModulePreview(
    ModuleUiState(LoadState.Content(previewDetails(previewRows(LessonRowState.Done, LessonRowState.Done, LessonRowState.Next, LessonRowState.Available)))),
)

@Preview(name = "Module locked", group = PREVIEW_GROUP)
@Composable
private fun ModuleLockedPreview() = ModulePreview(
    ModuleUiState(
        LoadState.Content(
            previewDetails(previewRows(LessonRowState.Locked, LessonRowState.Locked), lockedBy = RequiredModule("alphabet", "Алфавит", "Алфавит", 1, 2)),
        ),
    ),
)

@Preview(name = "Module locked · sheet", group = PREVIEW_GROUP)
@Composable
private fun ModuleLockedSheetPreview() {
    val required = RequiredModule("alphabet", "Алфавит", "Алфавит", 1, 2)
    ModulePreview(
        ModuleUiState(LoadState.Content(previewDetails(previewRows(LessonRowState.Locked, LessonRowState.Locked), lockedBy = required)), lockedSheet = required),
    )
}

@Preview(name = "Module done", group = PREVIEW_GROUP)
@Composable
private fun ModuleDonePreview() = ModulePreview(
    ModuleUiState(LoadState.Content(previewDetails(previewRows(LessonRowState.Done, LessonRowState.Done, LessonRowState.Done, LessonRowState.Done)))),
)

@Preview(name = "Module loading", group = PREVIEW_GROUP)
@Composable
private fun ModuleLoadingPreview() = ModulePreview(ModuleUiState())

@Preview(name = "Module error", group = PREVIEW_GROUP)
@Composable
private fun ModuleErrorPreview() = ModulePreview(ModuleUiState(LoadState.Error("NET_TIMEOUT")))

@Preview(name = "Module empty", group = PREVIEW_GROUP)
@Composable
private fun ModuleEmptyPreview() = ModulePreview(ModuleUiState(LoadState.Content(previewDetails(emptyList()))))

@Preview(name = "Module · long titles", group = PREVIEW_GROUP)
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
