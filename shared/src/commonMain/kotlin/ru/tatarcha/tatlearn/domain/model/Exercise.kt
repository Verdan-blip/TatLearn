package ru.tatarcha.tatlearn.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExerciseType { Matching, Quiz, Listening, SentenceBuilder, Translation }

/** Татарские слова в упражнениях — данные, и их нужно проверить носителем (см. флаги в JSON уроков). */
data class WordPair(val tatar: String, val russian: String)

/** [notes] сопоставляет татарское слово с подсказкой маскота, которая показывается, пока слово выбрано. */
data class MatchingExercise(val pairs: List<WordPair>, val notes: Map<String, String> = emptyMap())

/** [audioUrl] — озвучка слова; пока null, кнопка звука на экране не показывается (TODO(audio)). */
data class QuizQuestion(
    val word: String,
    val transcription: String?,
    val options: List<String>,
    val correctIndex: Int,
    val isNewWord: Boolean,
    val audioUrl: String? = null,
)

/** [audioUrl] равен null, пока нет настоящего аудио; тогда плеер показывает заглушку. */
data class ListeningTask(val audioUrl: String?, val options: List<String>, val correctIndex: Int)

data class SentenceTask(val promptRu: String, val wordBank: List<String>, val answer: List<String>)

data class TranslationTask(val promptRu: String, val acceptedAnswers: List<String>, val audioUrl: String?)
