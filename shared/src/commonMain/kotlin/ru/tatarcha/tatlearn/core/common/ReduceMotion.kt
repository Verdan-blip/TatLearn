package ru.tatarcha.tatlearn.core.common

/**
 * Включена ли системная настройка «уменьшить анимацию». Реализации: Android (масштаб анимаций 0, «Убрать анимации»),
 * iOS (`UIAccessibilityIsReduceMotionEnabled`).
 */
interface ReduceMotion {
    fun isEnabled(): Boolean
}
