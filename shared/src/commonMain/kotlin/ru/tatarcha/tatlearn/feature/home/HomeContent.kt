package ru.tatarcha.tatlearn.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.designsystem.component.BottomBarItem
import ru.tatarcha.tatlearn.core.designsystem.component.ComingSoonState
import ru.tatarcha.tatlearn.core.designsystem.component.LoadStateContent
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrBottomBar
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastData
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastHost
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastKind
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.RoadmapBuilder
import ru.tatarcha.tatlearn.domain.model.CardGain
import ru.tatarcha.tatlearn.domain.model.CurrentLesson
import ru.tatarcha.tatlearn.domain.model.HomeCardKind
import ru.tatarcha.tatlearn.domain.model.HomeContent
import ru.tatarcha.tatlearn.domain.model.LearningModule
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.ProgressGain
import ru.tatarcha.tatlearn.domain.model.UserStats
import ru.tatarcha.tatlearn.feature.home.roadmap.CurrentCardVisibility
import ru.tatarcha.tatlearn.feature.home.roadmap.ModuleRoadmap
import ru.tatarcha.tatlearn.feature.home.roadmap.RoadmapFloatingLesson
import ru.tatarcha.tatlearn.feature.home.roadmap.RoadmapHeader
import ru.tatarcha.tatlearn.feature.home.roadmap.rememberCurrentCardVisibility
import ru.tatarcha.tatlearn.feature.home.roadmap.rememberRoadmapAnimation
import ru.tatarcha.tatlearn.feature.home.roadmap.rememberRoadmapListState
import ru.tatarcha.tatlearn.feature.home.roadmap.roadmapCurrentItemIndex
import ru.tatarcha.tatlearn.feature.home.roadmap.roadmapScrollTarget
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.home_contest_soon_message
import tatlearn.shared.generated.resources.home_contest_soon_title
import tatlearn.shared.generated.resources.home_dictionary_soon_message
import tatlearn.shared.generated.resources.home_dictionary_soon_title
import tatlearn.shared.generated.resources.home_lesson_button
import tatlearn.shared.generated.resources.home_nav_contest
import tatlearn.shared.generated.resources.home_nav_dictionary
import tatlearn.shared.generated.resources.home_nav_home
import tatlearn.shared.generated.resources.home_nav_profile
import tatlearn.shared.generated.resources.roadmap_locked_toast

@Composable
fun HomeContent(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    /** Содержимое вкладки «Профиль»; нижнее меню рисует главная. */
    profileTab: @Composable (Modifier) -> Unit = {},
    /** Включена системная настройка «уменьшить анимацию»: итог показывается сразу, без движения. */
    reduceMotion: Boolean = false,
    /** Лёгкий гаптик, когда после модуля открылся следующий. */
    onUnlocked: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.tab == HomeNavItem.Home) {
            LoadStateContent(
                state = state.content,
                onRetry = { onEvent(HomeUiEvent.RetryClicked) },
                modifier = Modifier
                    .fillMaxSize()
                    .background(TamyrTheme.colors.background)
                    .statusBarsPadding(),
            ) { content ->
                HomeLoaded(
                    userName = state.userName,
                    stats = state.stats,
                    content = content,
                    progressGain = state.progressGain,
                    reduceMotion = reduceMotion,
                    onUnlocked = onUnlocked,
                    onEvent = onEvent,
                )
            }
        } else if (state.tab == HomeNavItem.Profile) {
            Column(Modifier.fillMaxSize().background(TamyrTheme.colors.background).statusBarsPadding()) {
                profileTab(Modifier.weight(1f))
                HomeBottomBar(selected = HomeNavItem.Profile, onItemClick = { item -> onEvent(HomeUiEvent.NavItemClicked(item)) })
            }
        } else {
            ComingSoonTab(tab = state.tab, onEvent = onEvent)
        }
        TamyrToastHost(
            toast = state.toast?.let { toast -> homeToastData(toast) },
            onDismiss = { onEvent(HomeUiEvent.ToastDismissed) },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun homeToastData(toast: HomeToast): TamyrToastData = TamyrToastData(
    text = when (toast) {
        is HomeToast.ModuleLocked -> stringResource(Res.string.roadmap_locked_toast, toast.afterTitle)
    },
    kind = TamyrToastKind.Info,
)

/** Заглушка вкладки, которой ещё нет (экран 16); нижнее меню остаётся, «Өй» ведёт на главную. */
@Composable
private fun ComingSoonTab(
    tab: HomeNavItem,
    onEvent: (HomeUiEvent) -> Unit,
) {
    val (title, message) = when (tab) {
        HomeNavItem.Contest -> stringResource(Res.string.home_contest_soon_title) to stringResource(Res.string.home_contest_soon_message)
        else -> stringResource(Res.string.home_dictionary_soon_title) to stringResource(Res.string.home_dictionary_soon_message)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TamyrTheme.colors.background)
            .statusBarsPadding(),
    ) {
        ComingSoonState(title = title, message = message, modifier = Modifier.weight(1f))
        HomeBottomBar(selected = tab, onItemClick = { item -> onEvent(HomeUiEvent.NavItemClicked(item)) })
    }
}

