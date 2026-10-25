package ru.tatarcha.tatlearn.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.ExerciseOutcome
import ru.tatarcha.tatlearn.domain.model.ExerciseResult
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.LessonContent
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.UserProfile
import ru.tatarcha.tatlearn.domain.model.UserSession
import ru.tatarcha.tatlearn.domain.model.UserStats

/** Сбои — это [ru.tatarcha.tatlearn.core.common.AppException] внутри [Result]. */
interface LessonsRepository {
    /** Каталог модулей по порядку; прогресс пользователя сюда не входит (он в [ProgressRepository]). */
    suspend fun getModules(): Result<List<CourseModule>>
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

    /** Только для отладки: стирает статусы уроков и историю заданий (XP и серия остаются). */
    suspend fun resetLessonProgress()

    /** Только для отладки: обнуляет XP и серию. */
    suspend fun resetStats()
}

/** Гостевой профиль на устройстве (имя и аватар); не зависит от сессии входа. */
interface ProfileRepository {
    /** Null, пока знакомство не пройдено. */
    val profile: StateFlow<UserProfile?>

    /** Срабатывает после каждого сохранения профиля (экран профиля показывает «Сохранено»). */
    val saved: SharedFlow<Unit>

    /** Сохраняет имя (уже нормализованное) и аватар; при первом сохранении создаёт локальный идентификатор. */
    fun save(name: String, avatar: Avatar)

    /** Только для отладки (и будущего «Удалить данные»): стирает профиль, знакомство покажется снова. */
    fun clear()
}
