package ru.tatarcha.tatlearn.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.tatarcha.tatlearn.core.common.TimeProvider
import ru.tatarcha.tatlearn.core.common.epochDay
import ru.tatarcha.tatlearn.core.database.TamyrDatabase
import ru.tatarcha.tatlearn.domain.StreakCalculator
import ru.tatarcha.tatlearn.domain.exercise.XpRules
import ru.tatarcha.tatlearn.domain.model.ExerciseOutcome
import ru.tatarcha.tatlearn.domain.model.ExerciseResult
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.LessonStatus
import ru.tatarcha.tatlearn.domain.model.UserStats
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository

class SqlDelightProgressRepository(
    database: TamyrDatabase,
    private val time: TimeProvider,
) : ProgressRepository {

    private val queries = database.progressQueries

    override fun observeStats(): Flow<UserStats> =
        queries.selectStats().asFlow().mapToOneOrNull(Dispatchers.Default)
            .map { row -> row?.let { UserStats(it.xp.toInt(), it.streak.toInt()) } ?: UserStats.Empty }

    override fun observeLessonProgress(): Flow<List<LessonProgress>> =
        queries.selectLessonProgress().asFlow().mapToList(Dispatchers.Default)
            .map { rows -> rows.map { LessonProgress(it.lessonId, it.status.toStatus(), it.bestScore.toInt()) } }

    override suspend fun recordExerciseResult(result: ExerciseResult): ExerciseOutcome =
        withContext(Dispatchers.Default) {
            val xpEarned = result.correct * XP_PER_CORRECT
            val now = time.nowMillis()
            val today = time.epochDay()
            var stats = UserStats.Empty
            queries.transaction {
                queries.insertCompletedExercise(
                    result.lessonId, result.type.name, result.correct.toLong(), result.total.toLong(), xpEarned.toLong(), now,
                )
                val old = queries.selectStats().executeAsOneOrNull()
                val streak = StreakCalculator.next(old?.streak?.toInt() ?: 0, old?.lastActiveDay, today)
                stats = UserStats(xp = (old?.xp?.toInt() ?: 0) + xpEarned, streak = streak)
                queries.upsertStats(stats.xp.toLong(), stats.streak.toLong(), today)

                val lesson = queries.selectLessonProgressById(result.lessonId).executeAsOneOrNull()
                if (lesson == null || lesson.status == LessonStatus.NotStarted.name) {
                    queries.upsertLessonProgress(result.lessonId, LessonStatus.InProgress.name, lesson?.bestScore ?: 0, now)
                }
            }
            ExerciseOutcome(xpEarned, stats)
        }

    override suspend fun completeLesson(lessonId: String, scorePercent: Int) {
        withContext(Dispatchers.Default) {
            val best = queries.selectLessonProgressById(lessonId).executeAsOneOrNull()?.bestScore ?: 0L
            queries.upsertLessonProgress(lessonId, LessonStatus.Completed.name, maxOf(best, scorePercent.toLong()), time.nowMillis())
        }
    }

    private fun String.toStatus() = LessonStatus.entries.firstOrNull { it.name == this } ?: LessonStatus.NotStarted

    private companion object {
        const val XP_PER_CORRECT = XpRules.PER_CORRECT_STEP
    }
}
