package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import ru.tatarcha.tatlearn.core.audio.AudioPlayerState
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import ru.tatarcha.tatlearn.domain.model.ListeningTask
import ru.tatarcha.tatlearn.domain.model.QuizQuestion
import ru.tatarcha.tatlearn.domain.model.SentenceTask
import ru.tatarcha.tatlearn.domain.model.TranslationTask
import ru.tatarcha.tatlearn.domain.model.WordPair

/** Тестовые данные только для `@Preview`; скопированы из lesson_4.json (нужна проверка носителем языка). */
internal object LessonPreviewData {
    val matching = ExerciseStep.Matching(
        id = "matching",
        pairs = listOf(
            WordPair("әни", "мама"),
            WordPair("ипи", "хлеб"),
            WordPair("су", "вода"),
            WordPair("мәктәп", "школа"),
            WordPair("дус", "друг"),
        ),
        rightOrder = listOf(2, 0, 3, 1, 4),
        notes = mapOf("ипи" to "Мыраужан: «Ипи» — хлеб. В деревне говорят: «Ипи — барыннан да олы» — хлеб всему голова."),
    )
    val quiz = ExerciseStep.Quiz(
        id = "quiz-0",
        question = QuizQuestion("китап", "[китáп]", listOf("книга", "стол", "окно", "ручка"), correctIndex = 0, isNewWord = true),
    )
    val quizRepeat = ExerciseStep.Quiz(
        id = "quiz-1",
        question = QuizQuestion("әти", null, listOf("папа", "дедушка", "брат", "друг"), correctIndex = 0, isNewWord = false),
    )
    val listening = ExerciseStep.Listening(
        id = "listening-0",
        task = ListeningTask(audioUrl = null, options = listOf("Рәхмәт", "Рәхим ит", "Исәнме"), correctIndex = 0),
    )
    val sentence = ExerciseStep.SentenceBuilder(
        id = "sentence-0",
        task = SentenceTask("Я иду в школу", listOf("Мин", "барам", "мәктәпкә", "китап", "өйгә"), listOf("Мин", "мәктәпкә", "барам")),
    )
    val translation = ExerciseStep.Translation(
        id = "translation-0",
        task = TranslationTask("Доброе утро", listOf("Хәерле иртә"), audioUrl = null),
    )

    fun text(value: String) = TextFieldValue(value, TextRange(value.length))

    /** Урок из 12 одинаковых шагов, стоящий на [stepIndex], чтобы полоса прогресса показывала реалистичное значение. */
    fun state(
        step: ExerciseStep,
        input: StepInput,
        feedback: StepFeedback? = null,
        lives: Int = 3,
        stepIndex: Int = 4,
        audio: AudioPlayerState = AudioPlayerState.Idle,
        showExitDialog: Boolean = false,
    ) = LessonUiState(
        load = LoadState.Content(Unit),
        steps = List(12) { step },
        stepIndex = stepIndex,
        lives = lives,
        input = input,
        feedback = feedback,
        audio = audio,
        showExitDialog = showExitDialog,
    )
}
