package ru.tatarcha.tatlearn.core.audio

import ru.tatarcha.tatlearn.domain.model.NarratorVoice

fun interface NarratorPreview {
    fun play(voice: NarratorVoice)

    companion object {
        val None = NarratorPreview { }
    }
}
