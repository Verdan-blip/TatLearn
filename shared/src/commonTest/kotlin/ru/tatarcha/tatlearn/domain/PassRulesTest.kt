package ru.tatarcha.tatlearn.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ru.tatarcha.tatlearn.domain.exercise.PassRules

class PassRulesTest {
    @Test fun passedAtThresholdAndAbove() {
        assertTrue(PassRules.isPassed(accuracyPercent = 80, isMistakesRun = false, mistakes = 2))
        assertTrue(PassRules.isPassed(accuracyPercent = 100, isMistakesRun = false, mistakes = 0))
    }

    @Test fun notPassedBelowThreshold() {
        assertFalse(PassRules.isPassed(accuracyPercent = 79, isMistakesRun = false, mistakes = 3))
    }

    @Test fun mistakesRunIsPassedOnlyWhenEverythingIsFixed() {
        assertTrue(PassRules.isPassed(accuracyPercent = 100, isMistakesRun = true, mistakes = 0))
        assertFalse(PassRules.isPassed(accuracyPercent = 67, isMistakesRun = true, mistakes = 1))
    }

    @Test fun missingAnswersRoundsUp() {
        assertEquals(1, PassRules.missingAnswers(correct = 7, total = 10))
        assertEquals(2, PassRules.missingAnswers(correct = 6, total = 10))
        assertEquals(1, PassRules.missingAnswers(correct = 3, total = 5))
        assertEquals(0, PassRules.missingAnswers(correct = 9, total = 10))
        assertEquals(0, PassRules.missingAnswers(correct = 0, total = 0))
    }
}
