package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.LessonStatus
import ru.tatarcha.tatlearn.domain.model.ModuleState
import ru.tatarcha.tatlearn.domain.model.RoadmapRow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoadmapBuilderTest {
    private var lessonNumber = 0

    private fun module(id: String, lessons: Int): CourseModule {
        val items = List(lessons) { Lesson("lesson_${++lessonNumber}", lessonNumber, "T", "Р") }
        return CourseModule(id, id, id, id, items)
    }

    private val catalog = listOf(module("m1", 1), module("m2", 2), module("m3", 2), module("m4", 1), module("m5", 1))

    private fun roadmap(vararg doneLessons: Int): List<RoadmapRow> {
        val progress = doneLessons.map { LessonProgress("lesson_$it", LessonStatus.Completed, 90) }
        return RoadmapBuilder.build(HomeContentBuilder.build(catalog, progress)!!)
    }

    private fun RoadmapRow.numbers(): List<Int?> = when (this) {
        is RoadmapRow.Tiles -> listOf(left?.number, right?.number)
        is RoadmapRow.Current -> listOf(entry.number)
    }

    @Test fun freshCourseStartsWithTheCurrentCardOfModuleOne() {
        val rows = roadmap()
        assertEquals(listOf(listOf(1), listOf(null, 2), listOf(3, 4), listOf(5, null)), rows.map { it.numbers() })
        assertEquals(ModuleState.Current, (rows[0] as RoadmapRow.Current).entry.state)
    }

    @Test fun currentOddModuleStandsAboveItsRightPartner() {
        // Пройден модуль 1 (урок 1), текущий модуль 2 (чётный): сосед слева стоит над карточкой.
        val rows = roadmap(1)
        assertEquals(listOf(listOf(1, null), listOf(2), listOf(3, 4), listOf(5, null)), rows.map { it.numbers() })
    }

    @Test fun otherTilesDoNotMoveWhenCurrentModuleChanges() {
        fun columns(rows: List<RoadmapRow>): Map<Int, Boolean> = buildMap {
            rows.forEach { row ->
                if (row is RoadmapRow.Tiles) {
                    row.left?.let { put(it.number, true) }
                    row.right?.let { put(it.number, false) }
                }
            }
        }
        val before = columns(roadmap())
        val after = columns(roadmap(1, 2, 3))
        // Модули 4 и 5 остаются в своих колонках (чётный справа, нечётный слева), пока они не стали текущими.
        assertEquals(before[4], after[4])
        assertEquals(before[5], after[5])
    }

    @Test fun onlyTheNearestLockedModuleShowsTheUnlockHint() {
        val rows = roadmap()
        val hints = rows.flatMap { row ->
            when (row) {
                is RoadmapRow.Tiles -> listOfNotNull(row.left, row.right)
                is RoadmapRow.Current -> listOf(row.entry)
            }
        }.filter { it.state == ModuleState.Locked }.map { it.showsUnlockHint }
        assertEquals(listOf(true, false, false, false), hints)
    }

    @Test fun finishedCourseHasNoCurrentCardAndAllModulesAreDone() {
        val rows = roadmap(1, 2, 3, 4, 5, 6, 7)
        assertTrue(rows.none { it is RoadmapRow.Current })
        val states = rows.flatMap { (it as RoadmapRow.Tiles).let { r -> listOfNotNull(r.left, r.right) } }.map { it.state }
        assertTrue(states.all { it == ModuleState.Done })
        assertNull(rows.firstOrNull { it is RoadmapRow.Current })
    }
}
