package ru.tatarcha.tatlearn.domain.exercise

import ru.tatarcha.tatlearn.domain.model.ExerciseType
import ru.tatarcha.tatlearn.domain.model.ListeningTask
import ru.tatarcha.tatlearn.domain.model.QuizQuestion
import ru.tatarcha.tatlearn.domain.model.SentenceTask
import ru.tatarcha.tatlearn.domain.model.TranslationTask
import ru.tatarcha.tatlearn.domain.model.WordPair

/** Один экран урока. [id] стабилен, чтобы «повторить ошибки» могло ссылаться на шаги. */
sealed interface ExerciseStep {
    val id: String
    val type: ExerciseType

    /** [rightOrder] — порядок показа русской колонки (индексы в [pairs]). */
    data class Matching(
        override val id: String,
        val pairs: List<WordPair>,
        val rightOrder: List<Int>,
        val notes: Map<String, String>,
    ) : ExerciseStep {
        override val type = ExerciseType.Matching
    }

    data class Quiz(override val id: String, val question: QuizQuestion) : ExerciseStep {
        override val type = ExerciseType.Quiz
    }

    data class Listening(override val id: String, val task: ListeningTask) : ExerciseStep {
        override val type = ExerciseType.Listening
    }

    data class SentenceBuilder(override val id: String, val task: SentenceTask) : ExerciseStep {
        override val type = ExerciseType.SentenceBuilder
    }

    data class Translation(override val id: String, val task: TranslationTask) : ExerciseStep {
        override val type = ExerciseType.Translation
    }
}
