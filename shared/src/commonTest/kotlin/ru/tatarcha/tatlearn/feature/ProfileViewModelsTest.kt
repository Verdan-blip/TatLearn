package ru.tatarcha.tatlearn.feature

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.tatarcha.tatlearn.core.common.TimeProvider
import ru.tatarcha.tatlearn.core.haptics.PatternHaptics
import ru.tatarcha.tatlearn.core.sound.PlayerSoundEffects
import ru.tatarcha.tatlearn.data.local.HapticsSettings
import ru.tatarcha.tatlearn.data.local.NarratorSettings
import ru.tatarcha.tatlearn.data.local.SoundEffectsSettings
import ru.tatarcha.tatlearn.domain.model.NarratorVoice
import ru.tatarcha.tatlearn.data.local.OnboardingStorage
import ru.tatarcha.tatlearn.data.local.SettingsProfileRepository
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.AvatarPose
import ru.tatarcha.tatlearn.domain.model.NameError
import ru.tatarcha.tatlearn.domain.model.ProfileRules
import ru.tatarcha.tatlearn.domain.model.UserProfile
import ru.tatarcha.tatlearn.domain.model.UserSession
import ru.tatarcha.tatlearn.domain.model.UserStats
import ru.tatarcha.tatlearn.feature.intro.IntroUiEffect
import ru.tatarcha.tatlearn.feature.intro.IntroUiEvent
import ru.tatarcha.tatlearn.feature.intro.IntroViewModel
import ru.tatarcha.tatlearn.feature.profile.ProfileEditUiEffect
import ru.tatarcha.tatlearn.feature.profile.ProfileEditUiEvent
import ru.tatarcha.tatlearn.feature.profile.ProfileEditViewModel
import ru.tatarcha.tatlearn.feature.profile.ProfileUiEffect
import ru.tatarcha.tatlearn.feature.profile.ProfileUiEvent
import ru.tatarcha.tatlearn.feature.profile.ProfileViewModel
import ru.tatarcha.tatlearn.navigation.AuthKey
import ru.tatarcha.tatlearn.navigation.HomeKey
import ru.tatarcha.tatlearn.navigation.IntroKey
import ru.tatarcha.tatlearn.navigation.OnboardingKey
import ru.tatarcha.tatlearn.navigation.StartDestinationResolver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelsTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    // --- Правила имени ---

    @Test fun nameRulesTrimCollapseAndValidate() {
        assertEquals("Алия Гайнуллина", ProfileRules.normalize("  Алия   Гайнуллина "))
        assertEquals(NameError.Empty, ProfileRules.validate("   "))
        assertNull(ProfileRules.validate("Әлфия"))
        assertNull(ProfileRules.validate("а".repeat(ProfileRules.MAX_NAME_LENGTH)))
        assertEquals(NameError.TooLong, ProfileRules.validate("а".repeat(ProfileRules.MAX_NAME_LENGTH + 1)))
        assertEquals("Ә", ProfileRules.initial(" әлфия"))
        assertEquals("", ProfileRules.initial("  "))
    }

    // --- Хранилище ---

    @Test fun repositoryPersistsProfileAvatarAndKeepsIdAcrossSaves() = runTest {
        val settings = MapSettings()
        val repo = SettingsProfileRepository(settings, newId = { "guest-1" })
        assertNull(repo.profile.value)
        repo.save("Алия", Avatar.Pose(AvatarPose.Sleeping))
        repo.save("Алия Г", Avatar.Color(2))
        val restored = SettingsProfileRepository(settings, newId = { "other" }).profile.value
        assertEquals(UserProfile("guest-1", "Алия Г", Avatar.Color(2)), restored)
        repo.clear()
        assertNull(repo.profile.value)
        assertNull(SettingsProfileRepository(settings).profile.value)
    }

    @Test fun repositoryReadsUnknownAvatarAsDefault() {
        val settings = MapSettings("profile_id" to "x", "profile_name" to "Алия", "profile_avatar" to "pose:Dragon")
        assertEquals(Avatar.Default, SettingsProfileRepository(settings).profile.value?.avatar)
        val bad = MapSettings("profile_id" to "x", "profile_name" to "Алия", "profile_avatar" to "color:99")
        assertEquals(Avatar.Default, SettingsProfileRepository(bad).profile.value?.avatar)
    }

    // --- Стартовый маршрут ---

    @Test fun guestModeRoutesOnboardingThenIntroThenHome() {
        val onboarding = OnboardingStorage(MapSettings())
        val profile = FakeProfile()
        fun route() = StartDestinationResolver(SessionOnlyAuth(), onboarding, profile, registrationEnabled = false).resolve()
        assertEquals(OnboardingKey, route())
        onboarding.markSeen()
        assertEquals(IntroKey, route())
        profile.save("Алия", Avatar.Default)
        assertEquals(HomeKey, route())
    }

    @Test fun signedInUserGetsProfileFromSessionNameWithoutIntro() {
        val profile = FakeProfile()
        val resolver = StartDestinationResolver(SessionOnlyAuth(UserSession("Алия", "a@b.ru")), OnboardingStorage(MapSettings()), profile, registrationEnabled = false)
        assertEquals(HomeKey, resolver.resolve())
        assertEquals("Алия", profile.profile.value?.name)
        assertEquals(Avatar.Default, profile.profile.value?.avatar)
    }

    @Test fun registrationFlagKeepsOldFlowAndIgnoresProfile() {
        val onboarding = OnboardingStorage(MapSettings())
        fun route(session: UserSession? = null) =
            StartDestinationResolver(SessionOnlyAuth(session), onboarding, FakeProfile(UserProfile("id", "Алия", Avatar.Default)), registrationEnabled = true).resolve()
        assertEquals(OnboardingKey, route())
        onboarding.markSeen()
        assertEquals(AuthKey(), route())
        assertEquals(HomeKey, route(UserSession("A", "a@b.ru")))
    }

    // --- Знакомство ---

    @Test fun introContinueSavesTrimmedNameAndChosenColor() = runTest {
        val profile = FakeProfile()
        val vm = IntroViewModel(profile)
        vm.onEvent(IntroUiEvent.NameChanged("  Алия  "))
        vm.onEvent(IntroUiEvent.ColorSelected(Avatar.Color(2)))
        vm.onEvent(IntroUiEvent.ContinueClicked)
        assertEquals<List<Pair<String, Avatar>>>(listOf("Алия" to Avatar.Color(2)), profile.saves)
        assertEquals(IntroUiEffect.NavigateHome, vm.effects.first())
    }

    @Test fun introEmptyNameFallsBackToDefaultAndSkipUsesDefaults() = runTest {
        val profile = FakeProfile()
        val vm = IntroViewModel(profile)
        vm.onEvent(IntroUiEvent.ContinueClicked)
        assertEquals(ProfileRules.DEFAULT_NAME, profile.saves.single().first)
        val skip = FakeProfile()
        val skipVm = IntroViewModel(skip)
        skipVm.onEvent(IntroUiEvent.NameChanged("Алия"))
        skipVm.onEvent(IntroUiEvent.ColorSelected(Avatar.Color(3)))
        skipVm.onEvent(IntroUiEvent.SkipClicked)
        assertEquals(ProfileRules.DEFAULT_NAME to Avatar.Default, skip.saves.single())
        assertEquals(IntroUiEffect.NavigateHome, skipVm.effects.first())
    }

    @Test fun introBlocksTooLongNameAndShowsCounterFromFifteenChars() = runTest {
        val profile = FakeProfile()
        val vm = IntroViewModel(profile)
        vm.onEvent(IntroUiEvent.NameChanged("а".repeat(14)))
        assertNull(vm.state.value.counter)
        vm.onEvent(IntroUiEvent.NameChanged("а".repeat(31)))
        assertEquals(31 to 20, vm.state.value.counter)
        assertEquals(NameError.TooLong, vm.state.value.nameError)
        assertFalse(vm.state.value.canContinue)
        vm.onEvent(IntroUiEvent.ContinueClicked)
        assertTrue(profile.saves.isEmpty())
    }

    @Test fun introIgnoresPoseSelection() = runTest {
        val vm = IntroViewModel(FakeProfile())
        vm.onEvent(IntroUiEvent.ColorSelected(Avatar.Pose(AvatarPose.Cheer)))
        assertEquals(Avatar.Color(0), vm.state.value.avatar)
    }

    // --- Профиль ---

    @Test fun profileTabShowsProfileAndSavedToastAndOpensEdit() = runTest {
        val profile = FakeProfile(UserProfile("id", "Алия", Avatar.Pose(AvatarPose.Searching)))
        val vm = ProfileViewModel(profile, StatsProgress(UserStats(xp = 340, streak = 12)), SessionOnlyAuth(), HapticsSettings(MapSettings()), SoundEffectsSettings(MapSettings()), NarratorSettings(MapSettings()), testReminders())
        assertEquals("Алия", vm.state.value.name)
        assertEquals(Avatar.Pose(AvatarPose.Searching), vm.state.value.avatar)
        assertEquals(4, vm.state.value.stats.level)
        assertEquals(60, vm.state.value.stats.xpToNextLevel)
        assertFalse(vm.state.value.savedToast)
        profile.save("Алия Г", Avatar.Default)
        assertEquals("Алия Г", vm.state.value.name)
        assertTrue(vm.state.value.savedToast)
        vm.onEvent(ProfileUiEvent.ToastDismissed)
        assertFalse(vm.state.value.savedToast)
        vm.onEvent(ProfileUiEvent.EditClicked)
        assertEquals(ProfileUiEffect.NavigateToEdit, vm.effects.first())
    }

    @Test fun profileTabFallsBackToSessionNameWithoutProfile() = runTest {
        val vm = ProfileViewModel(FakeProfile(), StatsProgress(), SessionOnlyAuth(UserSession("Тимур", "t@b.ru")), HapticsSettings(MapSettings()), SoundEffectsSettings(MapSettings()), NarratorSettings(MapSettings()), testReminders())
        assertEquals("Тимур", vm.state.value.name)
        assertEquals(Avatar.Default, vm.state.value.avatar)
    }

    // --- Вибро ---

    @Test fun hapticsSettingDefaultsToOnAndPersists() = runTest {
        val settings = MapSettings()
        assertTrue(HapticsSettings(settings).enabled.value)
        val vm = ProfileViewModel(FakeProfile(), StatsProgress(), SessionOnlyAuth(), HapticsSettings(settings), SoundEffectsSettings(settings), NarratorSettings(MapSettings()), testReminders())
        assertTrue(vm.state.value.hapticsEnabled)
        vm.onEvent(ProfileUiEvent.HapticsToggled(false))
        assertFalse(vm.state.value.hapticsEnabled)
        assertFalse(HapticsSettings(settings).enabled.value)
    }

    private class FakeClock(var now: Long = 1_000L) : TimeProvider {
        override fun nowMillis() = now
    }

    @Test fun soundsSettingDefaultsToOnAndPersistsIndependentlyOfHaptics() = runTest {
        val settings = MapSettings()
        assertTrue(SoundEffectsSettings(settings).enabled.value)
        val vm = ProfileViewModel(FakeProfile(), StatsProgress(), SessionOnlyAuth(), HapticsSettings(settings), SoundEffectsSettings(settings), NarratorSettings(MapSettings()), testReminders())
        assertTrue(vm.state.value.soundsEnabled)
        vm.onEvent(ProfileUiEvent.SoundsToggled(false))
        assertFalse(vm.state.value.soundsEnabled)
        assertTrue(vm.state.value.hapticsEnabled)
        assertFalse(SoundEffectsSettings(settings).enabled.value)
    }

    @Test fun narratorVoiceDefaultsToMaleAndPersists() = runTest {
        val settings = MapSettings()
        assertEquals(NarratorVoice.Male, NarratorSettings(settings).voice.value)
        val vm = ProfileViewModel(FakeProfile(), StatsProgress(), SessionOnlyAuth(), HapticsSettings(settings), SoundEffectsSettings(settings), NarratorSettings(settings), testReminders())
        assertEquals(NarratorVoice.Male, vm.state.value.narratorVoice)
        vm.onEvent(ProfileUiEvent.NarratorVoiceSelected(NarratorVoice.Female))
        assertEquals(NarratorVoice.Female, vm.state.value.narratorVoice)
        assertEquals(NarratorVoice.Female, NarratorSettings(settings).voice.value)
    }

    @Test fun selectingNarratorVoicePlaysItsPreviewEvenWhenAlreadySelected() = runTest {
        val played = mutableListOf<NarratorVoice>()
        val vm = ProfileViewModel(
            FakeProfile(), StatsProgress(), SessionOnlyAuth(), HapticsSettings(MapSettings()), SoundEffectsSettings(MapSettings()),
            NarratorSettings(MapSettings()), testReminders(), preview = { played += it },
        )
        vm.onEvent(ProfileUiEvent.NarratorVoiceSelected(NarratorVoice.Female))
        vm.onEvent(ProfileUiEvent.NarratorVoiceSelected(NarratorVoice.Female)) // повторный тап по выбранному голосу слушает его снова
        vm.onEvent(ProfileUiEvent.NarratorVoiceSelected(NarratorVoice.Male))
        assertEquals(listOf(NarratorVoice.Female, NarratorVoice.Female, NarratorVoice.Male), played)
    }

    @Test fun aboutClickedOpensAboutScreen() = runTest {
        val vm = ProfileViewModel(FakeProfile(), StatsProgress(), SessionOnlyAuth(), HapticsSettings(MapSettings()), SoundEffectsSettings(MapSettings()), NarratorSettings(MapSettings()), testReminders())
        vm.onEvent(ProfileUiEvent.AboutClicked)
        assertEquals(ProfileUiEffect.NavigateToAbout, vm.effects.first())
    }

    @Test fun narratorVoiceResolvesBundledPathsOnlyForFemale() {
        assertEquals("audio/alma.m4a", NarratorVoice.Male.resolve("audio/alma.m4a"))
        assertEquals("audio/female/alma.m4a", NarratorVoice.Female.resolve("audio/alma.m4a"))
        assertEquals("https://x/alma.m4a", NarratorVoice.Female.resolve("https://x/alma.m4a")) // сетевые адреса не меняем
    }

    @Test fun soundEffectsPlayRequestedFileOnceCacheItAndRespectSetting() = runTest {
        val player = RecordingSoundPlayer()
        val settings = SoundEffectsSettings(MapSettings())
        val reads = mutableListOf<String>()
        val sounds = PlayerSoundEffects(player, settings, this) { path -> reads += path; path.encodeToByteArray() }
        sounds.correct()
        sounds.correct()
        sounds.wrong()
        sounds.complete()
        advanceUntilIdle()
        assertEquals(listOf("correct.m4a", "correct.m4a", "wrong.m4a", "complete.m4a"), player.played.map { it.decodeToString().substringAfterLast('/') })
        assertEquals(3, reads.size) // повторный звук берётся из кэша
        settings.setEnabled(false)
        sounds.correct()
        advanceUntilIdle()
        assertEquals(4, player.played.size)
    }

    @Test fun missingSoundFileDoesNotCrashOrPlay() = runTest {
        val player = RecordingSoundPlayer()
        val sounds = PlayerSoundEffects(player, SoundEffectsSettings(MapSettings()), this) { error("no file") }
        sounds.correct()
        advanceUntilIdle()
        assertTrue(player.played.isEmpty())
    }

    @Test fun hapticPatternsUseSoftTicksAndRespectSettingAndDebounce() = runTest {
        val platform = RecordingPlatformHaptics()
        val settings = HapticsSettings(MapSettings())
        val clock = FakeClock()
        val haptics = PatternHaptics(platform, settings, this, clock)
        haptics.correct()
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("success"), platform.events)
        // Слишком быстрое второе событие подавляется (не «дрожь»).
        clock.now += 50
        haptics.wrong()
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("success"), platform.events)
        clock.now += 500
        haptics.wrong()
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("success", "light", "light"), platform.events)
        clock.now += 500
        haptics.celebrate()
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("success", "light", "light", "success", "light", "medium"), platform.events)
        clock.now += 500
        haptics.unlocked()
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("success", "light", "light", "success", "light", "medium", "light"), platform.events)
        // Выключено в профиле: тишина.
        settings.setEnabled(false)
        clock.now += 500
        haptics.correct()
        testScheduler.advanceUntilIdle()
        assertEquals(7, platform.events.size)
    }

    // --- Изменение профиля ---

    private fun editVm(profile: FakeProfile = FakeProfile(UserProfile("id", "Алия", Avatar.Color(0)))) =
        ProfileEditViewModel(profile, SessionOnlyAuth()) to profile

    @Test fun editSaveIsEnabledOnlyForValidChanges() = runTest {
        val (vm, profile) = editVm()
        assertFalse(vm.state.value.canSave)
        vm.onEvent(ProfileEditUiEvent.SaveClicked)
        assertTrue(profile.saves.isEmpty())
        vm.onEvent(ProfileEditUiEvent.NameChanged("Алия"))
        assertFalse(vm.state.value.hasChanges)
        vm.onEvent(ProfileEditUiEvent.NameChanged("   "))
        assertEquals(NameError.Empty, vm.state.value.nameError)
        assertFalse(vm.state.value.canSave)
        vm.onEvent(ProfileEditUiEvent.NameChanged("а".repeat(21)))
        assertFalse(vm.state.value.canSave)
        vm.onEvent(ProfileEditUiEvent.NameChanged("  Әлфия  "))
        assertTrue(vm.state.value.canSave)
        vm.onEvent(ProfileEditUiEvent.AvatarSelected(Avatar.Pose(AvatarPose.Cheer)))
        vm.onEvent(ProfileEditUiEvent.SaveClicked)
        assertEquals<List<Pair<String, Avatar>>>(listOf("Әлфия" to Avatar.Pose(AvatarPose.Cheer)), profile.saves)
        assertEquals(ProfileEditUiEffect.Close, vm.effects.first())
    }

    @Test fun editAvatarOnlyChangeCountsAsChange() = runTest {
        val (vm, _) = editVm()
        vm.onEvent(ProfileEditUiEvent.AvatarSelected(Avatar.Color(1)))
        assertTrue(vm.state.value.canSave)
        vm.onEvent(ProfileEditUiEvent.AvatarSelected(Avatar.Color(0)))
        assertFalse(vm.state.value.hasChanges)
    }

    @Test fun editBackClosesDirectlyWithoutChangesAndAsksWithChanges() = runTest {
        val (clean, _) = editVm()
        clean.onEvent(ProfileEditUiEvent.BackClicked)
        assertEquals(ProfileEditUiEffect.Close, clean.effects.first())
        assertFalse(clean.state.value.exitSheet)

        val (dirty, profile) = editVm()
        dirty.onEvent(ProfileEditUiEvent.NameChanged("Другое"))
        dirty.onEvent(ProfileEditUiEvent.BackClicked)
        assertTrue(dirty.state.value.exitSheet)
        dirty.onEvent(ProfileEditUiEvent.ExitDismissed)
        assertFalse(dirty.state.value.exitSheet)
        dirty.onEvent(ProfileEditUiEvent.BackClicked)
        dirty.onEvent(ProfileEditUiEvent.ExitConfirmed)
        assertEquals(ProfileEditUiEffect.Close, dirty.effects.first())
        assertTrue(profile.saves.isEmpty())
    }
}
