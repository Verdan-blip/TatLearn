package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.tatarcha.tatlearn.core.audio.AudioPlayer
import ru.tatarcha.tatlearn.core.audio.AudioPlayerState
import ru.tatarcha.tatlearn.core.audio.StubbingAudioPlayer
import ru.tatarcha.tatlearn.core.common.AppException
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.common.TimeProvider
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import ru.tatarcha.tatlearn.domain.exercise.XpRules
import ru.tatarcha.tatlearn.domain.model.ExerciseOutcome
import ru.tatarcha.tatlearn.domain.model.ExerciseResult
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.LessonContent
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.ListeningTask
import ru.tatarcha.tatlearn.domain.model.MatchingExercise
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.QuizQuestion
import ru.tatarcha.tatlearn.domain.model.SentenceTask
import ru.tatarcha.tatlearn.domain.model.TranslationTask
import ru.tatarcha.tatlearn.domain.model.UserStats
import ru.tatarcha.tatlearn.domain.model.WordPair
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository
import ru.tatarcha.tatlearn.navigation.LessonKey
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal fun testLessonContent() = LessonContent(
    lesson = Lesson("lesson_t", 1, "Тест", "Тест"),
    matching = MatchingExercise(listOf(WordPair("ипи", "хлеб"), WordPair("су", "вода"), WordPair("дус", "друг"))),
    quiz = listOf(
        QuizQuestion("китап", null, listOf("книга", "стол"), 0, isNewWord = true),
        QuizQuestion("өстәл", null, listOf("окно", "стол"), 1, isNewWord = false),
    ),
    listening = listOf(ListeningTask(null, listOf("Рәхмәт", "Исәнме"), 0)),
    sentences = listOf(SentenceTask("Я иду в школу", listOf("Мин", "барам", "мәктәпкә", "китап"), listOf("Мин", "мәктәпкә", "барам"))),
    translations = listOf(TranslationTask("Доброе утро", listOf("Хәерле иртә"), null)),
)

private class FakeLessons(private val result: Result<LessonContent> = Result.success(testLessonContent())) : LessonsRepository {
    override suspend fun getModules(): Result<List<CourseModule>> = error("unused")
    override suspend fun getLessonContent(lessonId: String): Result<LessonContent> = result
}

private class FakeProgress : ProgressRepository {
    val recorded = mutableListOf<ExerciseResult>()
    /** Серия была вчерашней: первый ответ за сегодня её продлевает, остальные нет. */
    var extendsStreakOnFirstAnswer = false
    var completed: Pair<String, Int>? = null
    private val stats = MutableStateFlow(UserStats.Empty)
    override fun observeStats(): Flow<UserStats> = stats
    override fun observeLessonProgress(): Flow<List<LessonProgress>> = stats.map { emptyList() }
    override suspend fun recordExerciseResult(result: ExerciseResult): ExerciseOutcome {
        val extended = extendsStreakOnFirstAnswer && recorded.isEmpty()
        recorded += result
        val xp = result.correct * XpRules.PER_CORRECT_STEP
        stats.value = UserStats(stats.value.xp + xp, 1)
        return ExerciseOutcome(xp, stats.value, extended)
    }
    override suspend fun completeLesson(lessonId: String, scorePercent: Int) { completed = lessonId to scorePercent }
    override suspend fun resetLessonProgress() = Unit
    override suspend fun resetStats() = Unit
}

private class SilentPlayer : AudioPlayer {
    override val state: StateFlow<AudioPlayerState> = MutableStateFlow(AudioPlayerState.Idle)
    override fun play(url: String, rate: Float) = Unit
    override fun stop() = Unit
    override fun release() = Unit
}

private class FixedTime : TimeProvider {
    var now = 1_000L
    override fun nowMillis() = now
}

