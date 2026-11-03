package ru.tatarcha.tatlearn.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExerciseType { Matching, Quiz, Listening, SentenceBuilder, Translation }

/**
 * Татарские слова в упражнениях — данные, и их нужно проверить носителем (см. флаги в JSON уроков).
 * [audioUrl] — озвучка слова, играет при выборе татарской карточки; null — карточка молчит (звук не изображаем).
 */
data class WordPair(val tatar: String, val russian: String, val audioUrl: String? = null)

/** [notes] сопоставляет татарское слово с подсказкой маскота, которая показывается, пока слово выбрано. */
data class MatchingExercise(val pairs: List<WordPair>, val notes: Map<String, String> = emptyMap())

/** [audioUrl] — озвучка слова; пока null, кнопка звука на экране не показывается (TODO(audio)). [explanation] показывается после ошибки. */
data class QuizQuestion(
    val word: String,
    val transcription: String?,
    val options: List<String>,
    val correctIndex: Int,
    val isNewWord: Boolean,
    val audioUrl: String? = null,
    val explanation: String? = null,
)

/** [audioUrl] равен null, пока нет настоящего аудио; тогда плеер показывает заглушку. */
data class ListeningTask(val audioUrl: String?, val options: List<String>, val correctIndex: Int, val explanation: String? = null)

/**
 * [wordAudio] — озвучка слов банка (слово -> путь), играет при тапе слова из банка; слова без записи молчат.
 * [audioUrl] — озвучка предложения целиком (ответа).
 */
data class SentenceTask(
    val promptRu: String,
    val wordBank: List<String>,
    val answer: List<String>,
    val explanation: String? = null,
    val wordAudio: Map<String, String> = emptyMap(),
    val audioUrl: String? = null,
)

data class TranslationTask(val promptRu: String, val acceptedAnswers: List<String>, val audioUrl: String?, val explanation: String? = null)
