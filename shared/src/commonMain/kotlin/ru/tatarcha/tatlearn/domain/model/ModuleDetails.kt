package ru.tatarcha.tatlearn.domain.model

/** Состояние строки урока на экране модуля. */
enum class LessonRowState {
    /** Пройден (статус Completed). */
    Done,

    /** Рекомендуемый следующий: первый непройденный урок открытого модуля. */
    Next,

    /** Открыт, но не рекомендован: уроки открытого модуля доступны в любом порядке. */
    Available,

    /** Модуль закрыт, урок можно только посмотреть. */
    Locked,
}

/** [bestScore] — лучший результат пройденного урока в процентах; null у непройденных. */
data class ModuleLessonRow(val lesson: Lesson, val state: LessonRowState, val bestScore: Int?)

/** Модуль, который нужно пройти, чтобы открыть текущий. */
data class RequiredModule(val id: String, val titleTt: String, val titleRu: String, val completedLessons: Int, val totalLessons: Int)

/**
 * Экран модуля: уроки со статусами из локального прогресса.
 * [lockedBy] не null, если модуль закрыт. [nextLesson] — рекомендуемый урок (null у закрытого и пройденного).
 * [nextModule] — следующий по порядку модуль, предлагается, когда этот пройден целиком.
 */
data class ModuleDetails(
    val module: CourseModule,
    val lessons: List<ModuleLessonRow>,
    val completedLessons: Int,
    val lockedBy: RequiredModule?,
    val nextLesson: Lesson?,
    val nextModule: CourseModule?,
) {
    val isLocked: Boolean get() = lockedBy != null
    val isCompleted: Boolean get() = lessons.isNotEmpty() && completedLessons == lessons.size
    val progress: Float get() = if (lessons.isEmpty()) 0f else completedLessons.toFloat() / lessons.size
}
