package ru.tatarcha.tatlearn.domain.model

/**
 * Голос диктора, которым звучат слова и фразы уроков (выбор в профиле, по умолчанию мужской).
 * Мужской: пути из уроков как есть (записи автора и синтез tat_marat). Женский: копии синтеза tat_albina в `audio/female`,
 * для всех слов уроков, в том числе тех, что у мужского голоса записаны автором (см. design/tts/silero_build.py).
 */
enum class NarratorVoice {
    Male,
    Female,
    ;

    /** Путь озвучки для этого голоса; сетевые адреса и пути вне `audio/` не меняются. */
    fun resolve(audioUrl: String): String =
        if (this == Female && audioUrl.startsWith(AUDIO_PREFIX)) FEMALE_PREFIX + audioUrl.removePrefix(AUDIO_PREFIX) else audioUrl

    private companion object {
        const val AUDIO_PREFIX = "audio/"
        const val FEMALE_PREFIX = "audio/female/"
    }
}
