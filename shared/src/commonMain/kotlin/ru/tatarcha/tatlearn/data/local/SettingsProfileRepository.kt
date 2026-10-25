package ru.tatarcha.tatlearn.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.AvatarColors
import ru.tatarcha.tatlearn.domain.model.AvatarPose
import ru.tatarcha.tatlearn.domain.model.UserProfile
import ru.tatarcha.tatlearn.domain.repository.ProfileRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Профиль в multiplatform-settings, как и сессия. Данные не уходят в сеть.
 * Аватар хранится строкой: `color:N` или `pose:Имя`; неизвестное значение читается как аватар по умолчанию.
 */
class SettingsProfileRepository(
    private val settings: Settings,
    private val newId: () -> String = ::randomId,
) : ProfileRepository {
    private val _profile = MutableStateFlow(read())
    override val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _saved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val saved: SharedFlow<Unit> = _saved.asSharedFlow()

    override fun save(name: String, avatar: Avatar) {
        val id = _profile.value?.id ?: newId()
        settings.putString(KEY_ID, id)
        settings.putString(KEY_NAME, name)
        settings.putString(KEY_AVATAR, encode(avatar))
        _profile.value = UserProfile(id, name, avatar)
        _saved.tryEmit(Unit)
    }

    override fun clear() {
        settings.remove(KEY_ID)
        settings.remove(KEY_NAME)
        settings.remove(KEY_AVATAR)
        _profile.value = null
    }

    private fun read(): UserProfile? {
        val id = settings.getStringOrNull(KEY_ID) ?: return null
        val name = settings.getStringOrNull(KEY_NAME) ?: return null
        return UserProfile(id, name, decode(settings.getStringOrNull(KEY_AVATAR)))
    }

    internal companion object {
        const val KEY_ID = "profile_id"
        const val KEY_NAME = "profile_name"
        const val KEY_AVATAR = "profile_avatar"

        fun encode(avatar: Avatar): String = when (avatar) {
            is Avatar.Color -> "color:${avatar.index}"
            is Avatar.Pose -> "pose:${avatar.pose.name}"
        }

        fun decode(raw: String?): Avatar {
            val (kind, value) = raw?.split(':', limit = 2)?.takeIf { it.size == 2 } ?: return Avatar.Default
            return when (kind) {
                "color" -> value.toIntOrNull()?.takeIf { it in 0 until AvatarColors.COUNT }?.let(Avatar::Color)
                "pose" -> AvatarPose.entries.firstOrNull { it.name == value }?.let(Avatar::Pose)
                else -> null
            } ?: Avatar.Default
        }

        @OptIn(ExperimentalUuidApi::class)
        fun randomId(): String = Uuid.random().toString()
    }
}
