package ru.tatarcha.tatlearn.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.tatarcha.tatlearn.feature.auth.AuthMode

/** Типизированные ключи Navigation 3. Стек навигации — список этих ключей. */
@Serializable data object OnboardingKey : NavKey
@Serializable data class AuthKey(val mode: AuthMode = AuthMode.SignIn) : NavKey
@Serializable data object HomeKey : NavKey
/** [stepIds] ограничивает урок этими шагами («повторить ошибки»); пусто — весь урок. */
@Serializable data class LessonKey(val lessonId: String, val stepIds: List<String> = emptyList()) : NavKey

@Serializable
data class ResultKey(
    val lessonId: String,
    val xpEarned: Int,
    val accuracyPercent: Int,
    val durationSeconds: Int,
    val mistakeStepIds: List<String>,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    /** Это повтор ошибок (урок запущен с `stepIds`). */
    val isMistakesRun: Boolean = false,
    /** Серию продлили в этом уроке (первое занятие за сегодня при вчерашней серии): только тогда показывается «Стрик продлён». */
    val streakExtended: Boolean = false,
) : NavKey

/** Каталог дизайн-системы только для отладки. */
@Serializable data object ShowcaseKey : NavKey