@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewModelTest {
    private val progress = FakeProgress()
    private val time = FixedTime()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(stepIds: List<String> = emptyList(), lessons: LessonsRepository = FakeLessons()) =
        LessonViewModel(LessonKey("lesson_t", stepIds), lessons, progress, StubbingAudioPlayer(SilentPlayer()), time)

    private fun LessonViewModel.step() = state.value.step

    @Test fun loadsAndPlansAllSteps() = runTest {
        val vm = viewModel()
        // Аудирование без audioUrl не планируется (MVP), поэтому 5 шагов, а не 6.
        assertEquals(5, vm.state.value.steps.size)
        assertIs<LoadState.Content<Unit>>(vm.state.value.load)
        assertIs<ExerciseStep.Matching>(vm.step())
    }

    @Test fun loadFailureShowsErrorCode() = runTest {
        val vm = viewModel(lessons = FakeLessons(Result.failure(AppException("NET_TIMEOUT"))))
        assertEquals(LoadState.Error("NET_TIMEOUT"), vm.state.value.load)
    }

    @Test fun correctQuizAnswerAwardsXpAndMovesOn() = runTest {
        val vm = viewModel(listOf("quiz-0", "quiz-1"))
        assertTrue(vm.state.value.canCheckViaSelection().not())
        vm.onEvent(LessonUiEvent.OptionSelected(0))
        vm.onEvent(LessonUiEvent.CheckClicked)
        val feedback = vm.state.value.feedback!!
        assertTrue(feedback.isCorrect)
        assertEquals("китап — книга", feedback.detail)
        assertEquals(XpRules.PER_CORRECT_STEP, feedback.xpGained)
        vm.onEvent(LessonUiEvent.ContinueClicked)
        assertEquals(1, vm.state.value.stepIndex)
        assertNull(vm.state.value.feedback)
    }

    @Test fun wrongAnswerRevealsAnswer() = runTest {
        val vm = viewModel(listOf("quiz-0"))
        vm.onEvent(LessonUiEvent.OptionSelected(1))
        vm.onEvent(LessonUiEvent.CheckClicked)
        val feedback = vm.state.value.feedback!!
        assertEquals(false, feedback.isCorrect)
        assertEquals("книга", feedback.correctAnswer)
        assertEquals(0, progress.recorded.single().correct)
    }

    @Test fun optionCannotChangeAfterCheck() = runTest {
        val vm = viewModel(listOf("quiz-0"))
        vm.onEvent(LessonUiEvent.OptionSelected(0))
        vm.onEvent(LessonUiEvent.CheckClicked)
        vm.onEvent(LessonUiEvent.OptionSelected(1))
        assertEquals(0, (vm.state.value.input as StepInput.Choice).selected)
    }

    @Test fun resultKnowsWhetherThisLessonExtendedTheStreak() = runTest {
        progress.extendsStreakOnFirstAnswer = true
        val vm = viewModel(listOf("quiz-0", "quiz-1"))
        vm.onEvent(LessonUiEvent.OptionSelected(0)); vm.onEvent(LessonUiEvent.CheckClicked); vm.onEvent(LessonUiEvent.ContinueClicked)
        vm.onEvent(LessonUiEvent.OptionSelected(1)); vm.onEvent(LessonUiEvent.CheckClicked); vm.onEvent(LessonUiEvent.ContinueClicked)
        assertTrue((vm.effects.first() as LessonUiEffect.NavigateToResult).key.streakExtended)
    }

    @Test fun resultHidesStreakExtendedWhenItWasAlreadyActiveToday() = runTest {
        val vm = viewModel(listOf("quiz-0", "quiz-1"))
        vm.onEvent(LessonUiEvent.OptionSelected(0)); vm.onEvent(LessonUiEvent.CheckClicked); vm.onEvent(LessonUiEvent.ContinueClicked)
        vm.onEvent(LessonUiEvent.OptionSelected(1)); vm.onEvent(LessonUiEvent.CheckClicked); vm.onEvent(LessonUiEvent.ContinueClicked)
        assertEquals(false, (vm.effects.first() as LessonUiEffect.NavigateToResult).key.streakExtended)
    }

    @Test fun finishingLessonSendsResultWithMistakes() = runTest {
        val vm = viewModel(listOf("quiz-0", "quiz-1"))
        vm.onEvent(LessonUiEvent.OptionSelected(0)); vm.onEvent(LessonUiEvent.CheckClicked); vm.onEvent(LessonUiEvent.ContinueClicked)
        vm.onEvent(LessonUiEvent.OptionSelected(0)); vm.onEvent(LessonUiEvent.CheckClicked) // неверно, правильный индекс 1
        time.now += 65_000
        vm.onEvent(LessonUiEvent.ContinueClicked)
        val result = (vm.effects.first() as LessonUiEffect.NavigateToResult).key
        assertEquals(XpRules.PER_CORRECT_STEP, result.xpEarned)
        assertEquals(50, result.accuracyPercent)
        assertEquals(65, result.durationSeconds)
        assertEquals(listOf("quiz-1"), result.mistakeStepIds)
        assertNull(progress.completed) // 50% ниже порога
    }

    @Test fun matchingFlowWithMistake() = runTest {
        val vm = viewModel(listOf("matching"))
        vm.onEvent(LessonUiEvent.MatchLeftClicked(0))
        vm.onEvent(LessonUiEvent.MatchRightClicked(1)) // неверная пара
        assertEquals(0 to 1, (vm.state.value.input as StepInput.Matching).wrongPair)
        advanceUntilIdle()
        assertNull((vm.state.value.input as StepInput.Matching).wrongPair)
        listOf(0, 1, 2).forEach { pair ->
            vm.onEvent(LessonUiEvent.MatchLeftClicked(pair))
            vm.onEvent(LessonUiEvent.MatchRightClicked(pair))
        }
        assertTrue(vm.state.value.feedback!!.isCorrect)
        assertEquals(0, progress.recorded.single().correct) // была ошибка, значит шаг не «чистый»
    }

    @Test fun sentenceNeedsWholeAnswerInOrder() = runTest {
        val vm = viewModel(listOf("sentence-0"))
        vm.onEvent(LessonUiEvent.BankWordClicked(0))
        vm.onEvent(LessonUiEvent.BankWordClicked(1)) // Мин, барам -> неверный порядок
        vm.onEvent(LessonUiEvent.BankWordClicked(2))
        vm.onEvent(LessonUiEvent.CheckClicked)
        assertEquals(false, vm.state.value.feedback!!.isCorrect)
        assertEquals("Мин мәктәпкә барам", vm.state.value.feedback!!.correctAnswer)
    }

    @Test fun sentenceChipsCanBeTakenBack() = runTest {
        val vm = viewModel(listOf("sentence-0"))
        vm.onEvent(LessonUiEvent.BankWordClicked(0))
        vm.onEvent(LessonUiEvent.BankWordClicked(2))
        vm.onEvent(LessonUiEvent.PlacedWordClicked(0))
        assertEquals(listOf(2), (vm.state.value.input as StepInput.Sentence).placed)
        vm.onEvent(LessonUiEvent.BankWordClicked(2)) // уже выставлено: игнорируется
        assertEquals(listOf(2), (vm.state.value.input as StepInput.Sentence).placed)
    }

    @Test fun translationAcceptsRussianKeyboardSpellingWithHint() = runTest {
        val vm = viewModel(listOf("translation-0"))
        vm.onEvent(LessonUiEvent.TextChanged(TextFieldValue("хаерле ирта")))
        vm.onEvent(LessonUiEvent.CheckClicked)
        val feedback = vm.state.value.feedback!!
        assertTrue(feedback.isCorrect)
        assertEquals("Хәерле иртә", feedback.spellingHint)
    }

    @Test fun letterIsInsertedAtCursor() = runTest {
        val vm = viewModel(listOf("translation-0"))
        vm.onEvent(LessonUiEvent.TextChanged(TextFieldValue("хрле", TextRange(1))))
        vm.onEvent(LessonUiEvent.LetterClicked("ә"))
        val value = (vm.state.value.input as StepInput.Translation).value
        assertEquals("хәрле", value.text)
        assertEquals(TextRange(2), value.selection)
    }

    @Test fun mistakesNeverEndTheLessonEarly() = runTest {
        val vm = viewModel(listOf("quiz-0", "quiz-1", "sentence-0"))
        listOf(1, 0).forEach { pick ->
            vm.onEvent(LessonUiEvent.OptionSelected(pick))
            vm.onEvent(LessonUiEvent.CheckClicked)
            vm.onEvent(LessonUiEvent.ContinueClicked)
        }
        assertEquals(2, vm.state.value.stepIndex)
        assertIs<ExerciseStep.SentenceBuilder>(vm.step())
    }

    @Test fun mistakesRunIsCompletedWhenAllFixed() = runTest {
        val vm = viewModel(listOf("quiz-0", "quiz-1"))
        vm.onEvent(LessonUiEvent.OptionSelected(0)); vm.onEvent(LessonUiEvent.CheckClicked); vm.onEvent(LessonUiEvent.ContinueClicked)
        vm.onEvent(LessonUiEvent.OptionSelected(1)); vm.onEvent(LessonUiEvent.CheckClicked); vm.onEvent(LessonUiEvent.ContinueClicked)
        val result = (vm.effects.first() as LessonUiEffect.NavigateToResult).key
        assertEquals(100, result.accuracyPercent)
        assertTrue(result.isMistakesRun)
        assertEquals(2, result.correctAnswers)
        assertEquals(2, result.totalAnswers)
        // Повтор ошибок засчитывается как порог, а не как 100%, чтобы не завышать лучший результат.
        assertEquals("lesson_t" to XpRules.PASS_THRESHOLD_PERCENT, progress.completed)
    }

    @Test fun exitNeedsConfirmation() = runTest {
        val vm = viewModel()
        vm.onEvent(LessonUiEvent.CloseClicked)
        assertTrue(vm.state.value.showExitDialog)
        vm.onEvent(LessonUiEvent.ExitDismissed)
        assertEquals(false, vm.state.value.showExitDialog)
        vm.onEvent(LessonUiEvent.CloseClicked)
        vm.onEvent(LessonUiEvent.ExitConfirmed)
        assertIs<LessonUiEffect.Exit>(vm.effects.first())
    }

    private fun LessonUiState.canCheckViaSelection() = (input as? StepInput.Choice)?.selected != null
}
