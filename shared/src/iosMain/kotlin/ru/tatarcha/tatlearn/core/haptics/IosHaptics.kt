package ru.tatarcha.tatlearn.core.haptics

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

/**
 * expect/actual (сторона iOS): Taptic Engine через `UIFeedbackGenerator`. Системная настройка «Тактильные сигналы в системе»
 * и режим энергосбережения подавляют отклик сами. Генераторы создаются один раз и «прогреваются» (`prepare`), чтобы не было задержки.
 * Проверка: запустить на реальном iPhone (симулятор не вибрирует), пройти шаг урока, включить и выключить «Вибро-отклик» в профиле.
 */
class IosHaptics : PlatformHaptics {
    private val light = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).also { it.prepare() }
    private val medium = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).also { it.prepare() }
    private val notification = UINotificationFeedbackGenerator().also { it.prepare() }

    override fun tickLight() {
        light.impactOccurred()
        light.prepare()
    }

    override fun tickMedium() {
        medium.impactOccurred()
        medium.prepare()
    }

    override fun success() {
        notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
        notification.prepare()
    }
}