@Composable
private fun HomeLoaded(
    userName: String,
    stats: UserStats,
    content: HomeContent,
    progressGain: ProgressGain?,
    reduceMotion: Boolean,
    onUnlocked: () -> Unit,
    onEvent: (HomeUiEvent) -> Unit,
) {
    val rows = remember(content) { RoadmapBuilder.build(content) }
    val listState = rememberRoadmapListState(rows)
    val cardIndex = roadmapCurrentItemIndex(rows)
    val visibility by rememberCurrentCardVisibility(listState, cardIndex)
    val scope = rememberCoroutineScope()
    val current = content.currentLesson
    val animation = rememberRoadmapAnimation(
        gain = progressGain,
        reduceMotion = reduceMotion,
        // Анимация ждёт, пока карточка не окажется на экране, но не дольше трёх секунд (design/home-animations.md, D).
        awaitVisible = {
            withTimeoutOrNull(VISIBLE_WAIT_MS) {
                snapshotFlow { visibility }.first { it == CurrentCardVisibility.Visible }
            } != null
        },
        onUnlocked = onUnlocked,
        onStarted = { onEvent(HomeUiEvent.ProgressGainStarted) },
        onFinished = { onEvent(HomeUiEvent.ProgressGainFinished) },
    )
    // Текущий модуль сменился (открылся следующий): лента встаёт на него. Возврат с экрана модуля позицию не трогает.
    var scrolledFor by rememberSaveable { mutableStateOf(current.moduleId) }
    LaunchedEffect(current.moduleId) {
        if (scrolledFor != current.moduleId) {
            listState.scrollToItem(roadmapScrollTarget(rows))
            scrolledFor = current.moduleId
        }
    }
    // Проявление при первом открытии (A1); при возврате с экрана модуля лента не мигает.
    var introPlayed by rememberSaveable { mutableStateOf(reduceMotion) }
    val intro = remember { Animatable(if (introPlayed) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!introPlayed) {
            intro.animateTo(1f, tween(INTRO_MS, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)))
            introPlayed = true
        }
    }
    var pointsUp by remember { mutableStateOf(true) }
    if (visibility != CurrentCardVisibility.Visible) pointsUp = visibility == CurrentCardVisibility.Above
    val showFloating = current.kind != HomeCardKind.AllDone && visibility != CurrentCardVisibility.Visible
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            ModuleRoadmap(
                rows = rows,
                current = current,
                totalModules = content.modules.size,
                header = {
                    RoadmapHeader(
                        userName = userName,
                        stats = stats,
                    )
                },
                onModuleClick = { moduleId -> onEvent(HomeUiEvent.ModuleClicked(moduleId)) },
                onStartLesson = { onEvent(HomeUiEvent.StartLessonClicked) },
                state = listState,
                animation = animation,
                reduceMotion = reduceMotion,
                modifier = Modifier.graphicsLayer { alpha = intro.value },
            )
            FloatingLesson(
                visible = showFloating,
                reduceMotion = reduceMotion,
                lessonText = stringResource(Res.string.home_lesson_button, current.lesson.number, current.lesson.titleTt),
                pointsUp = pointsUp,
                onStart = { onEvent(HomeUiEvent.StartLessonClicked) },
                onScrollToCurrent = { scope.launch { listState.animateScrollToItem(cardIndex) } },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        HomeBottomBar(selected = HomeNavItem.Home, onItemClick = { item -> onEvent(HomeUiEvent.NavItemClicked(item)) })
    }
}

