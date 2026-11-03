package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.audio.StubbingAudioPlayer
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.common.MviViewModel
import ru.tatarcha.tatlearn.core.common.TimeProvider
import ru.tatarcha.tatlearn.core.haptics.Haptics
import ru.tatarcha.tatlearn.core.sound.SoundEffects
import ru.tatarcha.tatlearn.core.common.errorCode
import ru.tatarcha.tatlearn.domain.exercise.AnswerNormalizer
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import ru.tatarcha.tatlearn.domain.exercise.LessonPlanner
import ru.tatarcha.tatlearn.domain.exercise.PassRules
import ru.tatarcha.tatlearn.domain.exercise.XpRules
import ru.tatarcha.tatlearn.domain.model.ExerciseResult
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository
import ru.tatarcha.tatlearn.navigation.LessonKey
import ru.tatarcha.tatlearn.navigation.ResultKey

/** Ведёт весь урок: загружает контент, проходит шаги, проверяет ответы, сохраняет прогресс, завершается в Result. */
class LessonViewModel(
    private val key: LessonKey,
    private val lessons: LessonsRepository,
    private val progress: ProgressRepository,
    private val player: StubbingAudioPlayer,
    private val time: TimeProvider,
    private val haptics: Haptics,
    private val sounds: SoundEffects,
) : MviViewModel<LessonUiState, LessonUiEvent, LessonUiEffect>(LessonUiState()) {

    private var startedAtMillis = 0L
    private var xpEarned = 0
    private var streakExtended = false
    private var correctSteps = 0
    private var checkedSteps = 0
    private val mistakeIds = mutableListOf<String>()

    init {
        player.state.onEach { audio -> setState { copy(audio = audio) } }.launchIn(viewModelScope)
        load()
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }

    override fun onEvent(event: LessonUiEvent) {
        when (event) {
            LessonUiEvent.RetryClicked -> load()
            is LessonUiEvent.OptionSelected -> if (state.value.feedback == null) setState { copy(input = StepInput.Choice(event.index)) }
            is LessonUiEvent.MatchLeftClicked -> onMatchLeft(event.pairIndex)
            is LessonUiEvent.MatchRightClicked -> onMatchRight(event.pairIndex)
            is LessonUiEvent.BankWordClicked -> onBankWord(event.bankIndex)
            is LessonUiEvent.PlacedWordClicked -> onPlacedWord(event.position)
            is LessonUiEvent.TextChanged -> if (state.value.feedback == null) setState { copy(input = StepInput.Translation(event.value)) }
            is LessonUiEvent.LetterClicked -> onLetter(event.letter)
            is LessonUiEvent.PlayAudioClicked -> playAudio(event.slow)
            LessonUiEvent.CheckClicked -> check()
            LessonUiEvent.ContinueClicked -> next()
            LessonUiEvent.CloseClicked -> setState { copy(showExitDialog = true) }
            LessonUiEvent.ExitDismissed -> setState { copy(showExitDialog = false) }
            LessonUiEvent.ExitConfirmed -> {
                player.stop()
                setState { copy(showExitDialog = false) }
                sendEffect(LessonUiEffect.Exit)
            }
        }
    }

    // region загрузка

    private fun load() {
        viewModelScope.launch {
            setState { copy(load = LoadState.Loading) }
            lessons.getLessonContent(key.lessonId).fold(
                onSuccess = { content ->
                    val steps = LessonPlanner.plan(content, key.stepIds.toSet())
                    if (steps.isEmpty()) {
                        setState { copy(load = LoadState.Error(CODE_EMPTY_LESSON)) }
                    } else {
                        startedAtMillis = time.nowMillis()
                        setState { copy(load = LoadState.Content(Unit), steps = steps, stepIndex = 0, input = initialInput(steps.first()), feedback = null) }
                    }
                },
                onFailure = { error -> setState { copy(load = LoadState.Error(error.errorCode())) } },
            )
        }
    }

    private fun initialInput(step: ExerciseStep): StepInput = when (step) {
        is ExerciseStep.Matching -> StepInput.Matching()
        is ExerciseStep.Quiz, is ExerciseStep.Listening -> StepInput.Choice()
        is ExerciseStep.SentenceBuilder -> StepInput.Sentence()
        is ExerciseStep.Translation -> StepInput.Translation()
    }

    // endregion

    // region сопоставление

    private fun onMatchLeft(pairIndex: Int) {
        val current = state.value
        val input = current.input as? StepInput.Matching ?: return
        if (current.feedback != null || pairIndex in input.matched || input.wrongPair != null) return
        val selecting = input.leftSelected != pairIndex
        setState { copy(input = input.copy(leftSelected = if (selecting) pairIndex else null)) }
        // Слово звучит при выборе татарской карточки, чтобы произношение слышали вместе со значением. Снятие выбора и карточка без
        // записи молчат (заглушку звука не изображаем); новое слово обрывает предыдущее.
        if (selecting) (current.step as? ExerciseStep.Matching)?.pairs?.getOrNull(pairIndex)?.audioUrl?.let { player.play(it) }
    }

    private fun onMatchRight(pairIndex: Int) {
        val current = state.value
        val input = current.input as? StepInput.Matching ?: return
        val step = current.step as? ExerciseStep.Matching ?: return
        val left = input.leftSelected ?: return
        if (current.feedback != null || pairIndex in input.matched || input.wrongPair != null) return

        if (left == pairIndex) {
            val matched = input.matched + pairIndex
            setState { copy(input = input.copy(leftSelected = null, matched = matched)) }
            if (matched.size == step.pairs.size) finishStep(correct = input.mistakes == 0, feedback = StepFeedback(isCorrect = true))
        } else {
            val mistakes = input.mistakes + 1
            setState { copy(input = input.copy(leftSelected = null, wrongPair = left to pairIndex, mistakes = mistakes)) }
            viewModelScope.launch {
                delay(WRONG_FLASH_MS)
                setState { copy(input = (this.input as? StepInput.Matching)?.copy(wrongPair = null) ?: this.input) }
            }
        }
    }

    // endregion

    // region сборка предложения

    private fun onBankWord(bankIndex: Int) {
        val input = state.value.input as? StepInput.Sentence ?: return
        if (state.value.feedback != null || bankIndex in input.placed) return
        setState { copy(input = input.copy(placed = input.placed + bankIndex)) }
        // Слово из банка звучит при тапе; возврат слова из ответа молчит, иначе будет шум. Слова без записи молчат.
        (state.value.step as? ExerciseStep.SentenceBuilder)?.task?.let { task ->
            task.wordBank.getOrNull(bankIndex)?.let { task.wordAudio[it] }?.let { player.play(it) }
        }
    }

    private fun onPlacedWord(position: Int) {
        val input = state.value.input as? StepInput.Sentence ?: return
        if (state.value.feedback != null) return
        setState { copy(input = input.copy(placed = input.placed.filterIndexed { i, _ -> i != position })) }
    }

    // endregion

    private fun onLetter(letter: String) {
        val input = state.value.input as? StepInput.Translation ?: return
        if (state.value.feedback != null) return
        val value = input.value
        val start = value.selection.min
        val end = value.selection.max
        val text = value.text.replaceRange(start, end, letter)
        setState { copy(input = StepInput.Translation(TextFieldValue(text, TextRange(start + letter.length)))) }
    }

    private fun playAudio(slow: Boolean) {
        val url = when (val step = state.value.step) {
            is ExerciseStep.Listening -> step.task.audioUrl
            is ExerciseStep.Translation -> step.task.audioUrl
            is ExerciseStep.Quiz -> step.question.audioUrl
            else -> null
        }
        player.play(url, if (slow) SLOW_RATE else 1f)
    }

    // region проверка

    private fun check() {
        val current = state.value
        if (current.feedback != null) return
        val step = current.step ?: return
        val input = current.input
        when {
            step is ExerciseStep.Quiz && input is StepInput.Choice -> {
                val picked = input.selected ?: return
                val q = step.question
                val answer = q.options[q.correctIndex]
                finishStep(picked == q.correctIndex, if (picked == q.correctIndex) StepFeedback(true, detail = "${q.word} — $answer") else StepFeedback(false, correctAnswer = answer, explanation = q.explanation))
            }
            step is ExerciseStep.Listening && input is StepInput.Choice -> {
                val picked = input.selected ?: return
                val answer = step.task.options[step.task.correctIndex]
                finishStep(picked == step.task.correctIndex, if (picked == step.task.correctIndex) StepFeedback(true, detail = answer) else StepFeedback(false, correctAnswer = answer, explanation = step.task.explanation))
            }
            step is ExerciseStep.SentenceBuilder && input is StepInput.Sentence -> {
                val words = input.placed.map { step.task.wordBank[it] }
                val correct = words == step.task.answer
                val answer = step.task.answer.joinToString(" ")
                finishStep(correct, if (correct) StepFeedback(true, detail = answer) else StepFeedback(false, correctAnswer = answer, explanation = step.task.explanation))
            }
            step is ExerciseStep.Translation && input is StepInput.Translation -> {
                val expected = step.task.acceptedAnswers.first()
                when (AnswerNormalizer.check(input.value.text, step.task.acceptedAnswers)) {
                    AnswerNormalizer.Verdict.Exact -> finishStep(true, StepFeedback(true, detail = expected))
                    AnswerNormalizer.Verdict.Approximate -> finishStep(true, StepFeedback(true, spellingHint = expected))
                    AnswerNormalizer.Verdict.Wrong -> finishStep(false, StepFeedback(false, correctAnswer = expected, explanation = step.task.explanation))
                }
            }
        }
    }

    private fun finishStep(correct: Boolean, feedback: StepFeedback) {
        val step = state.value.step ?: return
        checkedSteps++
        if (correct) correctSteps++ else mistakeIds += step.id
        player.stop()
        // Вибро и звук следуют за панелью ответа (feedback.isCorrect), а не за учётом точности (correct): в сопоставлении
        // шаг с ошибкой портит точность, но панель показывает успех, и отклик не должен ей противоречить.
        // Вибро на итог шага (на отдельные пары его нет). Звук после player.stop(): запись слова уже оборвана,
        // наложения нет. На тапы и пары звуков нет.
        if (feedback.isCorrect) haptics.correct() else haptics.wrong()
        if (feedback.isCorrect) sounds.correct() else sounds.wrong()
        viewModelScope.launch {
            val outcome = progress.recordExerciseResult(ExerciseResult(key.lessonId, step.type, if (correct) 1 else 0, 1))
            xpEarned += outcome.xpEarned
            if (outcome.streakExtended) streakExtended = true
            setState { copy(feedback = feedback.copy(xpGained = outcome.xpEarned)) }
        }
    }

    private fun next() {
        val current = state.value
        if (current.feedback == null) return
        val lastStep = current.stepIndex >= current.steps.lastIndex
        if (lastStep) {
            finishLesson()
        } else {
            val nextStep = current.steps[current.stepIndex + 1]
            setState { copy(stepIndex = stepIndex + 1, input = initialInput(nextStep), feedback = null) }
        }
    }

    private fun finishLesson() {
        viewModelScope.launch {
            val accuracy = if (checkedSteps == 0) 0 else correctSteps * 100 / checkedSteps
            val isMistakesRun = key.stepIds.isNotEmpty()
            if (PassRules.isPassed(accuracy, isMistakesRun, mistakeIds.size)) {
                progress.completeLesson(key.lessonId, if (isMistakesRun) XpRules.PASS_THRESHOLD_PERCENT else accuracy)
            }
            val seconds = ((time.nowMillis() - startedAtMillis) / MILLIS_PER_SECOND).toInt().coerceAtLeast(0)
            sendEffect(LessonUiEffect.NavigateToResult(ResultKey(key.lessonId, xpEarned, accuracy, seconds, mistakeIds.toList(), correctSteps, checkedSteps, isMistakesRun, streakExtended)))
        }
    }

    // endregion

    private companion object {
        const val WRONG_FLASH_MS = 700L
        const val SLOW_RATE = 0.75f
        const val MILLIS_PER_SECOND = 1000
        const val CODE_EMPTY_LESSON = "NO_EXERCISES"
    }
}
