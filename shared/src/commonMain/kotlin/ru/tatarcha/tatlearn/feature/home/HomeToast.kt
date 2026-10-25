package ru.tatarcha.tatlearn.feature.home

sealed interface HomeToast {
    data object AllModulesUnavailable : HomeToast
}
