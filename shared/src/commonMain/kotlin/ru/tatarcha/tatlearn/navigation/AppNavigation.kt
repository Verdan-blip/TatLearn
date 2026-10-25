package ru.tatarcha.tatlearn.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.feature.auth.AuthScene
import ru.tatarcha.tatlearn.feature.home.HomeScene
import ru.tatarcha.tatlearn.feature.lesson.LessonRoute
import ru.tatarcha.tatlearn.feature.module.ModuleScene
import ru.tatarcha.tatlearn.feature.onboarding.OnboardingScene
import ru.tatarcha.tatlearn.feature.result.ResultScene
import ru.tatarcha.tatlearn.feature.showcase.ShowcaseScreen
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.showcase_open
import tatlearn.shared.generated.resources.showcase_open_short

// Не-JVM таргеты не умеют рефлексией находить подклассы NavKey, поэтому они регистрируются явно.
private val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(OnboardingKey::class)
            subclass(AuthKey::class)
            subclass(HomeKey::class)
            subclass(ModuleKey::class)
            subclass(LessonKey::class)
            subclass(ResultKey::class)
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
                    OnboardingScene(onNavigateToAuth = { mode -> backStack.add(AuthKey(mode)) })
                }
                entry<AuthKey> { key ->
                    AuthScene(
                        mode = key.mode,
                        onNavigateToHome = { resetTo(HomeKey) },
                    )
                }
                entry<HomeKey> {
                    HomeScene(
                        onStartLesson = { lessonId -> backStack.add(LessonKey(lessonId)) },
                        onOpenModule = { moduleId -> backStack.add(ModuleKey(moduleId)) },
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
                entry<ShowcaseKey> {
                    ShowcaseScreen(viewModel = koinViewModel(), onClose = ::pop)
                }
            },
        )
        if (showDebugTools && backStack.lastOrNull() != ShowcaseKey) {
            // Отладочная кнопка UI kit: плавающая снизу справа, над нижним меню (в релизной сборке не показывается).
            val fabDescription = stringResource(Res.string.showcase_open)
            SmallFloatingActionButton(
                onClick = { backStack.add(ShowcaseKey) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .safeContentPadding()
                    .padding(end = TamyrTheme.spacing.m, bottom = TamyrSizes.debugFabBottomInset)
                    .size(TamyrSizes.debugFab)
                    .semantics { contentDescription = fabDescription },
                shape = TamyrTheme.shapes.circle,
                containerColor = TamyrTheme.colors.primary,
                contentColor = TamyrTheme.colors.onPrimary,
            ) {
                Text(text = stringResource(Res.string.showcase_open_short), style = TamyrTheme.typography.label12Bold)
            }
        }
    }
}
