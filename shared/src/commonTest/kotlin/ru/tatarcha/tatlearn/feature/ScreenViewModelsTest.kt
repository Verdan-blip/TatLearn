package ru.tatarcha.tatlearn.feature

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.tatarcha.tatlearn.core.common.AppException
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.data.local.OnboardingStorage
import ru.tatarcha.tatlearn.domain.model.CourseModule
import ru.tatarcha.tatlearn.domain.model.ExerciseOutcome
import ru.tatarcha.tatlearn.domain.model.ExerciseResult
import ru.tatarcha.tatlearn.domain.model.HomeContent
import ru.tatarcha.tatlearn.domain.model.LessonStatus
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.LessonContent
import ru.tatarcha.tatlearn.domain.model.LessonProgress
import ru.tatarcha.tatlearn.domain.model.UserSession
import ru.tatarcha.tatlearn.domain.model.UserStats
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository
import ru.tatarcha.tatlearn.feature.auth.AuthMode
import ru.tatarcha.tatlearn.feature.auth.AuthToast
import ru.tatarcha.tatlearn.feature.auth.AuthUiEffect
import ru.tatarcha.tatlearn.feature.auth.AuthUiEvent
import ru.tatarcha.tatlearn.feature.auth.AuthValidator
import ru.tatarcha.tatlearn.feature.auth.AuthViewModel
import ru.tatarcha.tatlearn.feature.auth.EmailError
import ru.tatarcha.tatlearn.feature.auth.PasswordError
import ru.tatarcha.tatlearn.feature.auth.SocialProvider
import ru.tatarcha.tatlearn.feature.home.HomeNavItem
import ru.tatarcha.tatlearn.feature.home.HomeToast
import ru.tatarcha.tatlearn.feature.home.HomeUiEffect
import ru.tatarcha.tatlearn.feature.home.HomeUiEvent
import ru.tatarcha.tatlearn.feature.home.HomeViewModel
import ru.tatarcha.tatlearn.feature.onboarding.OnboardingUiEffect
import ru.tatarcha.tatlearn.feature.onboarding.OnboardingUiEvent
import ru.tatarcha.tatlearn.feature.onboarding.OnboardingViewModel
import ru.tatarcha.tatlearn.feature.result.ResultUiEffect
import ru.tatarcha.tatlearn.feature.result.ResultUiEvent
import ru.tatarcha.tatlearn.feature.result.ResultViewModel
import ru.tatarcha.tatlearn.navigation.AuthKey
import ru.tatarcha.tatlearn.navigation.HomeKey
import ru.tatarcha.tatlearn.navigation.OnboardingKey
import ru.tatarcha.tatlearn.navigation.ResultKey
import ru.tatarcha.tatlearn.navigation.StartDestinationResolver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeAuth(
    initial: UserSession? = null,
    var failure: Throwable? = null,
) : AuthRepository {
    private val _session = MutableStateFlow(initial)
    override val session: StateFlow<UserSession?> = _session
    val calls = mutableListOf<String>()

    override suspend fun signIn(email: String, password: String): Result<UserSession> = authenticate("in", email)
    override suspend fun signUp(email: String, password: String): Result<UserSession> = authenticate("up", email)
    override fun signOut() { _session.value = null }

    private fun authenticate(kind: String, email: String): Result<UserSession> {
        calls += "$kind:$email"
        failure?.let { return Result.failure(it) }
        return Result.success(UserSession("User", email).also { _session.value = it })
    }
}

private class HomeLessons(var result: Result<List<CourseModule>>) : LessonsRepository {
    var calls = 0
    override suspend fun getModules(): Result<List<CourseModule>> { calls++; return result }
    override suspend fun getLessonContent(lessonId: String): Result<LessonContent> = error("unused")
}

private class StatsProgress(initial: UserStats = UserStats.Empty) : ProgressRepository {
    val stats = MutableStateFlow(initial)
    val lessonProgress = MutableStateFlow<List<LessonProgress>>(emptyList())
    override fun observeStats(): Flow<UserStats> = stats
    override fun observeLessonProgress(): Flow<List<LessonProgress>> = lessonProgress
    override suspend fun recordExerciseResult(result: ExerciseResult): ExerciseOutcome = error("unused")
    override suspend fun completeLesson(lessonId: String, scorePercent: Int) = Unit
}

