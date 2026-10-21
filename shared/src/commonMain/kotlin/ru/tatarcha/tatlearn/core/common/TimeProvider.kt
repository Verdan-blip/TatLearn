package ru.tatarcha.tatlearn.core.common

import kotlin.time.Clock

/** Подменяемые часы, чтобы логику серии можно было тестировать. */
interface TimeProvider {
    fun nowMillis(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}

private const val MILLIS_PER_DAY = 86_400_000L

/** День от эпохи в UTC. Поэтому дни серии переключаются в полночь UTC (TODO: использовать часовой пояс пользователя). */
fun TimeProvider.epochDay(): Long = nowMillis() / MILLIS_PER_DAY
