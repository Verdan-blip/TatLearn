package ru.tatarcha.tatlearn.core.sound

import android.media.AudioAttributes
import android.media.MediaPlayer
import ru.tatarcha.tatlearn.core.audio.BytesMediaDataSource

// expect/actual (сторона Android): android.media.MediaPlayer с назначением USAGE_ASSISTANCE_SONIFICATION, оно идёт
// по системному потоку звуков интерфейса и замолкает в беззвучном режиме и режиме вибрации.
// Проверка: в отладочных инструментах нажать «Звук: верный ответ» (громкость системных звуков не на нуле).
actual fun createPlatformSoundPlayer(): PlatformSoundPlayer = MediaPlayerSoundPlayer()

private class MediaPlayerSoundPlayer : PlatformSoundPlayer {
    private var player: MediaPlayer? = null

    override fun play(bytes: ByteArray) {
        release()
        val mp = MediaPlayer()
        player = mp
        try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .build(),
            )
            mp.setDataSource(BytesMediaDataSource(bytes))
            mp.setOnPreparedListener { it.start() }
            mp.setOnCompletionListener { release() }
            mp.setOnErrorListener { _, _, _ ->
                release()
                true
            }
            mp.prepareAsync()
        } catch (e: Exception) {
            release()
        }
    }

    private fun release() {
        player?.release()
        player = null
    }
}
