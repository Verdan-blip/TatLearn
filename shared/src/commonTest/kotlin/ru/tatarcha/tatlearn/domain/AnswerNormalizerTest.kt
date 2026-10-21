package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.exercise.AnswerNormalizer
import ru.tatarcha.tatlearn.domain.exercise.AnswerNormalizer.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals

class AnswerNormalizerTest {
    private val accepted = listOf("Хәерле иртә")

    @Test fun exactMatchIgnoresCaseSpacesAndPunctuation() {
        assertEquals(Verdict.Exact, AnswerNormalizer.check("  хәерле   ИРТӘ! ", accepted))
        assertEquals(Verdict.Exact, AnswerNormalizer.check("«Хәерле иртә».", accepted))
    }

    @Test fun russianKeyboardSubstitutionsAreApproximate() {
        assertEquals(Verdict.Approximate, AnswerNormalizer.check("хаерле ирта", accepted))
        assertEquals(Verdict.Approximate, AnswerNormalizer.check("Хэерле иртэ", accepted))
    }

    @Test fun allSpecialLettersFold() {
        assertEquals("аоужнх", AnswerNormalizer.fold("әөүҗңһ"))
        assertEquals(Verdict.Approximate, AnswerNormalizer.check("ужнх", listOf("үҗңһ")))
    }

    @Test fun differentWordsAreWrong() {
        assertEquals(Verdict.Wrong, AnswerNormalizer.check("Хәерле кич", accepted))
        assertEquals(Verdict.Wrong, AnswerNormalizer.check("", accepted))
        assertEquals(Verdict.Wrong, AnswerNormalizer.check("   ", accepted))
    }

    @Test fun anyAcceptedAnswerCounts() {
        assertEquals(Verdict.Exact, AnswerNormalizer.check("исәнмесез", listOf("Исәнмесез", "Сәлам")))
        assertEquals(Verdict.Exact, AnswerNormalizer.check("сәлам", listOf("Исәнмесез", "Сәлам")))
    }
}
