package ru.tatarcha.tatlearn.core.sound

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.Foundation.NSData
import platform.Foundation.create

// expect/actual (сторона iOS): AVAudioPlayer из памяти; категория Ambient замолкает от кнопки «беззвучно»
// и не прерывает чужую музыку (в отличие от Playback, которым играют слова).
// Проверка: в симуляторе или на устройстве нажать «Звук: верный ответ» в отладочных инструментах; с включённым
// беззвучным режимом звука быть не должно. Симулятор переключатель беззвучного режима не имитирует: проверять на устройстве.
actual fun createPlatformSoundPlayer(): PlatformSoundPlayer = AvAudioSoundPlayer()

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class AvAudioSoundPlayer : PlatformSoundPlayer {
    // Держим ссылку: AVAudioPlayer перестаёт играть, если его освободить.
    private var player: AVAudioPlayer? = null

    override fun play(bytes: ByteArray) {
        if (bytes.isEmpty()) return
        player?.stop()
        val data = bytes.usePinned { pinned -> NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong()) }
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryAmbient, null)
        player = AVAudioPlayer(data = data, error = null).also {
            it.prepareToPlay()
            it.play()
        }
    }
}
