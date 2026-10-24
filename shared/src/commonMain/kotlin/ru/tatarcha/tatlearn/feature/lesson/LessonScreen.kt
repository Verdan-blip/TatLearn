package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.tatarcha.tatlearn.core.audio.AudioPlayerState
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.designsystem.component.ErrorState
import ru.tatarcha.tatlearn.core.designsystem.component.LoadingState
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.AnswerFeedback
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.ExerciseFooter
import ru.tatarcha.tatlearn.core.designsystem.component.exercise.ExerciseHeader
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrConfirmSheet
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import ru.tatarcha.tatlearn.navigation.LessonKey
import ru.tatarcha.tatlearn.navigation.ResultKey
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.exercise_correct_answer
import tatlearn.shared.generated.resources.exercise_exit_confirm
import tatlearn.shared.generated.resources.exercise_exit_message
import tatlearn.shared.generated.resources.exercise_exit_stay
import tatlearn.shared.generated.resources.exercise_exit_title
import tatlearn.shared.generated.resources.exercise_spell_hint
import tatlearn.shared.generated.resources.exercise_xp_gain

@Composable
fun LessonRoute(
    key: LessonKey,
    onFinished: (ResultKey) -> Unit,
    onExit: () -> Unit,
    viewModel: LessonViewModel = koinViewModel(parameters = { parametersOf(key) }),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LessonUiEffect.NavigateToResult -> onFinished(effect.key)
                LessonUiEffect.Exit -> onExit()
            }
        }
    }
    // Назад (системная кнопка или свайп от края) спрашивает подтверждение, а не уходит сразу.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = true,
        onBackCompleted = { viewModel.onEvent(LessonUiEvent.CloseClicked) },
    )
    val state by viewModel.state.collectAsState()
    LessonScreen(state, viewModel::onEvent)
}

@Composable
fun LessonScreen(state: LessonUiState, onEvent: (LessonUiEvent) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        when (val load = state.load) {
            LoadState.Loading -> LoadingState()
            is LoadState.Error -> ErrorState(code = load.code, onRetry = { onEvent(LessonUiEvent.RetryClicked) })
            is LoadState.Content -> ExerciseFrame(state, onEvent)
        }
        TamyrConfirmSheet(
            visible = state.showExitDialog,
            title = stringResource(Res.string.exercise_exit_title),
            message = stringResource(Res.string.exercise_exit_message),
            confirmText = stringResource(Res.string.exercise_exit_confirm),
            dismissText = stringResource(Res.string.exercise_exit_stay),
            onConfirm = { onEvent(LessonUiEvent.ExitConfirmed) },
            onDismiss = { onEvent(LessonUiEvent.ExitDismissed) },
        )
    }
}

@Composable
private fun ExerciseFrame(state: LessonUiState, onEvent: (LessonUiEvent) -> Unit) {
    val step = state.step ?: return
    val feedback = state.feedback
    Column(Modifier.fillMaxSize().background(TamyrTheme.colors.background).statusBarsPadding()) {
        ExerciseHeader(
            progress = state.progress,
            onClose = { onEvent(LessonUiEvent.CloseClicked) },
        )
        // Каждый шаг въезжает справа; обратная связь того же шага не должна запускать анимацию заново.
        AnimatedContent(
            targetState = state.stepIndex,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = {
                (slideInHorizontally(tween(TamyrMotion.STEP_MS)) { it / 4 } + fadeIn(tween(TamyrMotion.STEP_MS))) togetherWith
                    fadeOut(tween(TamyrMotion.FAST_MS))
            },
            label = "exerciseStep",
        ) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                when (step) {
                    is ExerciseStep.Matching -> MatchingContent(step, state.input as StepInput.Matching, onEvent)
                    is ExerciseStep.Quiz -> QuizContent(step, state.input as StepInput.Choice, feedback, onEvent)
                    is ExerciseStep.Listening -> ListeningContent(step, state.input as StepInput.Choice, feedback, onEvent)
                    is ExerciseStep.SentenceBuilder -> SentenceContent(step, state.input as StepInput.Sentence, feedback, onEvent)
                    is ExerciseStep.Translation -> TranslationContent(step, state.input as StepInput.Translation, feedback, onEvent)
                }
            }
        }
        ExerciseFooter(
            feedback = when {
                feedback == null -> AnswerFeedback.None
                feedback.isCorrect -> AnswerFeedback.Correct
                else -> AnswerFeedback.Wrong
            },
            checkEnabled = state.canCheck(),
            detail = feedback?.let { feedbackDetail(it) },
            onCheck = { onEvent(LessonUiEvent.CheckClicked) },
            onContinue = { onEvent(LessonUiEvent.ContinueClicked) },
        )
    }
}

