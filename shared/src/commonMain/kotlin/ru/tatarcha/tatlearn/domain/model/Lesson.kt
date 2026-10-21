package ru.tatarcha.tatlearn.domain.model

data class Lesson(val id: String, val number: Int, val titleTt: String, val titleRu: String)

data class CurrentLesson(
    val lesson: Lesson,
    /** Тема, показываемая на карточке главной, например «Гаилә» / «Семья». */
    val topicTt: String,
    val topicRu: String,
    val completedExercises: Int,
    val totalExercises: Int,
) {
    val progress: Float get() = if (totalExercises == 0) 0f else completedExercises.toFloat() / totalExercises
}

/** [lockedBy] — id модуля, который нужно пройти сначала; null, если модуль открыт. */
data class LearningModule(
    val id: String,
    val titleTt: String,
    val titleRu: String,
    val icon: String,
    val progressPercent: Int,
    val lockedBy: String?,
    val lockHint: String?,
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
