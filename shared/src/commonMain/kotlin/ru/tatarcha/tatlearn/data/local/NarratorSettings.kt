package ru.tatarcha.tatlearn.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.tatarcha.tatlearn.domain.model.NarratorVoice

/** Настройка «Голос диктора» из профиля; по умолчанию мужской (решение владельца 04.11.2026). */
class NarratorSettings(private val settings: Settings) {
    private val _voice = MutableStateFlow(read())
    val voice: StateFlow<NarratorVoice> = _voice.asStateFlow()

    fun setVoice(value: NarratorVoice) {
        settings.putString(KEY_VOICE, value.name)
        _voice.value = value
    }

    private fun read(): NarratorVoice =
        NarratorVoice.entries.firstOrNull { it.name == settings.getString(KEY_VOICE, "") } ?: NarratorVoice.Male

    private companion object {
        const val KEY_VOICE = "narrator_voice"
    }
}
