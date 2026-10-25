package ru.tatarcha.tatlearn.data.mapper

import ru.tatarcha.tatlearn.data.remote.dto.LessonContentDto
import ru.tatarcha.tatlearn.data.remote.dto.LessonDto
import ru.tatarcha.tatlearn.data.remote.dto.ModuleDto
import ru.tatarcha.tatlearn.data.remote.dto.SessionDto
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.LessonContent
import ru.tatarcha.tatlearn.domain.model.ListeningTask
import ru.tatarcha.tatlearn.domain.model.MatchingExercise
import ru.tatarcha.tatlearn.domain.model.QuizQuestion
import ru.tatarcha.tatlearn.domain.model.SentenceTask
import ru.tatarcha.tatlearn.domain.model.TranslationTask
import ru.tatarcha.tatlearn.domain.model.UserSession
import ru.tatarcha.tatlearn.domain.model.WordPair

fun LessonDto.toDomain() = Lesson(id, number, titleTt, titleRu)

fun ModuleDto.toDomain() = CourseModule(id, titleTt, titleRu, icon, lessons.map { it.toDomain() }, requires, lockHint)

fun LessonContentDto.toDomain() = LessonContent(
    lesson = lesson.toDomain(),
    matching = MatchingExercise(matching.pairs.map { WordPair(it.tatar, it.russian) }, matching.notes),
    quiz = quiz.map { QuizQuestion(it.word, it.transcription, it.options, it.correctIndex, it.isNewWord) },
    listening = listening.map { ListeningTask(it.audioUrl, it.options, it.correctIndex) },
    sentences = sentences.map { SentenceTask(it.promptRu, it.wordBank, it.answer) },
    translations = translations.map { TranslationTask(it.promptRu, it.acceptedAnswers, it.audioUrl) },
)

fun SessionDto.toDomain() = UserSession(name, email)
