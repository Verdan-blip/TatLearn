package ru.tatarcha.tatlearn.core.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.rate
import platform.AVFoundation.setRate
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.darwin.NSObjectProtocol

// expect/actual (сторона iOS): AVPlayer для воспроизведения по URL; аудиокатегория «playback» делает звук
// слышимым даже при включённом беззвучном режиме. Проверка: в симуляторе запустить упражнение на аудирование
// с audioUrl (в тестовых данных настоящих URL пока нет). TODO(audio): отслеживать ошибки AVPlayerItem.
actual fun createPlatformAudioPlayer(): AudioPlayer = AvPlayerAudioPlayer()

@OptIn(ExperimentalForeignApi::class)
private class AvPlayerAudioPlayer : AudioPlayer {
    private val _state = MutableStateFlow(AudioPlayerState.Idle)
    override val state: StateFlow<AudioPlayerState> = _state.asStateFlow()
    private var player: AVPlayer? = null
    private var endObserver: NSObjectProtocol? = null

    override fun play(url: String, rate: Float) {
        stop()
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            _state.value = AudioPlayerState.Error
            return
        }
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
        val item = AVPlayerItem(uRL = nsUrl)
        endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = item,
            queue = null,
        ) { _ -> _state.value = AudioPlayerState.Completed }
        val avPlayer = AVPlayer(playerItem = item)
        player = avPlayer
        avPlayer.play()
        avPlayer.setRate(rate)
        _state.value = AudioPlayerState.Playing
    }

    override fun stop() {
        player?.pause()
        player = null
        endObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        endObserver = null
        _state.value = AudioPlayerState.Idle
    }

    override fun release() = stop()
}
