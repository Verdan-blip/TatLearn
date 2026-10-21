package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.exercise.ExerciseStep
import ru.tatarcha.tatlearn.domain.exercise.LessonPlanner
import ru.tatarcha.tatlearn.domain.model.UserStats
import ru.tatarcha.tatlearn.feature.lesson.testLessonContent
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

    @Test fun plannerOrdersStepsAndFilters() {
        val steps = LessonPlanner.plan(testLessonContent())
        assertEquals(listOf("matching", "quiz-0", "quiz-1", "listening-0", "sentence-0", "translation-0"), steps.map { it.id })
        assertEquals(listOf("quiz-1", "translation-0"), LessonPlanner.plan(testLessonContent(), setOf("translation-0", "quiz-1")).map { it.id })
    }

    @Test fun matchingShuffleIsStableAndNeverIdentity() {
        val a = LessonPlanner.plan(testLessonContent()).filterIsInstance<ExerciseStep.Matching>().single()
        val b = LessonPlanner.plan(testLessonContent()).filterIsInstance<ExerciseStep.Matching>().single()
        assertEquals(a.rightOrder, b.rightOrder)
        assertNotEquals(a.pairs.indices.toList(), a.rightOrder)
        assertTrue(a.rightOrder.sorted() == a.pairs.indices.toList())
    }
}
