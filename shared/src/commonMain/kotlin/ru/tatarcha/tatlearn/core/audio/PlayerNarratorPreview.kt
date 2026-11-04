package ru.tatarcha.tatlearn.core.audio

import ru.tatarcha.tatlearn.domain.model.NarratorVoice

class PlayerNarratorPreview(private val player: StubbingAudioPlayer) : NarratorPreview {
    override fun play(voice: NarratorVoice) = player.play(voice.resolve(SAMPLE_AUDIO))

    private companion object {
        const val SAMPLE_AUDIO = "audio/isenmesez.m4a"
    }
}
