package ru.tatarcha.tatlearn.domain.exercise

/** Когда урок считается пройденным. Правила временные: порог может прийти с сервера. */
object PassRules {
    /**
     * Урок пройден при точности не ниже порога. Повтор ошибок засчитывает урок, если все ошибочные
     * задания исправлены (предположение методиста, нужно подтвердить).
     */
    fun isPassed(accuracyPercent: Int, isMistakesRun: Boolean, mistakes: Int): Boolean =
        accuracyPercent >= XpRules.PASS_THRESHOLD_PERCENT || (isMistakesRun && mistakes == 0)

    /** Сколько верных ответов не хватило до порога в обычном прохождении урока. */
    fun missingAnswers(correct: Int, total: Int): Int {
        val needed = (XpRules.PASS_THRESHOLD_PERCENT * total + PERCENT - 1) / PERCENT
        return (needed - correct).coerceAtLeast(0)
    }

    private const val PERCENT = 100
}
