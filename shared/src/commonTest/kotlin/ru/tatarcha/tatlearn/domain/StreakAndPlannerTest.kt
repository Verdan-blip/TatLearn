package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import ru.tatarcha.tatlearn.domain.exercise.LessonPlanner
import ru.tatarcha.tatlearn.domain.model.UserStats
import ru.tatarcha.tatlearn.feature.lesson.testLessonContent
import ru.tatarcha.tatlearn.domain.model.LessonRule
import ru.tatarcha.tatlearn.domain.model.RuleExample
import ru.tatarcha.tatlearn.domain.model.QuizQuestion
import ru.tatarcha.tatlearn.domain.model.ListeningTask
import ru.tatarcha.tatlearn.domain.model.SentenceTask
import ru.tatarcha.tatlearn.domain.model.TranslationTask
import ru.tatarcha.tatlearn.domain.model.MatchingExercise
import ru.tatarcha.tatlearn.domain.model.ExerciseType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StreakAndPlannerTest {
    @Test fun streakRules() {
        assertEquals(1, StreakCalculator.next(0, null, today = 100))
        assertEquals(5, StreakCalculator.next(5, 100, today = 100))
        assertEquals(6, StreakCalculator.next(5, 99, today = 100))
        assertEquals(1, StreakCalculator.next(5, 90, today = 100))
    }

    @Test fun levelMath() {
        assertEquals(1, UserStats(xp = 0, streak = 0).level)
        assertEquals(100, UserStats(xp = 0, streak = 0).xpToNextLevel)
        assertEquals(4, UserStats(xp = 340, streak = 0).level)
        assertEquals(60, UserStats(xp = 340, streak = 0).xpToNextLevel)
    }

    @Test fun plannerSkipsListeningWithoutAudioAndKeepsItWithAudio() {
        val content = testLessonContent()
        assertEquals(emptyList(), LessonPlanner.plan(content).filterIsInstance<ExerciseStep.Listening>())
        val withAudio = content.copy(listening = content.listening.map { it.copy(audioUrl = "https://example.org/a.mp3") })
        assertEquals(listOf("listening-0"), LessonPlanner.plan(withAudio).filterIsInstance<ExerciseStep.Listening>().map { it.id })
    }

    @Test fun shownStreakIsZeroWhenADayWasMissed() {
        assertEquals(0, StreakCalculator.current(5, null, today = 100))
        assertEquals(5, StreakCalculator.current(5, 100, today = 100))
        assertEquals(5, StreakCalculator.current(5, 99, today = 100))
        assertEquals(0, StreakCalculator.current(5, 98, today = 100))
        assertEquals(0, StreakCalculator.current(1, 90, today = 100))
    }

    @Test fun plannerOrdersStepsAndFilters() {
        val steps = LessonPlanner.plan(testLessonContent())
        // Карточки слов, сопоставление, затем упражнения вперемешку; последним продуктивное (перевод), выбор ответа не в хвосте.
        assertEquals(
            listOf("intro-0", "intro-1", "intro-2", "matching", "quiz-0", "sentence-0", "quiz-1", "translation-0"),
            steps.map { it.id },
        )
        // «Повторить ошибки»: без карточек, в исходном порядке видов.
        assertEquals(listOf("quiz-1", "translation-0"), LessonPlanner.plan(testLessonContent(), setOf("translation-0", "quiz-1")).map { it.id })
    }

    @Test fun plannerAddsIntroCardsAndTheRuleCard() {
        val rule = LessonRule("Правило", "Одна мысль.", listOf(RuleExample("Мин чәй эчәм.", "Я пью чай.", "м")))
        val steps = LessonPlanner.plan(testLessonContent().copy(rule = rule))
        val intro = steps.filterIsInstance<ExerciseStep.WordIntro>()
        assertEquals(3, intro.size)
        assertEquals(listOf(1, 2, 3), intro.map { it.position })
        assertTrue(intro.last().isLast)
        assertEquals("rule", steps[3].id)
        assertEquals(rule, (steps[3] as ExerciseStep.Rule).rule)
    }

    @Test fun plannerNeverPutsTheSameKindTwiceInARowAndEndsWithAProductiveStep() {
        val content = testLessonContent().copy(
            quiz = List(4) { QuizQuestion("слово$it", null, listOf("а", "б"), 0, isNewWord = false) },
            listening = List(2) { ListeningTask("audio/x$it.m4a", listOf("а", "б"), 0) },
            sentences = List(2) { SentenceTask("Фраза", listOf("Мин", "барам"), listOf("Мин", "барам")) },
            translations = List(3) { TranslationTask("Фраза$it", listOf("Фраза"), null) },
        )
        val exercises = LessonPlanner.plan(content).filterNot { it is ExerciseStep.WordIntro || it is ExerciseStep.Rule }.drop(1)
        assertEquals(11, exercises.size)
        exercises.zipWithNext().forEach { (a, b) -> assertNotEquals(a.type, b.type) }
        assertTrue(exercises.last().type in setOf(ExerciseType.SentenceBuilder, ExerciseType.Translation))
    }

    @Test fun lessonWithoutWordsHasNoIntroCards() {
        val steps = LessonPlanner.plan(testLessonContent().copy(matching = MatchingExercise(emptyList())))
        assertTrue(steps.none { it is ExerciseStep.WordIntro })
    }

    @Test fun matchingShuffleIsStableAndNeverIdentity() {
        val a = LessonPlanner.plan(testLessonContent()).filterIsInstance<ExerciseStep.Matching>().single()
        val b = LessonPlanner.plan(testLessonContent()).filterIsInstance<ExerciseStep.Matching>().single()
        assertEquals(a.rightOrder, b.rightOrder)
        assertNotEquals(a.pairs.indices.toList(), a.rightOrder)
        assertTrue(a.rightOrder.sorted() == a.pairs.indices.toList())
    }
}
