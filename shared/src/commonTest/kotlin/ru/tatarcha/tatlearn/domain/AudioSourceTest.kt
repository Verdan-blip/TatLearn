package ru.tatarcha.tatlearn.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.tatarcha.tatlearn.core.audio.AudioPlayer
import ru.tatarcha.tatlearn.core.audio.AudioPlayerState
import ru.tatarcha.tatlearn.core.audio.StubbingAudioPlayer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class RecordingPlayer : AudioPlayer {
    val urls = mutableListOf<String>()
    val byteCounts = mutableListOf<Int>()
    override val state: StateFlow<AudioPlayerState> = MutableStateFlow(AudioPlayerState.Idle)
    override fun play(url: String, rate: Float) { urls += url }
    override fun play(bytes: ByteArray, rate: Float) { byteCounts += bytes.size }
    override fun stop() = Unit
    override fun release() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class AudioSourceTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun remoteUrlGoesStraightToPlatformPlayer() = runTest {
        val delegate = RecordingPlayer()
        StubbingAudioPlayer(delegate).play("https://example.org/a.m4a")
        assertEquals(listOf("https://example.org/a.m4a"), delegate.urls)
        assertTrue(delegate.byteCounts.isEmpty())
    }

    @Test fun missingBundledFileShowsError() = runTest {
        val delegate = RecordingPlayer()
        val player = StubbingAudioPlayer(delegate)
        player.play("audio/missing.m4a")
        assertEquals(AudioPlayerState.Error, player.state.value)
        assertTrue(delegate.byteCounts.isEmpty())
    }
}
