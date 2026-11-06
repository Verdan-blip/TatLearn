package ru.tatarcha.tatlearn.domain.exercise

import ru.tatarcha.tatlearn.domain.model.ExerciseType
import ru.tatarcha.tatlearn.domain.model.LessonContent
import kotlin.random.Random

/**
 * Превращает контент урока в упорядоченный список шагов (решения 04.11.2026, `.docs/21-course-roadmap.md`, раздел 4):
 * карточки новых слов, карточка-правило, сопоставление, затем упражнения вперемешку: не подряд одного вида, лёгкие раньше сложных,
 * последним продуктивное (сборка или перевод).
 * Аудирование не планируется, пока у задания нет `audioUrl` (озвучки нет, ответ на слух не определить):
 * данные остаются в JSON, и шаги включатся сами, когда записи появятся.
 * «Повторить ошибки» (`onlyStepIds`) идёт без карточек и в исходном порядке видов.
 */
object LessonPlanner {

    fun plan(content: LessonContent, onlyStepIds: Set<String> = emptySet()): List<ExerciseStep> {
        val exercises = exercises(content)
        if (onlyStepIds.isNotEmpty()) return exercises.filter { it.id in onlyStepIds }
        return intro(content) + interleaved(exercises)
    }

    /** Упражнения в исходном порядке видов: сопоставление, квизы, аудирование, предложения, переводы. */
    private fun exercises(content: LessonContent): List<ExerciseStep> = buildList {
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

    /** Карточки новых слов (слова сопоставления урока) и, если есть, карточка-правило. */
    private fun intro(content: LessonContent): List<ExerciseStep> = buildList {
        val words = content.matching.pairs
        words.forEachIndexed { i, word ->
            add(
                ExerciseStep.WordIntro(
                    id = "intro-$i",
                    word = word,
                    position = i + 1,
                    total = words.size,
                    words = words,
                    // Заметки сопоставления подписаны «Мыраужан: …»; на карточке слова говорит сам Мыраужан, префикс не нужен.
                    note = content.matching.notes[word.tatar]?.removePrefix(NOTE_PREFIX),
                ),
            )
        }
        content.rule?.let { add(ExerciseStep.Rule("rule", it)) }
    }

    /**
     * Сопоставление остаётся первым (лёгкое узнавание), остальные упражнения равномерно перемешиваются по видам:
     * положение шага внутри своего вида задаёт его место в уроке, продуктивные виды сдвинуты ближе к концу.
     */
    internal fun interleaved(steps: List<ExerciseStep>): List<ExerciseStep> {
        val matching = steps.filterIsInstance<ExerciseStep.Matching>()
        val rest = steps - matching.toSet()
        val byType = rest.groupBy { it.type }
        val mixed = rest
            .map { step ->
                val group = byType.getValue(step.type)
                val place = (group.indexOf(step) + 0.5) / group.size + bias(step.type)
                Triple(step, place, priority(step.type))
            }
            .sortedWith(compareBy({ it.second }, { it.third }))
            .map { it.first }
            .toMutableList()
        // Последний шаг продуктивный: если он оказался выбором ответа, переносим его перед последним продуктивным.
        val last = mixed.lastOrNull()
        if (last != null && last.type.isReceptive()) {
            val lastProductive = mixed.indexOfLast { !it.type.isReceptive() }
            if (lastProductive >= 0) {
                mixed.removeAt(mixed.lastIndex)
                mixed.add(lastProductive, last)
            }
        }
        return matching + mixed
    }

    private fun ExerciseType.isReceptive() = this == ExerciseType.Quiz || this == ExerciseType.Listening

    private fun bias(type: ExerciseType): Double = if (type.isReceptive()) RECEPTIVE_BIAS else PRODUCTIVE_BIAS

    private fun priority(type: ExerciseType): Int = when (type) {
        ExerciseType.Quiz -> 0
        ExerciseType.Listening -> 1
        ExerciseType.SentenceBuilder -> 2
        else -> 3
    }

    /** Детерминированное перемешивание (по содержимому), чтобы раскладка переживала рекомпозицию и смерть процесса. */
    internal fun shuffledOrder(keys: List<String>): List<Int> {
        val indices = keys.indices.toList()
        if (indices.size < 2) return indices
        val shuffled = indices.shuffled(Random(keys.joinToString("|").hashCode()))
        return if (shuffled == indices) indices.drop(1) + indices.first() else shuffled
    }

    private const val NOTE_PREFIX = "Мыраужан: "
    private const val RECEPTIVE_BIAS = -0.05
    private const val PRODUCTIVE_BIAS = 0.15
}
