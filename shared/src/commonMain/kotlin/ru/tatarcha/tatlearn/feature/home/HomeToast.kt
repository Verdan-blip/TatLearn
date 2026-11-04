package ru.tatarcha.tatlearn.feature.home

/** Уведомление главной. [ModuleLocked]: тап по закрытому модулю называет модуль, после которого он откроется. */
sealed interface HomeToast {
    data class ModuleLocked(val afterTitle: String) : HomeToast
}
