package ru.tatarcha.tatlearn.core.haptics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.common.TimeProvider
import ru.tatarcha.tatlearn.data.local.HapticsSettings

/**
 * Платформенные примитивы вибро-отклика (решение 25.10.2026: тактильный отклик как тихое подтверждение факта).
 * Реализации: Android (`Vibrator`) и iOS (Taptic Engine через `UIFeedbackGenerator`); вызывать на главном потоке.
 * Нет вибромотора, системный отклик выключен, режим энергосбережения: вызовы молча ничего не делают.
 */
interface PlatformHaptics {
    /** Очень лёгкий тик. */
    fun tickLight()

    /** Тик средней силы. */
    fun tickMedium()

    /** Мягкое «подтверждение» (на iOS success-уведомление). */
    fun success()
}

/** Смысловые события приложения; паттерны и настройка вкл/выкл спрятаны за интерфейсом. */
interface Haptics {
    /** Верный ответ после проверки шага (в том числе «принят с подсказкой написания»). */
    fun correct()

    /** Неверный ответ: пара лёгких тиков, заметно мягче и отличается рисунком, а не только силой. Не «error»-паттерн. */
    fun wrong()

    /** Урок пройден (экран результата): короткая нарастающая серия. Не вызывается, если урок не засчитан. */
    fun celebrate()

    /** Открылся новый модуль на главной: один очень лёгкий тик (единственный отклик на главной). */
    fun unlocked()
}

/**
 * Паттерны из примитивов [PlatformHaptics]. Уважает настройку профиля ([HapticsSettings]) и не срабатывает чаще,
 * чем раз в [MIN_INTERVAL_MS], чтобы быстрые события не сливались в «дрожь».
 */
class PatternHaptics(
    private val platform: PlatformHaptics,
    private val settings: HapticsSettings,
    private val scope: CoroutineScope,
    private val time: TimeProvider,
) : Haptics {
    private var lastEventAt = Long.MIN_VALUE

    override fun correct() = play { platform.success() }

    override fun wrong() = play {
        platform.tickLight()
        delay(WRONG_GAP_MS)
        platform.tickLight()
    }

    override fun unlocked() = play { platform.tickLight() }

    override fun celebrate() = play {
        platform.success()
        delay(CELEBRATE_GAP_MS)
        platform.tickLight()
        delay(CELEBRATE_GAP_MS)
        platform.tickMedium()
    }

    private fun play(pattern: suspend () -> Unit) {
        if (!settings.enabled.value) return
        val now = time.nowMillis()
        if (lastEventAt != Long.MIN_VALUE && now - lastEventAt < MIN_INTERVAL_MS) return
        lastEventAt = now
        scope.launch { pattern() }
    }

    internal companion object {
        const val MIN_INTERVAL_MS = 150L
        const val WRONG_GAP_MS = 60L
        const val CELEBRATE_GAP_MS = 90L
    }
}
