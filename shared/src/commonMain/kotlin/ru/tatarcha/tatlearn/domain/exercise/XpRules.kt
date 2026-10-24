package ru.tatarcha.tatlearn.domain.exercise

object XpRules {
    /** XP за каждый верно решённый шаг. */
    const val PER_CORRECT_STEP = 3
    /** Урок пройден, когда доля верных ответов достигла этого значения (временное значение, может прийти с сервера). */
    const val PASS_THRESHOLD_PERCENT = 80
}