private val foodModule = CourseModule(
    "food", "Ашлар", "Еда", "food",
    lessons = listOf(Lesson("lesson_3", 3, "Ашлар", "Еда, часть 1"), Lesson("lesson_4", 4, "Туганнар", "Семья")),
)
private val natureModule = CourseModule(
    "nature", "Табигать", "Природа", "nature",
    lessons = listOf(Lesson("lesson_7", 7, "Табигать", "Природа, часть 1")),
    lockHint = "Откроется после «Еды»",
)

private fun catalog() = listOf(foodModule, natureModule)

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenViewModelsTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    // --- Вход ---

    @Test fun validatorChecksEmailAndPassword() {
        assertEquals(EmailError.Empty, AuthValidator.email("  "))
        assertEquals(EmailError.Invalid, AuthValidator.email("aliya@"))
        assertNull(AuthValidator.email(" aliya@mail.ru "))
        assertEquals(PasswordError.Empty, AuthValidator.password("", AuthMode.SignIn))
        assertEquals(PasswordError.Empty, AuthValidator.password("", AuthMode.SignUp))
        assertEquals(PasswordError.TooShort, AuthValidator.password("12345", AuthMode.SignUp))
        assertNull(AuthValidator.password("123456", AuthMode.SignUp))
    }

    @Test fun signInAcceptsShortPassword() = runTest {
        val auth = FakeAuth()
        val vm = AuthViewModel(AuthMode.SignIn, auth)
        vm.onEvent(AuthUiEvent.EmailChanged("a@b.ru"))
        vm.onEvent(AuthUiEvent.PasswordChanged("1"))
        vm.onEvent(AuthUiEvent.SubmitClicked)
        assertNull(vm.state.value.passwordError)
        assertEquals(listOf("in:a@b.ru"), auth.calls)
    }

    @Test fun invalidFormDoesNotCallRepository() = runTest {
        val auth = FakeAuth()
        val vm = AuthViewModel(AuthMode.SignUp, auth)
        vm.onEvent(AuthUiEvent.EmailChanged("bad"))
        vm.onEvent(AuthUiEvent.PasswordChanged("1"))
        vm.onEvent(AuthUiEvent.SubmitClicked)
        assertEquals(EmailError.Invalid, vm.state.value.emailError)
        assertEquals(PasswordError.TooShort, vm.state.value.passwordError)
        assertTrue(auth.calls.isEmpty())
    }

    @Test fun editingFieldClearsItsError() = runTest {
        val vm = AuthViewModel(AuthMode.SignIn, FakeAuth())
        vm.onEvent(AuthUiEvent.SubmitClicked)
        assertEquals(EmailError.Empty, vm.state.value.emailError)
        vm.onEvent(AuthUiEvent.EmailChanged("a"))
        assertNull(vm.state.value.emailError)
        assertEquals(PasswordError.Empty, vm.state.value.passwordError)
    }

    @Test fun signInSuccessNavigatesHome() = runTest {
        val auth = FakeAuth()
        val vm = AuthViewModel(AuthMode.SignIn, auth)
        vm.onEvent(AuthUiEvent.EmailChanged(" aliya@mail.ru "))
        vm.onEvent(AuthUiEvent.PasswordChanged("secret1"))
        vm.onEvent(AuthUiEvent.SubmitClicked)
        assertEquals(listOf("in:aliya@mail.ru"), auth.calls)
        assertFalse(vm.state.value.isSubmitting)
        assertEquals(AuthUiEffect.NavigateToHome, vm.effects.first())
    }

    @Test fun signUpModeCallsSignUp() = runTest {
        val auth = FakeAuth()
        val vm = AuthViewModel(AuthMode.SignUp, auth)
        vm.onEvent(AuthUiEvent.EmailChanged("a@b.ru"))
        vm.onEvent(AuthUiEvent.PasswordChanged("secret1"))
        vm.onEvent(AuthUiEvent.SubmitClicked)
        assertEquals(listOf("up:a@b.ru"), auth.calls)
    }

    @Test fun requestFailureShowsErrorToastAndStaysOnScreen() = runTest {
        val vm = AuthViewModel(AuthMode.SignUp, FakeAuth(failure = AppException("NET_TIMEOUT")))
        vm.onEvent(AuthUiEvent.EmailChanged("a@b.ru"))
        vm.onEvent(AuthUiEvent.PasswordChanged("secret1"))
        vm.onEvent(AuthUiEvent.SubmitClicked)
        assertEquals(AuthToast.RequestFailed(AuthMode.SignUp, "NET_TIMEOUT"), vm.state.value.toast)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test fun nextSubmitHidesPreviousErrorToast() = runTest {
        val auth = FakeAuth(failure = AppException("NET_ERROR"))
        val vm = AuthViewModel(AuthMode.SignIn, auth)
        vm.onEvent(AuthUiEvent.EmailChanged("a@b.ru"))
        vm.onEvent(AuthUiEvent.PasswordChanged("secret1"))
        vm.onEvent(AuthUiEvent.SubmitClicked)
        auth.failure = null
        vm.onEvent(AuthUiEvent.SubmitClicked)
        assertNull(vm.state.value.toast)
    }

    @Test fun socialClickShowsToastWithoutCallingRepository() = runTest {
        val auth = FakeAuth()
        val vm = AuthViewModel(AuthMode.SignUp, auth)
        vm.onEvent(AuthUiEvent.SocialClicked(SocialProvider.Vk))
        assertEquals(AuthToast.SocialUnavailable, vm.state.value.toast)
        assertTrue(auth.calls.isEmpty())
    }

    @Test fun forgotPasswordShowsToastWithoutCallingRepository() = runTest {
        val auth = FakeAuth()
        val vm = AuthViewModel(AuthMode.SignIn, auth)
        vm.onEvent(AuthUiEvent.ForgotPasswordClicked)
        assertEquals(AuthToast.ForgotPasswordUnavailable, vm.state.value.toast)
        assertTrue(auth.calls.isEmpty())
    }

    @Test fun toastDisappearsAfterFiveSeconds() = runTest {
        val vm = AuthViewModel(AuthMode.SignIn, FakeAuth())
        vm.onEvent(AuthUiEvent.SocialClicked(SocialProvider.Google))
        testScheduler.advanceTimeBy(TamyrMotion.TOAST_VISIBLE_MS - 1)
        assertEquals(AuthToast.SocialUnavailable, vm.state.value.toast)
        testScheduler.advanceTimeBy(2)
        assertNull(vm.state.value.toast)
    }

    @Test fun toastCanBeDismissedManually() = runTest {
        val vm = AuthViewModel(AuthMode.SignIn, FakeAuth())
        vm.onEvent(AuthUiEvent.SocialClicked(SocialProvider.Google))
        vm.onEvent(AuthUiEvent.ToastDismissed)
        assertNull(vm.state.value.toast)
    }

    @Test fun repeatedToastRestartsTimer() = runTest {
        val vm = AuthViewModel(AuthMode.SignIn, FakeAuth())
        vm.onEvent(AuthUiEvent.SocialClicked(SocialProvider.Google))
        testScheduler.advanceTimeBy(TamyrMotion.TOAST_VISIBLE_MS - 1000)
        vm.onEvent(AuthUiEvent.SocialClicked(SocialProvider.Vk))
        testScheduler.advanceTimeBy(TamyrMotion.TOAST_VISIBLE_MS - 1000)
        assertEquals(AuthToast.SocialUnavailable, vm.state.value.toast)
    }

    @Test fun passwordVisibilityAndModeToggle() = runTest {
        val vm = AuthViewModel(AuthMode.SignIn, FakeAuth())
        vm.onEvent(AuthUiEvent.TogglePasswordVisibility)
        assertTrue(vm.state.value.passwordVisible)
        vm.onEvent(AuthUiEvent.ModeChanged(AuthMode.SignUp))
        assertEquals(AuthMode.SignUp, vm.state.value.mode)
    }

    // --- Главная ---

    @Test fun homeLoadsContentAndStats() = runTest {
        val progress = StatsProgress(UserStats(xp = 120, streak = 3))
        val vm = HomeViewModel(HomeLessons(Result.success(catalog())), progress, FakeAuth(UserSession("Алия", "a@b.ru")))
        assertEquals("Алия", vm.state.value.userName)
        assertIs<LoadState.Content<HomeContent>>(vm.state.value.content)
        assertEquals(3, vm.state.value.stats.streak)
        progress.stats.value = UserStats(130, 3)
        assertEquals(130, vm.state.value.stats.xp)
    }

    @Test fun homeErrorThenRetrySucceeds() = runTest {
        val lessons = HomeLessons(Result.failure(AppException("NET_TIMEOUT")))
        val vm = HomeViewModel(lessons, StatsProgress(), FakeAuth())
        assertEquals(LoadState.Error("NET_TIMEOUT"), vm.state.value.content)
        lessons.result = Result.success(catalog())
        vm.onEvent(HomeUiEvent.RetryClicked)
        assertIs<LoadState.Content<HomeContent>>(vm.state.value.content)
        assertEquals(2, lessons.calls)
    }

    @Test fun startLessonNavigatesOnlyWhenLoaded() = runTest {
        val lessons = HomeLessons(Result.failure(AppException("NET_ERROR")))
        val vm = HomeViewModel(lessons, StatsProgress(), FakeAuth())
        vm.onEvent(HomeUiEvent.StartLessonClicked)
        lessons.result = Result.success(catalog())
        vm.onEvent(HomeUiEvent.RetryClicked)
        vm.onEvent(HomeUiEvent.StartLessonClicked)
        assertEquals(HomeUiEffect.NavigateToLesson("lesson_3"), vm.effects.first())
    }

    @Test fun moduleClickOpensItsScreenEvenWhenLocked() = runTest {
        val vm = HomeViewModel(HomeLessons(Result.success(catalog())), StatsProgress(), FakeAuth())
        vm.onEvent(HomeUiEvent.ModuleClicked("food"))
        assertEquals(HomeUiEffect.NavigateToModule("food"), vm.effects.first())
        vm.onEvent(HomeUiEvent.ModuleClicked("nature"))
        assertEquals(HomeUiEffect.NavigateToModule("nature"), vm.effects.first())
        assertNull(vm.state.value.toast)
    }

    @Test fun finishedLessonMovesCurrentLessonAndUpdatesProgress() = runTest {
        val progress = StatsProgress()
        val vm = HomeViewModel(HomeLessons(Result.success(catalog())), progress, FakeAuth())
        progress.lessonProgress.value = listOf(LessonProgress("lesson_3", LessonStatus.Completed, 90))
        val home = (vm.state.value.content as LoadState.Content).data
        assertEquals("lesson_4", home.currentLesson.lesson.id)
        assertEquals(50, home.modules.first { it.id == "food" }.progressPercent)
        vm.onEvent(HomeUiEvent.StartLessonClicked)
        assertEquals(HomeUiEffect.NavigateToLesson("lesson_4"), vm.effects.first())
    }

    @Test fun emptyCatalogShowsNotFound() = runTest {
        val vm = HomeViewModel(HomeLessons(Result.success(emptyList())), StatsProgress(), FakeAuth())
        assertEquals(LoadState.Error("NOT_FOUND"), vm.state.value.content)
    }

    @Test fun allModulesAndNavItemsShowStubToasts() = runTest {
        val vm = HomeViewModel(HomeLessons(Result.success(catalog())), StatsProgress(), FakeAuth())
        vm.onEvent(HomeUiEvent.AllModulesClicked)
        assertEquals(HomeToast.AllModulesUnavailable, vm.state.value.toast)
        vm.onEvent(HomeUiEvent.ToastDismissed)
        vm.onEvent(HomeUiEvent.NavItemClicked(HomeNavItem.Home))
        assertNull(vm.state.value.toast)
        vm.onEvent(HomeUiEvent.NavItemClicked(HomeNavItem.Profile))
        assertEquals(HomeNavItem.Profile, vm.state.value.tab)
        vm.onEvent(HomeUiEvent.NavItemClicked(HomeNavItem.Home))
        assertEquals(HomeNavItem.Home, vm.state.value.tab)
    }

    @Test fun homeToastDisappearsAfterFiveSeconds() = runTest {
        val vm = HomeViewModel(HomeLessons(Result.success(catalog())), StatsProgress(), FakeAuth())
        vm.onEvent(HomeUiEvent.AllModulesClicked)
        testScheduler.advanceTimeBy(TamyrMotion.TOAST_VISIBLE_MS + 1)
        assertNull(vm.state.value.toast)
    }

    // --- Результат ---

    private val resultKey = ResultKey("lesson_4", xpEarned = 24, accuracyPercent = 75, durationSeconds = 130, mistakeStepIds = listOf("quiz-1", "sentence-0"))

    @Test fun resultStateComesFromKey() = runTest {
        val vm = ResultViewModel(resultKey, FakeAuth(UserSession("Алия", "a@b.ru")), StatsProgress(UserStats(50, 4)))
        val state = vm.state.value
        assertEquals("Алия", state.userName)
        assertEquals(24, state.xpEarned)
        assertEquals(75, state.accuracyPercent)
        assertEquals(2, state.mistakes)
        assertEquals(4, state.streak)
    }

    @Test fun resultBelowThresholdIsNotPassed() = runTest {
        val key = resultKey.copy(accuracyPercent = 70, correctAnswers = 7, totalAnswers = 10, mistakeStepIds = listOf("a", "b", "c"))
        val state = ResultViewModel(key, FakeAuth(), StatsProgress()).state.value
        assertEquals(false, state.passed)
        assertEquals(1, state.missingAnswers)
        assertEquals(false, state.isMistakesRun)
    }

    @Test fun resultAtThresholdIsPassed() = runTest {
        val key = resultKey.copy(accuracyPercent = 80, correctAnswers = 8, totalAnswers = 10)
        assertTrue(ResultViewModel(key, FakeAuth(), StatsProgress()).state.value.passed)
    }

    @Test fun mistakesRunShowsUnfixedMistakes() = runTest {
        val key = resultKey.copy(accuracyPercent = 50, isMistakesRun = true, mistakeStepIds = listOf("quiz-1"))
        val state = ResultViewModel(key, FakeAuth(), StatsProgress()).state.value
        assertEquals(false, state.passed)
        assertEquals(1, state.missingAnswers)
        assertTrue(state.isMistakesRun)
    }

    @Test fun resultEffects() = runTest {
        val vm = ResultViewModel(resultKey, FakeAuth(), StatsProgress())
        vm.onEvent(ResultUiEvent.RetryMistakesClicked)
        assertEquals(ResultUiEffect.NavigateToMistakes("lesson_4", listOf("quiz-1", "sentence-0")), vm.effects.first())
        vm.onEvent(ResultUiEvent.ContinueClicked)
        assertEquals(ResultUiEffect.NavigateHome, vm.effects.first())
    }

    // --- Онбординг и стартовый экран ---

    @Test fun onboardingNextWalksPagesThenSignsUp() = runTest {
        val storage = OnboardingStorage(MapSettings())
        val vm = OnboardingViewModel(storage)
        vm.onEvent(OnboardingUiEvent.NextClicked)
        vm.onEvent(OnboardingUiEvent.NextClicked)
        assertEquals(2, vm.state.value.currentPage)
        assertTrue(vm.state.value.isLastPage)
        assertFalse(storage.isSeen())
        vm.onEvent(OnboardingUiEvent.NextClicked)
        assertTrue(storage.isSeen())
        assertEquals(OnboardingUiEffect.NavigateToAuth(AuthMode.SignUp), vm.effects.first())
    }

    @Test fun onboardingBackStopsAtFirstPageAndSkipSignsIn() = runTest {
        val storage = OnboardingStorage(MapSettings())
        val vm = OnboardingViewModel(storage)
        vm.onEvent(OnboardingUiEvent.BackClicked)
        assertEquals(0, vm.state.value.currentPage)
        vm.onEvent(OnboardingUiEvent.NextClicked)
        vm.onEvent(OnboardingUiEvent.BackClicked)
        assertEquals(0, vm.state.value.currentPage)
        vm.onEvent(OnboardingUiEvent.SkipClicked)
        assertTrue(storage.isSeen())
        assertEquals(OnboardingUiEffect.NavigateToAuth(AuthMode.SignIn), vm.effects.first())
    }

    @Test fun onboardingKeepsCurrentPageInState() = runTest {
        val vm = OnboardingViewModel(OnboardingStorage(MapSettings()))
        assertEquals(0, vm.state.value.currentPage)
        vm.onEvent(OnboardingUiEvent.PageChanged(2))
        assertEquals(2, vm.state.value.currentPage)
        assertFalse(OnboardingStorage(MapSettings()).isSeen())
    }

    @Test fun startDestinationFollowsSessionAndOnboarding() {
        val settings = MapSettings()
        val onboarding = OnboardingStorage(settings)
        assertEquals(OnboardingKey, StartDestinationResolver(FakeAuth(), onboarding).resolve())
        onboarding.markSeen()
        assertEquals(AuthKey(), StartDestinationResolver(FakeAuth(), onboarding).resolve())
        assertEquals(HomeKey, StartDestinationResolver(FakeAuth(UserSession("A", "a@b.ru")), onboarding).resolve())
    }
}