@Composable
private fun feedbackDetail(feedback: StepFeedback): String? {
    val parts = buildList {
        if (feedback.isCorrect) {
            feedback.detail?.let(::add)
            feedback.spellingHint?.let { add(stringResource(Res.string.exercise_spell_hint, it)) }
            if (feedback.xpGained > 0) add(stringResource(Res.string.exercise_xp_gain, feedback.xpGained))
        } else {
            feedback.correctAnswer?.let { add(stringResource(Res.string.exercise_correct_answer, it)) }
        }
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

/** Активна ли «Проверить» на текущем шаге. У сопоставления своего состояния кнопки проверки нет. */
private fun LessonUiState.canCheck(): Boolean {
    val step = step ?: return false
    return when (val input = input) {
        is StepInput.Choice -> input.selected != null
        is StepInput.Matching -> false
        is StepInput.Sentence -> (step as? ExerciseStep.SentenceBuilder)?.let { input.placed.size == it.task.answer.size } ?: false
        is StepInput.Translation -> input.value.text.isNotBlank()
    }
}

// region Превью

private const val Group = "Lesson"
private val correct = StepFeedback(isCorrect = true, detail = "китап — книга", xpGained = 3)
private fun wrong(answer: String) = StepFeedback(isCorrect = false, correctAnswer = answer)

@Composable
private fun LessonPreview(state: LessonUiState) = PreviewFrame { LessonScreen(state) {} }

@Preview(name = "Loading", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun LessonLoadingPreview() = LessonPreview(LessonUiState(load = LoadState.Loading))

@Preview(name = "Error", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun LessonErrorPreview() = LessonPreview(LessonUiState(load = LoadState.Error("NET_TIMEOUT")))

@Preview(name = "Error · no exercises", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun LessonNoExercisesPreview() = LessonPreview(LessonUiState(load = LoadState.Error("NO_EXERCISES")))

@Preview(name = "Exit dialog", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun LessonExitDialogPreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.quiz, StepInput.Choice(selected = 0), showExitDialog = true),
)

// Сопоставление

@Preview(name = "Matching · start", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun MatchingStartPreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.matching, StepInput.Matching(), stepIndex = 0),
)

@Preview(name = "Matching · word selected + hint", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun MatchingSelectedPreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.matching, StepInput.Matching(leftSelected = 1, matched = setOf(0)), stepIndex = 0),
)

@Preview(name = "Matching · wrong pair flash", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun MatchingWrongPreview() = LessonPreview(
    LessonPreviewData.state(
        LessonPreviewData.matching,
        StepInput.Matching(leftSelected = 1, matched = setOf(0), wrongPair = 1 to 3, mistakes = 1),
        stepIndex = 0,
    ),
)

@Preview(name = "Matching · done", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun MatchingDonePreview() = LessonPreview(
    LessonPreviewData.state(
        LessonPreviewData.matching,
        StepInput.Matching(matched = setOf(0, 1, 2, 3, 4)),
        feedback = StepFeedback(isCorrect = true, xpGained = 3),
        stepIndex = 0,
    ),
)

// Квиз

@Preview(name = "Quiz · new word", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun QuizNewWordPreview() = LessonPreview(LessonPreviewData.state(LessonPreviewData.quiz, StepInput.Choice()))

@Preview(name = "Quiz · option selected", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun QuizSelectedPreview() = LessonPreview(LessonPreviewData.state(LessonPreviewData.quiz, StepInput.Choice(selected = 2)))

@Preview(name = "Quiz · correct", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun QuizCorrectPreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.quiz, StepInput.Choice(selected = 0), feedback = correct),
)

@Preview(name = "Quiz · wrong", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun QuizWrongPreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.quiz, StepInput.Choice(selected = 2), feedback = wrong("книга")),
)

@Preview(name = "Quiz · repeated word", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun QuizLastLifePreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.quizRepeat, StepInput.Choice(), stepIndex = 8),
)

// Аудирование

@Preview(name = "Listening · idle", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun ListeningIdlePreview() = LessonPreview(LessonPreviewData.state(LessonPreviewData.listening, StepInput.Choice()))

@Preview(name = "Listening · playing", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun ListeningPlayingPreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.listening, StepInput.Choice(selected = 1), audio = AudioPlayerState.Playing),
)

@Preview(name = "Listening · correct", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun ListeningCorrectPreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.listening, StepInput.Choice(selected = 0), feedback = StepFeedback(true, detail = "Рәхмәт", xpGained = 3)),
)

@Preview(name = "Listening · wrong", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun ListeningWrongPreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.listening, StepInput.Choice(selected = 2), feedback = wrong("Рәхмәт")),
)

@Preview(name = "Listening · audio error", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun ListeningAudioErrorPreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.listening, StepInput.Choice(), audio = AudioPlayerState.Error),
)

