package ru.tatarcha.tatlearn.core.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * expect/actual (сторона Android): примитивы поверх `Vibrator`. С API 29 используются готовые эффекты (TICK, CLICK),
 * с API 26 короткий one-shot с амплитудой, ниже просто длительность. С API 33 вибрация помечена как «касание»
 * (`USAGE_TOUCH`), поэтому системная настройка «Вибрация при касании» её отключает.
 * Проверка: запустить на реальном устройстве (эмулятор не вибрирует), пройти шаг урока, включить и выключить «Вибро-отклик» в профиле.
 */
class AndroidHaptics(context: Context) : PlatformHaptics {
    @Suppress("DEPRECATION")
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    override fun tickLight() = vibrate(effectId = VibrationEffect.EFFECT_TICK, fallbackMs = LIGHT_MS, fallbackAmplitude = LIGHT_AMPLITUDE)

    override fun tickMedium() = vibrate(effectId = VibrationEffect.EFFECT_CLICK, fallbackMs = MEDIUM_MS, fallbackAmplitude = MEDIUM_AMPLITUDE)

    override fun success() = vibrate(effectId = VibrationEffect.EFFECT_CLICK, fallbackMs = SUCCESS_MS, fallbackAmplitude = SUCCESS_AMPLITUDE)

    @Suppress("DEPRECATION")
    private fun vibrate(effectId: Int, fallbackMs: Long, fallbackAmplitude: Int) {
        val vibrator = vibrator?.takeIf { it.hasVibrator() } ?: return
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                vibrator.vibrate(VibrationEffect.createPredefined(effectId), VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> vibrator.vibrate(VibrationEffect.createPredefined(effectId))
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> vibrator.vibrate(VibrationEffect.createOneShot(fallbackMs, fallbackAmplitude))
            else -> vibrator.vibrate(fallbackMs)
        }
    }

    private companion object {
        const val LIGHT_MS = 12L
        const val LIGHT_AMPLITUDE = 60
        const val MEDIUM_MS = 20L
        const val MEDIUM_AMPLITUDE = 120
        const val SUCCESS_MS = 30L
        const val SUCCESS_AMPLITUDE = 160
    }
}
