package ru.tatarcha.tatlearn.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.tatarcha.tatlearn.domain.model.ExerciseOutcome
import ru.tatarcha.tatlearn.domain.model.ExerciseResult
import ru.tatarcha.tatlearn.domain.model.HomeContent
import ru.tatarcha.tatlearn.domain.model.LessonContent
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.UserSession
import ru.tatarcha.tatlearn.domain.model.UserStats

/** Сбои — это [ru.tatarcha.tatlearn.core.common.AppException] внутри [Result]. */
interface LessonsRepository {
    suspend fun getHome(): Result<HomeContent>
    suspend fun getLessonContent(lessonId: String): Result<LessonContent>
}

interface AuthRepository {
    /** Сохранённая сессия; null, если пользователь вышел. */
    val session: StateFlow<UserSession?>
    suspend fun signIn(email: String, password: String): Result<UserSession>
    suspend fun signUp(email: String, password: String): Result<UserSession>
    fun signOut()
}

/** Только локальный прогресс; поверх SQLDelight. */
interface ProgressRepository {
    fun observeStats(): Flow<UserStats>
    fun observeLessonProgress(): Flow<List<LessonProgress>>

    /** Сохраняет одно завершённое упражнение, добавляет XP, обновляет серию и статус урока. */
    suspend fun recordExerciseResult(result: ExerciseResult): ExerciseOutcome

    /** Отмечает урок пройденным, сохраняя лучший результат. */
    suspend fun completeLesson(lessonId: String, scorePercent: Int)
}
