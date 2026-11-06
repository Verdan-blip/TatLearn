package ru.tatarcha.tatlearn.domain.exercise

import ru.tatarcha.tatlearn.domain.model.ExerciseType
import ru.tatarcha.tatlearn.domain.model.LessonRule
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

    /**
     * Карточка нового слова (макеты 50–52): одно слово, перевод, звук. [position] с единицы, [words] все слова группы (на последней
     * карточке они показываются чипами), [note] подсказка Мыраужана. Проверки нет: шаг не влияет на точность и XP.
     */
    data class WordIntro(
        override val id: String,
        val word: WordPair,
        val position: Int,
        val total: Int,
        val words: List<WordPair>,
        val note: String?,
    ) : ExerciseStep {
        override val type = ExerciseType.Intro
        val isLast: Boolean get() = position == total
    }

    /** Карточка-правило (макет 53): одна мысль и примеры; проверки нет. */
    data class Rule(override val id: String, val rule: LessonRule) : ExerciseStep {
        override val type = ExerciseType.Rule
    }

    /** [isRetry]: слово вернулось после ошибки (макет 54); такой шаг не влияет на точность и XP. */
    data class Quiz(override val id: String, val question: QuizQuestion, val isRetry: Boolean = false) : ExerciseStep {
        override val type = ExerciseType.Quiz
    }

    data class Listening(override val id: String, val task: ListeningTask, val isRetry: Boolean = false) : ExerciseStep {
        override val type = ExerciseType.Listening
    }

    data class SentenceBuilder(override val id: String, val task: SentenceTask) : ExerciseStep {
        override val type = ExerciseType.SentenceBuilder
    }

    data class Translation(override val id: String, val task: TranslationTask) : ExerciseStep {
        override val type = ExerciseType.Translation
    }
}
