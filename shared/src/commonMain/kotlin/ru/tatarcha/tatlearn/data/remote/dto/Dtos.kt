package ru.tatarcha.tatlearn.data.remote.dto

import kotlinx.serialization.Serializable

// Фейковые JSON-файлы в composeResources/files имеют в точности форму ответов API,
// поэтому переход на настоящий бэкенд не меняет мапперы и доменный код.

@Serializable
data class LessonDto(val id: String, val number: Int, val titleTt: String, val titleRu: String)

@Serializable
data class ModuleDto(
    val id: String,
    val titleTt: String,
    val titleRu: String,
    val icon: String,
    /** Уроки модуля по порядку (id, номер, названия); сами задания приходят отдельным запросом. */
    val lessons: List<LessonDto>,
    /** id модуля, который нужно пройти сначала; без него — предыдущий в списке. */
    val requires: String? = null,
    /** Текст от сервера, например «Откроется после «Еды»» (грамматику на клиенте не вывести). */
    val lockHint: String? = null,
)

@Serializable
data class WordPairDto(val tatar: String, val russian: String)

@Serializable
data class MatchingDto(val pairs: List<WordPairDto>, val notes: Map<String, String> = emptyMap())

@Serializable
data class QuizQuestionDto(
    val word: String,
    val transcription: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val isNewWord: Boolean = false,
    val audioUrl: String? = null,
    val explanation: String? = null,
)

@Serializable
data class ListeningTaskDto(
    val audioUrl: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null,
)

@Serializable
data class SentenceTaskDto(
    val promptRu: String,
    val wordBank: List<String>,
    val answer: List<String>,
    val explanation: String? = null,
)

@Serializable
data class TranslationTaskDto(
    val promptRu: String,
    val acceptedAnswers: List<String>,
    val audioUrl: String? = null,
    val explanation: String? = null,
)

@Serializable
data class LessonContentDto(
    val lesson: LessonDto,
    /** Контент написан без носителя языка; перед релизом его нужно проверить. */
    val needsNativeReview: Boolean = true,
    val matching: MatchingDto,
    val quiz: List<QuizQuestionDto>,
    val listening: List<ListeningTaskDto>,
    val sentences: List<SentenceTaskDto>,
    val translations: List<TranslationTaskDto>,
)

@Serializable
data class CredentialsDto(val email: String, val password: String)

@Serializable
data class SessionDto(val name: String, val email: String, val token: String? = null)
