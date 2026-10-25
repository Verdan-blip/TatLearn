package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.CurrentLesson
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
            val lockedBy = requiredModule(modules, index)?.takeIf { !isDone(it, completed) }?.id
            val done = module.lessons.count { it.id in completed }
            LearningModule(
                id = module.id,
                titleTt = module.titleTt,
                titleRu = module.titleRu,
                icon = module.icon,
                progressPercent = if (module.lessons.isEmpty()) 0 else done * PERCENT / module.lessons.size,
                lockedBy = lockedBy,
                lockHint = if (lockedBy != null) module.lockHint else null,
            )
        }

        // Первый открытый модуль с непройденным уроком; если всё пройдено, показываем последний урок (TODO(design): состояние «всё пройдено»).
        val target = modules.zip(learning).firstOrNull { (module, view) ->
            !view.isLocked && module.lessons.any { it.id !in completed }
        } ?: modules.zip(learning).last { (module, _) -> module.lessons.isNotEmpty() }
        val (module, _) = target
        val lesson = module.lessons.firstOrNull { it.id !in completed } ?: module.lessons.last()
        return HomeContent(
            modules = learning,
            currentLesson = CurrentLesson(
                lesson = lesson,
                topicTt = module.titleTt,
                topicRu = module.titleRu,
                completedLessons = module.lessons.count { it.id in completed },
                totalLessons = module.lessons.size,
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
