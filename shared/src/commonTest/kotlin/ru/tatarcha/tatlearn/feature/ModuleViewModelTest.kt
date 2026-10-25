package ru.tatarcha.tatlearn.feature

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.tatarcha.tatlearn.core.common.AppException
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.ExerciseOutcome
import ru.tatarcha.tatlearn.domain.model.ExerciseResult
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.LessonContent
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.LessonRowState
import ru.tatarcha.tatlearn.domain.model.LessonStatus
import ru.tatarcha.tatlearn.domain.model.UserStats
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository
import ru.tatarcha.tatlearn.feature.module.ModuleUiEffect
import ru.tatarcha.tatlearn.feature.module.ModuleUiEvent
import ru.tatarcha.tatlearn.feature.module.ModuleViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

private class ModuleLessons(var result: Result<List<CourseModule>>) : LessonsRepository {
    var calls = 0
    override suspend fun getModules(): Result<List<CourseModule>> { calls++; return result }
    override suspend fun getLessonContent(lessonId: String): Result<LessonContent> = error("unused")
}

private class ModuleProgress : ProgressRepository {
    val lessonProgress = MutableStateFlow<List<LessonProgress>>(emptyList())
    override fun observeStats(): Flow<UserStats> = MutableStateFlow(UserStats.Empty)
    override fun observeLessonProgress(): Flow<List<LessonProgress>> = lessonProgress
    override suspend fun recordExerciseResult(result: ExerciseResult): ExerciseOutcome = error("unused")
    override suspend fun completeLesson(lessonId: String, scorePercent: Int) = Unit
    override suspend fun resetLessonProgress() = Unit
    override suspend fun resetStats() = Unit
}

private val alphabetModule = CourseModule("alphabet", "Алфавит", "Алфавит", "alphabet", listOf(Lesson("l1", 1, "А", "А"), Lesson("l2", 2, "Б", "Б")))
private val foodModule2 = CourseModule("food", "Ашлар", "Еда", "food", listOf(Lesson("l3", 3, "Ашлар", "Еда")))
private fun moduleCatalog() = listOf(alphabetModule, foodModule2)

@OptIn(ExperimentalCoroutinesApi::class)
class ModuleViewModelTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun loadsModuleAndFollowsProgress() = runTest {
        val progress = ModuleProgress()
        val vm = ModuleViewModel("alphabet", ModuleLessons(Result.success(moduleCatalog())), progress)
        val first = (vm.state.value.content as LoadState.Content).data
        assertEquals(LessonRowState.Next, first.lessons[0].state)
        progress.lessonProgress.value = listOf(LessonProgress("l1", LessonStatus.Completed, 88))
        val second = (vm.state.value.content as LoadState.Content).data
        assertEquals(LessonRowState.Done, second.lessons[0].state)
        assertEquals(88, second.lessons[0].bestScore)
    }

    @Test fun errorThenRetrySucceedsAndUnknownModuleIsNotFound() = runTest {
        val lessons = ModuleLessons(Result.failure(AppException("NET_TIMEOUT")))
        val vm = ModuleViewModel("alphabet", lessons, ModuleProgress())
        assertEquals(LoadState.Error("NET_TIMEOUT"), vm.state.value.content)
        lessons.result = Result.success(moduleCatalog())
        vm.onEvent(ModuleUiEvent.RetryClicked)
        assertIs<LoadState.Content<*>>(vm.state.value.content)
        assertEquals(2, lessons.calls)
        val missing = ModuleViewModel("zzz", lessons, ModuleProgress())
        assertEquals(LoadState.Error("NOT_FOUND"), missing.state.value.content)
    }

    @Test fun lessonAndContinueStartLessons() = runTest {
        val vm = ModuleViewModel("alphabet", ModuleLessons(Result.success(moduleCatalog())), ModuleProgress())
        vm.onEvent(ModuleUiEvent.LessonClicked("l2"))
        assertEquals(ModuleUiEffect.NavigateToLesson("l2"), vm.effects.first())
        vm.onEvent(ModuleUiEvent.ContinueClicked)
        assertEquals(ModuleUiEffect.NavigateToLesson("l1"), vm.effects.first())
    }

    @Test fun lockedLessonOpensSheetAndSheetLeadsToRequiredModule() = runTest {
        val vm = ModuleViewModel("food", ModuleLessons(Result.success(moduleCatalog())), ModuleProgress())
        vm.onEvent(ModuleUiEvent.LessonClicked("l3"))
        assertEquals("alphabet", vm.state.value.lockedSheet?.id)
        vm.onEvent(ModuleUiEvent.LockedSheetDismissed)
        assertNull(vm.state.value.lockedSheet)
        vm.onEvent(ModuleUiEvent.LessonClicked("l3"))
        vm.onEvent(ModuleUiEvent.LockedSheetConfirmed)
        assertEquals(ModuleUiEffect.NavigateToModule("alphabet"), vm.effects.first())
        assertNull(vm.state.value.lockedSheet)
    }

    @Test fun completedModuleLeadsToNextModuleAndBackNavigates() = runTest {
        val progress = ModuleProgress()
        progress.lessonProgress.value = listOf(LessonProgress("l1", LessonStatus.Completed, 90), LessonProgress("l2", LessonStatus.Completed, 95))
        val vm = ModuleViewModel("alphabet", ModuleLessons(Result.success(moduleCatalog())), progress)
        vm.onEvent(ModuleUiEvent.ContinueClicked)
        vm.onEvent(ModuleUiEvent.NextModuleClicked)
        assertEquals(ModuleUiEffect.NavigateToModule("food"), vm.effects.first())
        vm.onEvent(ModuleUiEvent.BackClicked)
        assertEquals(ModuleUiEffect.NavigateBack, vm.effects.first())
    }
}
