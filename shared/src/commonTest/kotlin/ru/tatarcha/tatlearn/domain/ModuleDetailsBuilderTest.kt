package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.LessonRowState
import ru.tatarcha.tatlearn.domain.model.LessonStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ModuleDetailsBuilderTest {
    private fun lesson(n: Int) = Lesson("lesson_$n", n, "T$n", "Р$n")

    private val alphabet = CourseModule("alphabet", "Алфавит", "Алфавит", "alphabet", listOf(lesson(1), lesson(2)))
    private val food = CourseModule("food", "Ашлар", "Еда", "food", listOf(lesson(3), lesson(4), lesson(5)))
    private val nature = CourseModule("nature", "Табигать", "Природа", "nature", listOf(lesson(6)), requires = "food", lockHint = "после еды")
    private val catalog = listOf(alphabet, food, nature)

    private fun done(vararg numbers: Int, score: Int = 90) = numbers.map { LessonProgress("lesson_$it", LessonStatus.Completed, score) }

    @Test fun firstModuleIsOpenWithFirstLessonRecommendedAndOthersFree() {
        val details = ModuleDetailsBuilder.build("alphabet", catalog, emptyList())!!
        assertEquals(listOf(LessonRowState.Next, LessonRowState.Available), details.lessons.map { it.state })
        assertEquals("lesson_1", details.nextLesson?.id)
        assertNull(details.lockedBy)
        assertEquals("food", details.nextModule?.id)
    }

    @Test fun completedLessonsCarryBestScoreAndNextMovesOn() {
        val details = ModuleDetailsBuilder.build("alphabet", catalog, done(1, score = 92))!!
        assertEquals(LessonRowState.Done, details.lessons[0].state)
        assertEquals(92, details.lessons[0].bestScore)
        assertEquals(LessonRowState.Next, details.lessons[1].state)
        assertEquals(1, details.completedLessons)
        assertEquals(0.5f, details.progress)
    }

    @Test fun startedButNotPassedLessonIsStillNext() {
        val progress = listOf(LessonProgress("lesson_1", LessonStatus.InProgress, 0))
        val details = ModuleDetailsBuilder.build("alphabet", catalog, progress)!!
        assertEquals(LessonRowState.Next, details.lessons[0].state)
        assertNull(details.lessons[0].bestScore)
    }

    @Test fun lockedModuleLocksAllLessonsAndNamesRequiredModuleWithItsProgress() {
        val details = ModuleDetailsBuilder.build("food", catalog, done(1))!!
        assertEquals(List(3) { LessonRowState.Locked }, details.lessons.map { it.state })
        assertNull(details.nextLesson)
        assertEquals("alphabet", details.lockedBy?.id)
        assertEquals(1, details.lockedBy?.completedLessons)
        assertEquals(2, details.lockedBy?.totalLessons)
    }

    @Test fun requiresOverridesPreviousModule() {
        // Предыдущий по списку — «Еда», но нужна именно она, поэтому после алфавита природа всё ещё закрыта.
        assertEquals("food", ModuleDetailsBuilder.build("nature", catalog, done(1, 2))!!.lockedBy?.id)
        assertNull(ModuleDetailsBuilder.build("nature", catalog, done(1, 2, 3, 4, 5))!!.lockedBy)
    }

    @Test fun completedModuleOffersNextModuleInsteadOfLesson() {
        val details = ModuleDetailsBuilder.build("alphabet", catalog, done(1, 2))!!
        assertEquals(true, details.isCompleted)
        assertNull(details.nextLesson)
        assertEquals("food", details.nextModule?.id)
    }

    @Test fun unknownModuleAndEmptyModuleAreHandled() {
        assertNull(ModuleDetailsBuilder.build("missing", catalog, emptyList()))
        val empty = ModuleDetailsBuilder.build("e", listOf(CourseModule("e", "E", "Э", "e", emptyList())), emptyList())!!
        assertEquals(false, empty.isCompleted)
        assertEquals(0f, empty.progress)
    }
}
