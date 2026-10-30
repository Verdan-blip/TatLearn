package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.model.CardGain
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.LessonStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeProgressTrackerTest {
    private fun lesson(n: Int) = Lesson("lesson_$n", n, "T$n", "Р$n")

    private val alphabet = CourseModule("alphabet", "Алфавит", "Алфавит", "alphabet", listOf(lesson(1), lesson(2)))
    private val food = CourseModule("food", "Ашлар", "Еда", "food", listOf(lesson(3), lesson(4), lesson(5)))
    private val catalog = listOf(alphabet, food)

    private fun home(vararg done: Int) = HomeContentBuilder.build(catalog, done.map { LessonProgress("lesson_$it", LessonStatus.Completed, 90) })!!

    @Test fun firstLaunchHasNothingToCompareWith() {
        assertNull(HomeProgressTracker().gainFor(home(1)))
    }

    @Test fun passedLessonGivesGainWithCardCounter() {
        val tracker = HomeProgressTracker()
        tracker.commit(home(1, 2, 3))
        val gain = tracker.gainFor(home(1, 2, 3, 4))!!
        assertEquals("food", gain.moduleId)
        assertEquals(33, gain.fromPercent)
        assertEquals(66, gain.toPercent)
        assertEquals(CardGain(fromCompleted = 1, toCompleted = 2, total = 3), gain.card)
        assertTrue(gain.card!!.showsChip)
    }

    @Test fun askingForGainDoesNotConsumeIt() {
        val tracker = HomeProgressTracker()
        tracker.commit(home())
        assertEquals(tracker.gainFor(home(1)), tracker.gainFor(home(1)))
    }

    @Test fun committedProgressIsNotAGainAnymore() {
        val tracker = HomeProgressTracker()
        tracker.commit(home())
        tracker.commit(home(1))
        assertNull(tracker.gainFor(home(1)))
    }

    @Test fun cardOfAnotherModuleHasNoCardGain() {
        val tracker = HomeProgressTracker()
        tracker.commit(home(1))
        val gain = tracker.gainFor(home(1, 2))!!
        assertEquals("alphabet", gain.moduleId)
        assertEquals(100, gain.toPercent)
        assertNull(gain.card)
    }

    @Test fun lowerOrEqualProgressIsNotAGain() {
        val tracker = HomeProgressTracker()
        tracker.commit(home(1, 2, 3))
        assertNull(tracker.gainFor(home(1, 2)))
        assertNull(tracker.gainFor(home(1, 2, 3)))
    }
}
