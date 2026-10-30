package ru.tatarcha.tatlearn.domain.model

data class Lesson(val id: String, val number: Int, val titleTt: String, val titleRu: String)

/**
 * Модуль курса из каталога (то, что приходит с сервера): уроки по порядку, без прогресса пользователя.
 * [requires] — id модуля, который нужно пройти сначала; null — предыдущий модуль в списке (у первого условий нет).
 */
data class CourseModule(
    val id: String,
    val titleTt: String,
    val titleRu: String,
    val icon: String,
    val lessons: List<Lesson>,
    val requires: String? = null,
)

/** Название модуля так, как оно показывается на плитке: «Ашлар · Еда» (если названия совпадают, одно). */
fun moduleTitle(titleTt: String, titleRu: String): String = if (titleTt == titleRu) titleTt else "$titleTt · $titleRu"


/** Что показывает главная карточка: старт (новичок), продолжение, начало нового модуля или «всё пройдено». */
enum class HomeCardKind { Start, Continue, NewModule, AllDone }

/**
 * Главная карточка: урок, модуль-тема и прогресс по урокам этого модуля.
 * [restartsLesson] — урок был начат, но не пройден; шаги внутри урока не сохраняются, он пойдёт с начала.
 */
data class CurrentLesson(
    val lesson: Lesson,
    /** Название модуля, показываемое на карточке главной, например «Ашлар» / «Еда». */
    val topicTt: String,
    val topicRu: String,
    val completedLessons: Int,
    val totalLessons: Int,
    val kind: HomeCardKind = HomeCardKind.Continue,
    val restartsLesson: Boolean = false,
    /** Модуль, о котором карточка; по нему видно, относится ли прирост прогресса к карточке. */
    val moduleId: String = "",
) {
    val progress: Float get() = if (totalLessons == 0) 0f else completedLessons.toFloat() / totalLessons
}

/**
 * Модуль на главной с прогрессом пользователя. [lockedBy] — id модуля, который нужно пройти сначала; null, если модуль открыт.
 * [lockedByTitleTt] и [lockedByTitleRu] — его названия для подсказки «Откроется после модуля …».
 */
data class LearningModule(
    val id: String,
    val titleTt: String,
    val titleRu: String,
    val icon: String,
    val progressPercent: Int,
    val lockedBy: String?,
    val lockedByTitleTt: String?,
    val lockedByTitleRu: String?,
) {
    val isLocked: Boolean get() = lockedBy != null
}

data class HomeContent(val modules: List<LearningModule>, val currentLesson: CurrentLesson)

data class LessonContent(
    val lesson: Lesson,
    val matching: MatchingExercise,
    val quiz: List<QuizQuestion>,
    val listening: List<ListeningTask>,
    val sentences: List<SentenceTask>,
    val translations: List<TranslationTask>,
)
