package ru.tatarcha.tatlearn.core.common

import android.content.Context
import android.provider.Settings

/**
 * expect/actual (сторона Android): «Убрать анимации» в настройках доступности задаёт масштаб анимаций 0.
 * Проверка: включить «Убрать анимации» (или в параметрах разработчика поставить масштаб 0), пройти урок и вернуться на главную:
 * итог виден сразу, золотое кольцо и «+1 урок» просто держатся около секунды.
 */
class AndroidReduceMotion(private val context: Context) : ReduceMotion {
    override fun isEnabled(): Boolean =
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}
