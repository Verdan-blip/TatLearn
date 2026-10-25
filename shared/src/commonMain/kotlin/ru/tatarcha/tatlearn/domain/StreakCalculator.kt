package ru.tatarcha.tatlearn.domain

/** Чистое правило серии: тот же день сохраняет её, следующий день продлевает, любой пропуск начинает заново с 1. */
object StreakCalculator {
    /**
     * Серия для показа: жива, если последнее занятие было сегодня или вчера. Если пропущен хотя бы один день, показываем 0,
     * хотя в базе лежит старое значение (оно сбросится на 1 при следующем занятии).
     */
    fun current(storedStreak: Int, lastActiveDay: Long?, today: Long): Int = when {
        lastActiveDay == null -> 0
        today - lastActiveDay in 0..1 -> storedStreak
        else -> 0
    }

    fun next(currentStreak: Int, lastActiveDay: Long?, today: Long): Int = when {
        lastActiveDay == null -> 1
        lastActiveDay == today -> maxOf(currentStreak, 1)
        lastActiveDay == today - 1 -> currentStreak + 1
        else -> 1
    }
}
