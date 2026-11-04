package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.CurrentLesson
import ru.tatarcha.tatlearn.domain.model.HomeCardKind
import ru.tatarcha.tatlearn.domain.model.HomeContent
import ru.tatarcha.tatlearn.domain.model.LearningModule
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.LessonStatus

/**
 * Собирает главную из каталога модулей (сервер) и локального прогресса (SQLDelight).
 * Правила (решения 24.10.2026): урок пройден при статусе `Completed`; прогресс модуля — доля пройденных уроков;
 * модуль закрыт, пока не пройдены все уроки модуля из `requires` (по умолчанию предыдущего в списке).
 */
object HomeContentBuilder {

    /** Null, если каталог пуст: показывать на главной нечего. */
    fun build(modules: List<CourseModule>, progress: List<LessonProgress>): HomeContent? {
        if (modules.none { it.lessons.isNotEmpty() }) return null
        val completed = completedIds(progress)

        val learning = modules.mapIndexed { index, module ->
            val required = requiredModule(modules, index)?.takeIf { !isDone(it, completed) }
            val done = module.lessons.count { it.id in completed }
            LearningModule(
                id = module.id,
                titleTt = module.titleTt,
                titleRu = module.titleRu,
                icon = module.icon,
                progressPercent = if (module.lessons.isEmpty()) 0 else done * PERCENT / module.lessons.size,
                lockedBy = required?.id,
                lockedByTitleTt = required?.titleTt,
                lockedByTitleRu = required?.titleRu,
                completedLessons = done,
                totalLessons = module.lessons.size,
            )
        }

        // Первый открытый модуль с непройденным уроком; если всё пройдено, показываем последний урок («Повторить урок N»).
        val open = modules.zip(learning).firstOrNull { (module, view) ->
            !view.isLocked && module.lessons.any { it.id !in completed }
        }
        val allDone = open == null
        val (module, _) = open ?: modules.zip(learning).last { (module, _) -> module.lessons.isNotEmpty() }
        val lesson = module.lessons.firstOrNull { it.id !in completed } ?: module.lessons.last()
        val started = progress.any { it.status == LessonStatus.InProgress && it.lessonId == lesson.id }
        // Новичок: ни одного пройденного и ни одного начатого урока. Тогда «Продолжить» нечего обещать.
        val newcomer = progress.none { it.status != LessonStatus.NotStarted }
        val moduleIndex = modules.indexOf(module)
        val kind = when {
            allDone -> HomeCardKind.AllDone
            newcomer -> HomeCardKind.Start
            // Первый урок следующего модуля, в котором ничего ещё не пройдено.
            moduleIndex > 0 && !started && lesson == module.lessons.first() && module.lessons.none { it.id in completed } -> HomeCardKind.NewModule
            else -> HomeCardKind.Continue
        }
        return HomeContent(
            modules = learning,
            currentLesson = CurrentLesson(
                lesson = lesson,
                topicTt = module.titleTt,
                topicRu = module.titleRu,
                completedLessons = module.lessons.count { it.id in completed },
                totalLessons = module.lessons.size,
                kind = kind,
                restartsLesson = started && kind == HomeCardKind.Continue,
                moduleId = module.id,
            ),
        )
    }

    internal fun completedIds(progress: List<LessonProgress>): Set<String> =
        progress.filter { it.status == LessonStatus.Completed }.map { it.lessonId }.toSet()

    internal fun isDone(module: CourseModule, completed: Set<String>): Boolean =
        module.lessons.isNotEmpty() && module.lessons.all { it.id in completed }

    /** Модуль, пройдя который открывается модуль [index]: `requires` или предыдущий в списке (у первого его нет). */
    internal fun requiredModule(modules: List<CourseModule>, index: Int): CourseModule? =
        modules[index].requires?.let { id -> modules.firstOrNull { it.id == id } } ?: modules.getOrNull(index - 1)

    private const val PERCENT = 100
}
