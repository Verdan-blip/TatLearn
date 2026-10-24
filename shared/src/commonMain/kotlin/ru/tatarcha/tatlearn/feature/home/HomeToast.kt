package ru.tatarcha.tatlearn.feature.home

sealed interface HomeToast {
    /** [hint] — подсказка из данных, как открыть модуль; null, если её нет. */
    data class ModuleLocked(val hint: String?) : HomeToast
    data object ModuleUnavailable : HomeToast
    data object AllModulesUnavailable : HomeToast
    data object SectionUnavailable : HomeToast
}