/** Плавающая панель появляется и исчезает мягко: затухание и сдвиг на четверть высоты за 200 мс. */
@Composable
private fun FloatingLesson(
    visible: Boolean,
    reduceMotion: Boolean,
    lessonText: String,
    pointsUp: Boolean,
    onStart: () -> Unit,
    onScrollToCurrent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (reduceMotion) EnterTransition.None else fadeIn(tween(FLOATING_MS)) + slideInVertically(tween(FLOATING_MS)) { it / 4 },
        exit = if (reduceMotion) ExitTransition.None else fadeOut(tween(FLOATING_MS)) + slideOutVertically(tween(FLOATING_MS)) { it / 4 },
    ) {
        RoadmapFloatingLesson(
            lessonText = lessonText,
            pointsUp = pointsUp,
            onStart = onStart,
            onScrollToCurrent = onScrollToCurrent,
        )
    }
}

private const val VISIBLE_WAIT_MS = 3_000L
private const val INTRO_MS = 200
private const val FLOATING_MS = 200

@Composable
private fun HomeBottomBar(selected: HomeNavItem, onItemClick: (HomeNavItem) -> Unit) {
    TamyrBottomBar(
        items = listOf(
            BottomBarItem(stringResource(Res.string.home_nav_home), selected = selected == HomeNavItem.Home, onClick = { onItemClick(HomeNavItem.Home) }) { HomeNavIcon(it) },
            BottomBarItem(stringResource(Res.string.home_nav_dictionary), selected = selected == HomeNavItem.Dictionary, onClick = { onItemClick(HomeNavItem.Dictionary) }) { BookNavIcon(it) },
            BottomBarItem(stringResource(Res.string.home_nav_contest), selected = selected == HomeNavItem.Contest, onClick = { onItemClick(HomeNavItem.Contest) }) { TrophyNavIcon(it) },
            BottomBarItem(stringResource(Res.string.home_nav_profile), selected = selected == HomeNavItem.Profile, onClick = { onItemClick(HomeNavItem.Profile) }) { PersonNavIcon(it) },
        ),
    )
}

private const val PREVIEW_GROUP = "Home"

private val previewModules = listOf(
    LearningModule("meeting", "Таныштыру", "Знакомство", "meeting", 100, null, null, null, completedLessons = 1, totalLessons = 1),
    LearningModule("alphabet", "Алфавит", "Алфавит", "alphabet", 100, null, null, null, completedLessons = 2, totalLessons = 2),
    LearningModule("food", "Ашлар", "Еда", "food", 60, null, null, null, completedLessons = 3, totalLessons = 5),
    LearningModule("numbers", "Саннар", "Числа", "numbers", 0, "food", "Ашлар", "Еда", completedLessons = 0, totalLessons = 5),
    LearningModule("nature", "Табигать", "Природа", "nature", 0, "numbers", "Саннар", "Числа", completedLessons = 0, totalLessons = 4),
)

private fun previewContent(
    modules: List<LearningModule> = previewModules,
    completed: Int = 3,
    total: Int = 5,
    kind: HomeCardKind = HomeCardKind.Continue,
    restartsLesson: Boolean = false,
    moduleId: String = "food",
) = HomeContent(
    modules,
    CurrentLesson(Lesson("lesson_4", 4, "Туганнар", "Семья"), "Ашлар", "Еда", completed, total, kind, restartsLesson, moduleId),
)

