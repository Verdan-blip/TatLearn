package ru.tatarcha.tatlearn.domain.model

data class UserSession(val name: String, val email: String)

enum class LessonStatus { NotStarted, InProgress, Completed }

data class LessonProgress(val lessonId: String, val status: LessonStatus, val bestScore: Int)

data class UserStats(val xp: Int, val streak: Int) {
    val level: Int get() = xp / XP_PER_LEVEL + 1
    val xpToNextLevel: Int get() = XP_PER_LEVEL - xp % XP_PER_LEVEL

    companion object {
        const val XP_PER_LEVEL = 100
        val Empty = UserStats(xp = 0, streak = 0)
    }
}

data class ExerciseResult(
    val lessonId: String,
    val type: ExerciseType,
    val correct: Int,
    val total: Int,
) {
    val accuracyPercent: Int get() = if (total == 0) 0 else correct * 100 / total
}

data class ExerciseOutcome(val xpEarned: Int, val stats: UserStats)
