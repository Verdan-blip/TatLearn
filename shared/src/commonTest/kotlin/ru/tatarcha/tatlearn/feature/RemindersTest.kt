package ru.tatarcha.tatlearn.feature

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.notifications.Reminders
import ru.tatarcha.tatlearn.data.local.HapticsSettings
import ru.tatarcha.tatlearn.data.local.SoundEffectsSettings
import ru.tatarcha.tatlearn.data.local.ReminderSettings
import ru.tatarcha.tatlearn.domain.model.UserStats
import ru.tatarcha.tatlearn.feature.profile.ProfileUiEvent
import ru.tatarcha.tatlearn.feature.profile.ProfileViewModel
import ru.tatarcha.tatlearn.feature.result.ReminderPrompt
import ru.tatarcha.tatlearn.feature.result.ResultUiEvent
import ru.tatarcha.tatlearn.feature.result.ResultViewModel
import ru.tatarcha.tatlearn.navigation.ResultKey
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RemindersTest {
    private val passedKey = ResultKey("lesson_1", xpEarned = 24, accuracyPercent = 90, durationSeconds = 130, mistakeStepIds = emptyList())
    private val failedKey = passedKey.copy(accuracyPercent = 40, correctAnswers = 4, totalAnswers = 10, mistakeStepIds = listOf("a"))

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun result(key: ResultKey, reminders: Reminders) =
        ResultViewModel(key, SessionOnlyAuth(), StatsProgress(UserStats.Empty), FakeProfile(), RecordingHaptics(), RecordingSounds(), reminders)

    @Test fun enableNeedsPermissionAndLeavesSettingsUntouchedWhenDenied() = runTest {
        val platform = FakePlatformReminders(granted = false)
        val reminders = testReminders(platform)
        assertFalse(reminders.enable(21))
        assertFalse(reminders.enabled.value)
        assertTrue(platform.scheduledHours.isEmpty())
        platform.granted = true
        assertTrue(reminders.enable(21))
        assertTrue(reminders.enabled.value)
        assertEquals(21, reminders.hour.value)
        assertEquals(listOf(21), platform.scheduledHours)
    }

    @Test fun changingHourReschedulesOnlyWhenEnabled() = runTest {
        val platform = FakePlatformReminders()
        val reminders = testReminders(platform)
        reminders.changeHour(8)
        assertEquals(8, reminders.hour.value)
        assertTrue(platform.scheduledHours.isEmpty())
        reminders.enable(8)
        reminders.changeHour(13)
        assertEquals(listOf(8, 13), platform.scheduledHours)
    }

    @Test fun disableCancelsAndRestoreSchedulesOnlyWhenEnabled() = runTest {
        val platform = FakePlatformReminders()
        val settings = ReminderSettings(MapSettings())
        val reminders = testReminders(platform, settings)
        reminders.restore()
        assertTrue(platform.scheduledHours.isEmpty())
        reminders.enable(19)
        reminders.restore()
        assertEquals(listOf(19, 19), platform.scheduledHours)
        reminders.disable()
        assertFalse(reminders.enabled.value)
        assertEquals(1, platform.cancels)
        assertFalse(ReminderSettings(MapSettings()).enabled.value)
    }

    @Test fun passedLessonOffersRemindersOnlyOnce() = runTest {
        val settings = ReminderSettings(MapSettings())
        val first = result(passedKey, testReminders(settings = settings))
        advanceUntilIdle()
        assertEquals(ReminderPrompt(hour = ReminderSettings.DEFAULT_HOUR), first.state.value.reminderPrompt)
        val second = result(passedKey, testReminders(settings = settings))
        advanceUntilIdle()
        assertNull(second.state.value.reminderPrompt)
    }

    @Test fun offerAppearsOnlyAfterScreenHasBeenShown() = runTest {
        val vm = result(passedKey, testReminders())
        assertNull(vm.state.value.reminderPrompt)
        advanceTimeBy(TamyrMotion.REMINDER_PROMPT_DELAY_MS - 1)
        assertNull(vm.state.value.reminderPrompt)
        advanceTimeBy(2)
        assertNotNull(vm.state.value.reminderPrompt)
    }

    @Test fun failedLessonDoesNotUseUpTheOffer() = runTest {
        val settings = ReminderSettings(MapSettings())
        val failed = result(failedKey, testReminders(settings = settings))
        advanceUntilIdle()
        assertNull(failed.state.value.reminderPrompt)
        val passed = result(passedKey, testReminders(settings = settings))
        advanceUntilIdle()
        assertNotNull(passed.state.value.reminderPrompt)
    }

    @Test fun alreadyEnabledRemindersAreNotOffered() = runTest {
        val reminders = testReminders()
        reminders.enable(8)
        val vm = result(passedKey, reminders)
        advanceUntilIdle()
        assertNull(vm.state.value.reminderPrompt)
    }

    @Test fun resultPromptEnablesChosenHour() = runTest {
        val platform = FakePlatformReminders()
        val vm = result(passedKey, testReminders(platform))
        advanceUntilIdle()
        vm.onEvent(ResultUiEvent.ReminderHourSelected(21))
        assertEquals(21, vm.state.value.reminderPrompt?.hour)
        vm.onEvent(ResultUiEvent.ReminderEnableClicked)
        assertNull(vm.state.value.reminderPrompt)
        assertEquals(listOf(21), platform.scheduledHours)
    }

    @Test fun resultPromptLaterEnablesNothing() = runTest {
        val platform = FakePlatformReminders()
        val reminders = testReminders(platform)
        val vm = result(passedKey, reminders)
        advanceUntilIdle()
        vm.onEvent(ResultUiEvent.ReminderLaterClicked)
        assertNull(vm.state.value.reminderPrompt)
        assertFalse(reminders.enabled.value)
        assertTrue(platform.scheduledHours.isEmpty())
    }

    @Test fun debugTestNotificationNeedsPermission() = runTest {
        val denied = FakePlatformReminders(granted = false)
        assertFalse(testReminders(denied).sendTest())
        assertEquals(0, denied.shownNow)
        val granted = FakePlatformReminders()
        assertTrue(testReminders(granted).sendTest())
        assertEquals(1, granted.shownNow)
    }

    @Test fun resetOfferDisablesAndAllowsOfferAgain() = runTest {
        val reminders = testReminders()
        reminders.enable(8)
        reminders.markPromptShown()
        reminders.resetOffer()
        assertFalse(reminders.enabled.value)
        assertTrue(reminders.shouldOfferPrompt)
    }

    private fun profile(reminders: Reminders) =
        ProfileViewModel(FakeProfile(), StatsProgress(), SessionOnlyAuth(), HapticsSettings(MapSettings()), SoundEffectsSettings(MapSettings()), reminders)

    @Test fun profileToggleEnablesAndChangesHour() = runTest {
        val platform = FakePlatformReminders()
        val vm = profile(testReminders(platform))
        assertFalse(vm.state.value.reminderEnabled)
        vm.onEvent(ProfileUiEvent.ReminderToggled(true))
        assertTrue(vm.state.value.reminderEnabled)
        vm.onEvent(ProfileUiEvent.ReminderHourSelected(13))
        assertEquals(13, vm.state.value.reminderHour)
        assertEquals(listOf(ReminderSettings.DEFAULT_HOUR, 13), platform.scheduledHours)
        vm.onEvent(ProfileUiEvent.ReminderToggled(false))
        assertFalse(vm.state.value.reminderEnabled)
    }

    @Test fun profileShowsToastWhenPermissionIsDenied() = runTest {
        val vm = profile(testReminders(FakePlatformReminders(granted = false)))
        vm.onEvent(ProfileUiEvent.ReminderToggled(true))
        assertFalse(vm.state.value.reminderEnabled)
        assertTrue(vm.state.value.reminderDeniedToast)
        vm.onEvent(ProfileUiEvent.ToastDismissed)
        assertFalse(vm.state.value.reminderDeniedToast)
    }
}
