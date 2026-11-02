package ru.tatarcha.tatlearn.core.sound

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.data.local.SoundEffectsSettings
import tatlearn.shared.generated.resources.Res

/**
 * Платформенный проигрыватель коротких звуков интерфейса (решение 02.11.2026: тихие сигналы на итог шага и конец урока).
 * Отдельно от `AudioPlayer`: не трогает запись слова, уважает беззвучный режим (см. actual-реализации).
 */
interface PlatformSoundPlayer {
    /** Играет готовую запись; новый звук обрывает предыдущий. Ошибки воспроизведения молча игнорируются. */
    fun play(bytes: ByteArray)
}

/**
 * expect/actual: на Android — MediaPlayer с назначением «звук интерфейса», на iOS — AVAudioPlayer с категорией Ambient.
 * Проверка: в отладочных инструментах нажать кнопки «Звук: …» (звук выключается переключателем «Звуки» в профиле).
 */
expect fun createPlatformSoundPlayer(): PlatformSoundPlayer

/** Смысловые звуки приложения; файлы и настройка вкл/выкл спрятаны за интерфейсом. Звуков на тапы нет специально. */
interface SoundEffects {
    /** Верный ответ после проверки шага (в том числе «принят с подсказкой написания»): одна высокая нота колокольчика. */
    fun correct()

    /** Неверный ответ: низкая мягкая нота, не «баззер» (в проекте нет жизней, ошибка не наказывает). */
    fun wrong()

    /** Урок пройден (экран результата): короткое «та-дам». Не вызывается, если урок не засчитан. */
    fun complete()
}

/**
 * Читает записи из `composeResources/files/sounds` (синтезированы скриптом `design/sounds/generate_sounds.py`),
 * кэширует их в памяти и играет через [PlatformSoundPlayer], если звуки включены в профиле ([SoundEffectsSettings]).
 */
class PlayerSoundEffects(
    private val player: PlatformSoundPlayer,
    private val settings: SoundEffectsSettings,
    private val scope: CoroutineScope,
    private val readBytes: suspend (String) -> ByteArray = { Res.readBytes(it) },
) : SoundEffects {
    private val cache = mutableMapOf<String, ByteArray>()

    override fun correct() = play(CORRECT)

    override fun wrong() = play(WRONG)

    override fun complete() = play(COMPLETE)

    private fun play(file: String) {
        if (!settings.enabled.value) return
        scope.launch {
            val bytes = cache[file] ?: try {
                readBytes("files/sounds/$file").also { cache[file] = it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Нет файла или ошибка чтения: звук необязателен, приложение работает без него.
                return@launch
            }
            player.play(bytes)
        }
    }

    internal companion object {
        const val CORRECT = "correct.m4a"
        const val WRONG = "wrong.m4a"
        const val COMPLETE = "complete.m4a"
    }
}
