package ru.tatarcha.tatlearn.core.audio

import android.media.AudioAttributes
import android.media.MediaDataSource
import android.media.MediaPlayer
import android.media.PlaybackParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// expect/actual (сторона Android): android.media.MediaPlayer, воспроизведение по URL или из памяти (MediaDataSource,
// им играют файлы из ресурсов приложения, у которых нет пути в файловой системе).
// Проверка: запустить урок с заданием на слух (например, урок 1), нажать на кнопку с динамиком и услышать слово.
actual fun createPlatformAudioPlayer(): AudioPlayer = MediaPlayerAudioPlayer()

private class MediaPlayerAudioPlayer : AudioPlayer {
    private val _state = MutableStateFlow(AudioPlayerState.Idle)
    override val state: StateFlow<AudioPlayerState> = _state.asStateFlow()
    private var player: MediaPlayer? = null

    override fun play(url: String, rate: Float) = start(rate) { setDataSource(url) }

    override fun play(bytes: ByteArray, rate: Float) = start(rate) { setDataSource(BytesMediaDataSource(bytes)) }

    private fun start(rate: Float, setSource: MediaPlayer.() -> Unit) {
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
            mp.setSource()
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

/** Отдаёт плееру запись, которая целиком лежит в памяти. */
private class BytesMediaDataSource(private val bytes: ByteArray) : MediaDataSource() {
    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position >= bytes.size) return -1
        val count = minOf(size, bytes.size - position.toInt())
        System.arraycopy(bytes, position.toInt(), buffer, offset, count)
        return count
    }

    override fun getSize(): Long = bytes.size.toLong()

    override fun close() = Unit
}
