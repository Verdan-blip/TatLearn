package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.HomeCardKind
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.LessonStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeContentBuilderTest {
    private fun lesson(n: Int) = Lesson("lesson_$n", n, "T$n", "Р$n")

    private val alphabet = CourseModule("alphabet", "Алфавит", "Алфавит", "alphabet", listOf(lesson(1), lesson(2)))
    private val food = CourseModule("food", "Ашлар", "Еда", "food", listOf(lesson(3), lesson(4)), lockHint = "после алфавита")
    private val numbers = CourseModule("numbers", "Саннар", "Числа", "numbers", listOf(lesson(5)))
    private val nature = CourseModule("nature", "Табигать", "Природа", "nature", listOf(lesson(6)), requires = "food", lockHint = "после еды")
    private val catalog = listOf(alphabet, food, numbers, nature)

    private fun done(vararg numbers: Int) = numbers.map { LessonProgress("lesson_$it", LessonStatus.Completed, 90) }

    @Test fun freshUserStartsFirstLessonAndOnlyFirstModuleIsOpen() {
        val home = HomeContentBuilder.build(catalog, emptyList())!!
        assertEquals("lesson_1", home.currentLesson.lesson.id)
        assertEquals(0, home.currentLesson.completedLessons)
        assertEquals(listOf(false, true, true, true), home.modules.map { it.isLocked })
        assertEquals("после алфавита", home.modules[1].lockHint)
    }

    @Test fun startedButNotCompletedLessonDoesNotCountAsPassed() {
        val progress = listOf(LessonProgress("lesson_1", LessonStatus.InProgress, 0))
        val home = HomeContentBuilder.build(catalog, progress)!!
        assertEquals("lesson_1", home.currentLesson.lesson.id)
        assertEquals(0, home.modules[0].progressPercent)
    }

    @Test fun passingAllLessonsOpensNextModuleSoftly() {
        val home = HomeContentBuilder.build(catalog, done(1, 2))!!
        assertEquals(100, home.modules[0].progressPercent)
        assertEquals(false, home.modules[1].isLocked)
        assertNull(home.modules[1].lockHint)
        assertEquals("lesson_3", home.currentLesson.lesson.id)
        assertEquals("Ашлар", home.currentLesson.topicTt)
    }

    @Test fun partialModuleKeepsNextOneLockedAndShowsPercentAndCard() {
        val home = HomeContentBuilder.build(catalog, done(1, 2, 3))!!
        assertEquals(50, home.modules[1].progressPercent)
        assertEquals(true, home.modules[2].isLocked)
        assertEquals("lesson_4", home.currentLesson.lesson.id)
        assertEquals(1, home.currentLesson.completedLessons)
        assertEquals(2, home.currentLesson.totalLessons)
    }

    @Test fun requiresOverridesPreviousModuleInOrder() {
        val home = HomeContentBuilder.build(catalog, done(1, 2, 3, 4))!!
        assertEquals(null, home.modules[3].lockedBy)
        assertEquals("food", HomeContentBuilder.build(catalog, done(1, 2))!!.modules[3].lockedBy)
    }

    @Test fun everythingDoneShowsLastLessonOfLastModule() {
        val home = HomeContentBuilder.build(catalog, done(1, 2, 3, 4, 5, 6))!!
        assertEquals(listOf(100, 100, 100, 100), home.modules.map { it.progressPercent })
        assertEquals("lesson_6", home.currentLesson.lesson.id)
        assertEquals(home.currentLesson.totalLessons, home.currentLesson.completedLessons)
    }

    @Test fun newcomerGetsStartCardNotContinue() {
        val home = HomeContentBuilder.build(catalog, emptyList())!!
        assertEquals(HomeCardKind.Start, home.currentLesson.kind)
        assertEquals(false, home.currentLesson.restartsLesson)
        assertEquals(HomeCardKind.Start, HomeContentBuilder.build(catalog, listOf(LessonProgress("lesson_1", LessonStatus.NotStarted, 0)))!!.currentLesson.kind)
    }

    @Test fun startedButNotPassedLessonIsContinueThatRestarts() {
        val home = HomeContentBuilder.build(catalog, listOf(LessonProgress("lesson_1", LessonStatus.InProgress, 0)))!!
        assertEquals(HomeCardKind.Continue, home.currentLesson.kind)
        assertEquals(true, home.currentLesson.restartsLesson)
        assertEquals("lesson_1", home.currentLesson.lesson.id)
    }

    @Test fun partlyPassedModuleIsContinueWithoutRestart() {
        val home = HomeContentBuilder.build(catalog, done(1))!!
        assertEquals(HomeCardKind.Continue, home.currentLesson.kind)
        assertEquals(false, home.currentLesson.restartsLesson)
    }

    @Test fun firstLessonOfNextModuleIsNewModuleCard() {
        val home = HomeContentBuilder.build(catalog, done(1, 2))!!
        assertEquals(HomeCardKind.NewModule, home.currentLesson.kind)
        assertEquals("lesson_3", home.currentLesson.lesson.id)
        // Урок нового модуля уже начат: это снова «Продолжить» с перезапуском.
        val started = HomeContentBuilder.build(catalog, done(1, 2) + LessonProgress("lesson_3", LessonStatus.InProgress, 0))!!
        assertEquals(HomeCardKind.Continue, started.currentLesson.kind)
        assertEquals(true, started.currentLesson.restartsLesson)
    }

    @Test fun finishedCourseIsAllDoneCard() {
        val home = HomeContentBuilder.build(catalog, done(1, 2, 3, 4, 5, 6))!!
        assertEquals(HomeCardKind.AllDone, home.currentLesson.kind)
    }

    @Test fun emptyCatalogGivesNothing() {
        assertNull(HomeContentBuilder.build(emptyList(), emptyList()))
        assertNull(HomeContentBuilder.build(listOf(CourseModule("x", "x", "x", "x", emptyList())), emptyList()))
    }
}
