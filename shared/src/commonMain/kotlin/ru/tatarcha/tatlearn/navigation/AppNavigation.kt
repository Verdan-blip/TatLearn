package ru.tatarcha.tatlearn.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import ru.tatarcha.tatlearn.core.common.BuildConfig
import ru.tatarcha.tatlearn.feature.about.AboutScene
import ru.tatarcha.tatlearn.feature.auth.AuthScene
import ru.tatarcha.tatlearn.feature.intro.IntroScene
import ru.tatarcha.tatlearn.feature.home.HomeScene
import ru.tatarcha.tatlearn.feature.lesson.LessonRoute
import ru.tatarcha.tatlearn.feature.module.ModuleScene
import ru.tatarcha.tatlearn.feature.onboarding.OnboardingScene
import ru.tatarcha.tatlearn.feature.profile.ProfileEditScene
import ru.tatarcha.tatlearn.feature.result.ResultScene
import ru.tatarcha.tatlearn.feature.showcase.ShowcaseScreen

// Не-JVM таргеты не умеют рефлексией находить подклассы NavKey, поэтому они регистрируются явно.
private val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(OnboardingKey::class)
            subclass(AuthKey::class)
            subclass(HomeKey::class)
            subclass(IntroKey::class)
            subclass(ProfileEditKey::class)
            subclass(ModuleKey::class)
            subclass(LessonKey::class)
            subclass(ResultKey::class)
            subclass(AboutKey::class)
            subclass(ShowcaseKey::class)
        }
    }
}

@Composable
fun AppNavigation(showDebugTools: Boolean = false) {
    val resolver = koinInject<StartDestinationResolver>()
    val startKey = remember { resolver.resolve() }
    val backStack = rememberNavBackStack(navSavedStateConfig, startKey)

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    /** Заменяет весь стек, например после входа или при уходе с экрана результата. */
    fun resetTo(key: NavKey) {
        backStack.clear()
        backStack.add(key)
    }

    Box {
        NavDisplay(
            backStack = backStack,
            onBack = ::pop,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<OnboardingKey> {
                    OnboardingScene(
                        // Регистрация закрыта фича-флагом: онбординг ведёт на знакомство вместо входа.
                        onNavigateToAuth = { mode ->
                            if (BuildConfig.REGISTRATION_ENABLED) backStack.add(AuthKey(mode)) else resetTo(IntroKey)
                        },
                    )
                }
                entry<IntroKey> {
                    IntroScene(onDone = { resetTo(HomeKey) })
                }
                entry<ProfileEditKey> {
                    ProfileEditScene(onClose = ::pop)
                }
                entry<AuthKey> { key ->
                    AuthScene(
                        mode = key.mode,
                        onNavigateToHome = { resetTo(HomeKey) },
                    )
                }
                entry<HomeKey> {
                    HomeScene(
                        showDebugTools = showDebugTools,
                        onOpenDebug = { backStack.add(ShowcaseKey) },
                        onStartLesson = { lessonId -> backStack.add(LessonKey(lessonId)) },
                        onOpenModule = { moduleId -> backStack.add(ModuleKey(moduleId)) },
                        onEditProfile = { backStack.add(ProfileEditKey) },
                        onOpenAbout = { backStack.add(AboutKey) },
                    )
                }
                entry<ModuleKey> { key ->
                    ModuleScene(
                        key = key,
                        onBack = ::pop,
                        onStartLesson = { lessonId -> backStack.add(LessonKey(lessonId)) },
                        onOpenModule = { moduleId -> backStack.add(ModuleKey(moduleId)) },
                    )
                }
                entry<LessonKey> { key ->
                    LessonRoute(
                        key = key,
                        // Законченный урок заменяется его результатом, поэтому «назад» из Result не откроет урок снова.
                        onFinished = { result ->
                            pop()
                            backStack.add(result)
                        },
                        onExit = ::pop,
                    )
                }
                entry<ResultKey> { key ->
                    ResultScene(
                        key = key,
                        onContinue = { resetTo(HomeKey) },
                        onRetryMistakes = { lessonId, stepIds -> backStack.add(LessonKey(lessonId, stepIds)) },
                    )
                }
                entry<AboutKey> {
                    AboutScene(onBack = ::pop)
                }
                entry<ShowcaseKey> {
                    ShowcaseScreen(
                        viewModel = koinViewModel(),
                        onClose = ::pop,
                        onSignedOut = { resetTo(if (BuildConfig.REGISTRATION_ENABLED) AuthKey() else IntroKey) },
                        onOnboardingReset = { resetTo(OnboardingKey) },
                    )
                }
            },
        )
    }
}
