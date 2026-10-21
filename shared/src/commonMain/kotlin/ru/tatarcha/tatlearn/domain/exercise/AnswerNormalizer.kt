package ru.tatarcha.tatlearn.domain.exercise

/**
 * Сравнивает введённые татарские ответы.
 *
 * Шаг 1 (нормализация): нижний регистр, единые кавычки/тире, убрать знаки препинания, схлопнуть пробелы.
 * Шаг 2 (свёртка): татарские буквы заменяются буквами русской раскладки, которые люди реально набирают
 * (ә→а, ө→о, ү→у, җ→ж, ң→н, һ→х; также э→а и ё→е).
 *
 * Ответ, совпавший только после свёртки, — [Verdict.Approximate]: принимается, но UI показывает
 * правильное написание. TODO(content): подтвердить с носителем языка, что такие замены — самые частые.
 */
object AnswerNormalizer {

    enum class Verdict { Exact, Approximate, Wrong }

    private val foldMap = mapOf(
        'ә' to 'а', 'э' to 'а',
        'ө' to 'о',
        'ү' to 'у',
        'җ' to 'ж',
        'ң' to 'н',
        'һ' to 'х',
        'ё' to 'е',
    )

    private val punctuation = setOf(
        '.', ',', '!', '?', ';', ':', '"', '\'', '«', '»', '“', '”', '„', '(', ')', '…', '-', '–', '—',
    )

    fun normalize(input: String): String {
        val cleaned = input.lowercase().filter { it !in punctuation }
        return cleaned.split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ")
    }

    fun fold(normalized: String): String = normalized.map { foldMap[it] ?: it }.joinToString("")

    fun check(input: String, acceptedAnswers: List<String>): Verdict {
        val typed = normalize(input)
        if (typed.isEmpty()) return Verdict.Wrong
        val accepted = acceptedAnswers.map(::normalize)
        if (typed in accepted) return Verdict.Exact
        val typedFolded = fold(typed)
        return if (accepted.any { fold(it) == typedFolded }) Verdict.Approximate else Verdict.Wrong
    }
}
