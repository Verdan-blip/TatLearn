package ru.tatarcha.tatlearn.domain.exercise

import ru.tatarcha.tatlearn.domain.model.LessonContent
import kotlin.random.Random

/**
 * Превращает контент урока в упорядоченный список шагов: сопоставление, квиз, аудирование, предложения, перевод.
 * Аудирование на MVP не планируется, пока у задания нет `audioUrl` (озвучки нет, ответ на слух не определить):
 * данные остаются в JSON, и шаги включатся сами, когда записи появятся.
 */
object LessonPlanner {

    fun plan(content: LessonContent, onlyStepIds: Set<String> = emptySet()): List<ExerciseStep> {
        val steps = buildList {
            if (content.matching.pairs.isNotEmpty()) {
                add(
                    ExerciseStep.Matching(
                        id = "matching",
                        pairs = content.matching.pairs,
                        rightOrder = shuffledOrder(content.matching.pairs.map { it.tatar }),
                        notes = content.matching.notes,
                    ),
                )
            }
            content.quiz.forEachIndexed { i, q -> add(ExerciseStep.Quiz("quiz-$i", q)) }
            content.listening.forEachIndexed { i, t ->
                if (t.audioUrl != null) add(ExerciseStep.Listening("listening-$i", t))
            }
            content.sentences.forEachIndexed { i, t -> add(ExerciseStep.SentenceBuilder("sentence-$i", t)) }
            content.translations.forEachIndexed { i, t -> add(ExerciseStep.Translation("translation-$i", t)) }
        }
        return if (onlyStepIds.isEmpty()) steps else steps.filter { it.id in onlyStepIds }
    }

    /** Детерминированное перемешивание (по содержимому), чтобы раскладка переживала рекомпозицию и смерть процесса. */
    internal fun shuffledOrder(keys: List<String>): List<Int> {
        val indices = keys.indices.toList()
        if (indices.size < 2) return indices
        val shuffled = indices.shuffled(Random(keys.joinToString("|").hashCode()))
        return if (shuffled == indices) indices.drop(1) + indices.first() else shuffled
    }
}
