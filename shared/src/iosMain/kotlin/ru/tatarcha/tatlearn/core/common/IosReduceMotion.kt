package ru.tatarcha.tatlearn.core.common

import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

/**
 * expect/actual (сторона iOS): настройка «Уменьшение движения» (Настройки → Универсальный доступ → Движение).
 * Проверка: включить её в симуляторе или на устройстве, пройти урок и вернуться на главную: итог виден сразу,
 * золотое кольцо и «+1 урок» просто держатся около секунды.
 */
class IosReduceMotion : ReduceMotion {
    override fun isEnabled(): Boolean = UIAccessibilityIsReduceMotionEnabled()
}
