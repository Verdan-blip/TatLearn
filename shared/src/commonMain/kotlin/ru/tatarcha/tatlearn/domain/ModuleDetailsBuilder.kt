package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.LessonRowState
import ru.tatarcha.tatlearn.domain.model.ModuleDetails
import ru.tatarcha.tatlearn.domain.model.ModuleLessonRow
import ru.tatarcha.tatlearn.domain.model.RequiredModule

/** Собирает экран модуля из каталога и прогресса; правила открытия те же, что у [HomeContentBuilder]. */
object ModuleDetailsBuilder {

    /** Null, если модуля с таким [moduleId] нет в каталоге. */
    fun build(moduleId: String, modules: List<CourseModule>, progress: List<LessonProgress>): ModuleDetails? {
        val index = modules.indexOfFirst { it.id == moduleId }
        if (index < 0) return null
        val module = modules[index]
        val completed = HomeContentBuilder.completedIds(progress)
        val scores = progress.associate { it.lessonId to it.bestScore }
        val required = HomeContentBuilder.requiredModule(modules, index)?.takeIf { !HomeContentBuilder.isDone(it, completed) }
        val firstOpen = module.lessons.firstOrNull { it.id !in completed }

        val rows = module.lessons.map { lesson ->
            when {
                lesson.id in completed -> ModuleLessonRow(lesson, LessonRowState.Done, scores[lesson.id])
                required != null -> ModuleLessonRow(lesson, LessonRowState.Locked, null)
                lesson == firstOpen -> ModuleLessonRow(lesson, LessonRowState.Next, null)
                else -> ModuleLessonRow(lesson, LessonRowState.Available, null)
            }
        }
        return ModuleDetails(
            module = module,
            lessons = rows,
            completedLessons = rows.count { it.state == LessonRowState.Done },
            lockedBy = required?.let {
                RequiredModule(it.id, it.titleTt, it.titleRu, it.lessons.count { l -> l.id in completed }, it.lessons.size)
            },
            nextLesson = if (required == null) firstOpen else null,
            nextModule = modules.getOrNull(index + 1),
        )
    }
}
