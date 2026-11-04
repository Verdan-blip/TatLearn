package ru.tatarcha.tatlearn.feature

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import ru.tatarcha.tatlearn.core.haptics.Haptics
import ru.tatarcha.tatlearn.core.haptics.PlatformHaptics
import ru.tatarcha.tatlearn.core.notifications.PlatformReminders
import ru.tatarcha.tatlearn.core.sound.PlatformSoundPlayer
import ru.tatarcha.tatlearn.core.sound.SoundEffects
import ru.tatarcha.tatlearn.core.notifications.ReminderTexts
import ru.tatarcha.tatlearn.core.notifications.Reminders
import ru.tatarcha.tatlearn.data.local.ReminderSettings
import com.russhwolf.settings.MapSettings
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.UserProfile
import ru.tatarcha.tatlearn.domain.model.UserSession
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.ProfileRepository

internal class FakeProfile(initial: UserProfile? = null) : ProfileRepository {
    private val state = MutableStateFlow(initial)
    override val profile: StateFlow<UserProfile?> = state
    private val savedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val saved: SharedFlow<Unit> = savedFlow
    val saves = mutableListOf<Pair<String, Avatar>>()

    override fun save(name: String, avatar: Avatar) {
        saves += name to avatar
        state.value = UserProfile(state.value?.id ?: "id", name, avatar)
        savedFlow.tryEmit(Unit)
    }

    override fun clear() { state.value = null }
}

/** Репозиторий входа для тестов профиля: хранит сессию, вход и регистрация не нужны. */
internal class SessionOnlyAuth(initial: UserSession? = null) : AuthRepository {
    private val state = MutableStateFlow(initial)
    override val session: StateFlow<UserSession?> = state
    override suspend fun signIn(email: String, password: String): Result<UserSession> = error("unused")
    override suspend fun signUp(email: String, password: String): Result<UserSession> = error("unused")
    override fun signOut() { state.value = null }
}

/** Записывает смысловые вибро-события вместо вибрации. */
internal class RecordingHaptics : Haptics {
    val events = mutableListOf<String>()
    override fun correct() { events += "correct" }
    override fun wrong() { events += "wrong" }
    override fun celebrate() { events += "celebrate" }
    override fun unlocked() { events += "unlocked" }
}

/** Записывает смысловые звуки вместо воспроизведения. */
internal class RecordingSounds : SoundEffects {
    val events = mutableListOf<String>()
    override fun correct() { events += "correct" }
    override fun wrong() { events += "wrong" }
    override fun complete() { events += "complete" }
}

/** Запоминает, какие записи пришли на воспроизведение (по размеру), для проверки `PlayerSoundEffects`. */
internal class RecordingSoundPlayer : PlatformSoundPlayer {
    val played = mutableListOf<ByteArray>()
    override fun play(bytes: ByteArray) { played += bytes }
}

/** Записывает платформенные примитивы, чтобы проверять паттерны `PatternHaptics`. */
internal class RecordingPlatformHaptics : PlatformHaptics {
    val events = mutableListOf<String>()
    override fun tickLight() { events += "light" }
    override fun tickMedium() { events += "medium" }
    override fun success() { events += "success" }
}

internal class FakePlatformReminders(var granted: Boolean = true) : PlatformReminders {
    val scheduledHours = mutableListOf<Int>()
    var cancels = 0
    override suspend fun requestPermission() = granted
    override fun schedule(hour: Int, texts: ReminderTexts) { scheduledHours += hour }
    override fun cancel() { cancels++ }
    var shownNow = 0
    override fun showNow(texts: ReminderTexts) { shownNow++ }
}

internal fun testReminders(
    platform: PlatformReminders = FakePlatformReminders(),
    settings: ReminderSettings = ReminderSettings(MapSettings()),
) = Reminders(platform, settings) { ReminderTexts("title", "body", "channel") }
