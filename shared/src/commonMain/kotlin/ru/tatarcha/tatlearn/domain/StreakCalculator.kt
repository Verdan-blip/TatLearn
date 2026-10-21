package ru.tatarcha.tatlearn.domain

/** Чистое правило серии: тот же день сохраняет её, следующий день продлевает, любой пропуск начинает заново с 1. */
object StreakCalculator {
    fun next(currentStreak: Int, lastActiveDay: Long?, today: Long): Int = when {
        lastActiveDay == null -> 1
        lastActiveDay == today -> maxOf(currentStreak, 1)
        lastActiveDay == today - 1 -> currentStreak + 1
        else -> 1
    }
}