@Composable
private fun HomePreview(state: HomeUiState) = PreviewFrame {
    HomeContent(
        state = state,
        onEvent = {},
    )
}

@Preview(name = "Loading", group = PREVIEW_GROUP)
@Composable
private fun HomeLoadingPreview() = HomePreview(HomeUiState(userName = "Алия"))

@Preview(name = "Error", group = PREVIEW_GROUP)
@Composable
private fun HomeErrorPreview() = HomePreview(HomeUiState(userName = "Алия", content = LoadState.Error("NET_TIMEOUT")))

@Preview(name = "Roadmap, current in the middle", group = PREVIEW_GROUP)
@Composable
private fun HomeContentPreview() = HomePreview(
    HomeUiState("Алия", UserStats(xp = 340, streak = 12), LoadState.Content(previewContent())),
)

@Preview(name = "Progress gain (marked as shown, no animation yet)", group = PREVIEW_GROUP)
@Composable
private fun HomeProgressGainPreview() = HomePreview(
    HomeUiState(
        userName = "Алия",
        stats = UserStats(xp = 340, streak = 12),
        content = LoadState.Content(previewContent()),
        progressGain = ProgressGain(moduleId = "food", fromPercent = 40, toPercent = 60, card = CardGain(fromCompleted = 2, toCompleted = 3, total = 5)),
    ),
)

@Preview(name = "Dictionary tab (coming soon)", group = PREVIEW_GROUP)
@Composable
private fun HomeComingSoonPreview() = HomePreview(
    HomeUiState(
        userName = "Алия",
        stats = UserStats(xp = 340, streak = 12),
        content = LoadState.Content(previewContent()),
        tab = HomeNavItem.Dictionary,
    ),
)

@Preview(name = "First launch (start of the course)", group = PREVIEW_GROUP)
@Composable
private fun HomeStartPreview() = HomePreview(
    HomeUiState(
        "Алия",
        UserStats.Empty,
        LoadState.Content(
            previewContent(
                modules = previewModules.map { it.copy(completedLessons = 0, progressPercent = 0) }.mapIndexed { index, module ->
                    if (index == 0) module.copy(lockedBy = null) else module.copy(lockedBy = "prev", lockedByTitleTt = "Таныштыру", lockedByTitleRu = "Знакомство")
                },
                completed = 0,
                total = 1,
                kind = HomeCardKind.Start,
                moduleId = "meeting",
            ),
        ),
    ),
)

@Preview(name = "Course done", group = PREVIEW_GROUP)
@Composable
private fun HomeAllDonePreview() = HomePreview(
    HomeUiState(
        "Алия",
        UserStats(xp = 1990, streak = 100),
        LoadState.Content(
            previewContent(
                previewModules.map { it.copy(progressPercent = 100, lockedBy = null, lockedByTitleTt = null, lockedByTitleRu = null, completedLessons = it.totalLessons) },
                completed = 4,
                kind = HomeCardKind.AllDone,
            ),
        ),
    ),
)

@Preview(name = "Started lesson restarts", group = PREVIEW_GROUP)
@Composable
private fun HomeRestartPreview() = HomePreview(
    HomeUiState("Алия", UserStats(xp = 340, streak = 12), LoadState.Content(previewContent(restartsLesson = true))),
)

@Preview(name = "Long name and titles", group = PREVIEW_GROUP)
@Composable
private fun HomeLongTextPreview() = HomePreview(
    HomeUiState(
        "Александра-Мөхәммәдия",
        UserStats(xp = 99, streak = 1),
        LoadState.Content(
            previewContent(
                modules = listOf(
                    LearningModule("a", "Бик озын исемле модуль", "Очень длинное название модуля", "alphabet", 50, null, null, null, completedLessons = 1, totalLessons = 2),
                    LearningModule("b", "Табигать һәм һава торышы", "Природа и погода", "nature", 0, "a", "Бик озын исемле модуль", "Очень длинное название модуля", completedLessons = 0, totalLessons = 3),
                ),
                completed = 1,
                total = 2,
                moduleId = "a",
            ),
        ),
    ),
)
