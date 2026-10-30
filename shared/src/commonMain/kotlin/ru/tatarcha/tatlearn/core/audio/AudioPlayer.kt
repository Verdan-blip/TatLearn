package ru.tatarcha.tatlearn.core.audio

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tatlearn.shared.generated.resources.Res

enum class AudioPlayerState { Idle, Playing, Completed, Error }

interface AudioPlayer {
    val state: StateFlow<AudioPlayerState>

    /** [rate] 1f — обычная скорость, 0.75f — кнопка «помедленнее». */
    fun play(url: String, rate: Float = 1f)

    /** Воспроизводит уже загруженную запись (например, файл из ресурсов приложения). */
    fun play(bytes: ByteArray, rate: Float = 1f)
    fun stop()
    fun release()
}

/**
 * expect/actual: на Android — MediaPlayer, на iOS — AVPlayer (см. androidMain / iosMain).
 * Создаётся на область экрана; по завершении вызовите [release].
 */
expect fun createPlatformAudioPlayer(): AudioPlayer

/**
 * Выбирает источник звука по `audioUrl`: `http(s)://` играет платформенный плеер напрямую, любой другой путь
 * (например, `audio/oy.m4a`) читается из ресурсов приложения `composeResources/files`. Если у задания нет URL,
 * плеер на мгновение изображает воспроизведение, чтобы можно было построить и протестировать ход упражнения.
 * TODO(audio): убрать заглушку, когда запишем все слова.
 */
class StubbingAudioPlayer(private val delegate: AudioPlayer) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var stubJob: Job? = null
    private var loadJob: Job? = null
    private var usingStub = false
    private val stubState = MutableStateFlow(AudioPlayerState.Idle)
    private val _state = MutableStateFlow(AudioPlayerState.Idle)

    /** Состояние заглушки, пока она «играет», иначе — состояние платформенного плеера. */
    val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

    init {
        scope.launch { delegate.state.collect { if (!usingStub) _state.value = it } }
        scope.launch { stubState.collect { if (usingStub) _state.value = it } }
    }

    fun play(url: String?, rate: Float = 1f) {
        stop()
        when {
            url == null -> {
                usingStub = true
                stubJob = scope.launch {
                    stubState.value = AudioPlayerState.Playing
                    delay((STUB_DURATION_MS / rate).toLong())
                    stubState.value = AudioPlayerState.Completed
                }
            }
            url.startsWith("http://") || url.startsWith("https://") -> {
                usingStub = false
                delegate.play(url, rate)
            }
            else -> playResource(url, rate)
        }
    }

    private fun playResource(path: String, rate: Float) {
        usingStub = false
        loadJob = scope.launch {
            val bytes = try {
                Res.readBytes("files/$path")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (bytes == null) {
                // Файла нет в ресурсах: показываем ошибку так же, как при сбое платформенного плеера.
                usingStub = true
                stubState.value = AudioPlayerState.Error
            } else {
                delegate.play(bytes, rate)
            }
        }
    }

    fun stop() {
        stubJob?.cancel()
        loadJob?.cancel()
        stubState.value = AudioPlayerState.Idle
        delegate.stop()
        _state.value = AudioPlayerState.Idle
    }

    fun release() {
        stop()
        delegate.release()
        scope.cancel()
    }

    private companion object {
        const val STUB_DURATION_MS = 1200f
    }
}
