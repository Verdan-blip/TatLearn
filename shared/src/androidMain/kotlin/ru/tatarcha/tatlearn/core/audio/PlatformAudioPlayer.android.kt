package ru.tatarcha.tatlearn.core.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// expect/actual (сторона Android): android.media.MediaPlayer, потоковое воспроизведение по URL.
// Проверка: запустить упражнение на аудирование с audioUrl (в тестовых данных настоящих URL пока нет).
actual fun createPlatformAudioPlayer(): AudioPlayer = MediaPlayerAudioPlayer()

private class MediaPlayerAudioPlayer : AudioPlayer {
    private val _state = MutableStateFlow(AudioPlayerState.Idle)
    override val state: StateFlow<AudioPlayerState> = _state.asStateFlow()
    private var player: MediaPlayer? = null

    override fun play(url: String, rate: Float) {
        stop()
        val mp = MediaPlayer()
        player = mp
        try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build(),
            )
            mp.setDataSource(url)
            mp.setOnPreparedListener {
                runCatching { it.playbackParams = PlaybackParams().setSpeed(rate) }
                it.start()
                _state.value = AudioPlayerState.Playing
            }
            mp.setOnCompletionListener { _state.value = AudioPlayerState.Completed }
            mp.setOnErrorListener { _, _, _ ->
                _state.value = AudioPlayerState.Error
                true
            }
            mp.prepareAsync()
        } catch (e: Exception) {
            _state.value = AudioPlayerState.Error
        }
    }

    override fun stop() {
        player?.let { runCatching { it.stop() }; it.release() }
        player = null
        _state.value = AudioPlayerState.Idle
    }

    override fun release() = stop()
}