// Сборка предложения

@Preview(name = "Sentence · empty", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun SentenceEmptyPreview() = LessonPreview(LessonPreviewData.state(LessonPreviewData.sentence, StepInput.Sentence()))

@Preview(name = "Sentence · partly placed", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun SentencePartialPreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.sentence, StepInput.Sentence(placed = listOf(0, 2))),
)

@Preview(name = "Sentence · complete", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun SentenceCompletePreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.sentence, StepInput.Sentence(placed = listOf(0, 2, 1))),
)

@Preview(name = "Sentence · correct", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun SentenceCorrectPreview() = LessonPreview(
    LessonPreviewData.state(
        LessonPreviewData.sentence,
        StepInput.Sentence(placed = listOf(0, 2, 1)),
        feedback = StepFeedback(true, detail = "Мин мәктәпкә барам", xpGained = 3),
    ),
)

@Preview(name = "Sentence · wrong", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun SentenceWrongPreview() = LessonPreview(
    LessonPreviewData.state(
        LessonPreviewData.sentence,
        StepInput.Sentence(placed = listOf(0, 1, 2)),
        feedback = wrong("Мин мәктәпкә барам"),
    ),
)

// Письменный перевод

@Preview(name = "Translation · empty", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun TranslationEmptyPreview() = LessonPreview(LessonPreviewData.state(LessonPreviewData.translation, StepInput.Translation()))

@Preview(name = "Translation · typed", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun TranslationTypedPreview() = LessonPreview(
    LessonPreviewData.state(LessonPreviewData.translation, StepInput.Translation(LessonPreviewData.text("Хәерле"))),
)

@Preview(name = "Translation · correct", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun TranslationCorrectPreview() = LessonPreview(
    LessonPreviewData.state(
        LessonPreviewData.translation,
        StepInput.Translation(LessonPreviewData.text("Хәерле иртә")),
        feedback = StepFeedback(true, detail = "Хәерле иртә", xpGained = 3),
    ),
)

@Preview(name = "Translation · approximate spelling", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun TranslationApproximatePreview() = LessonPreview(
    LessonPreviewData.state(
        LessonPreviewData.translation,
        StepInput.Translation(LessonPreviewData.text("Хэерле иртэ")),
        feedback = StepFeedback(true, spellingHint = "Хәерле иртә", xpGained = 3),
    ),
)

@Preview(name = "Translation · wrong", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun TranslationWrongPreview() = LessonPreview(
    LessonPreviewData.state(
        LessonPreviewData.translation,
        StepInput.Translation(LessonPreviewData.text("Исәнмесез")),
        feedback = wrong("Хәерле иртә"),
    ),
)

// endregion
