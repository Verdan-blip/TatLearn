package ru.tatarcha.tatlearn.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.designsystem.component.BottomBarItem
import ru.tatarcha.tatlearn.core.designsystem.component.LoadStateContent
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.StreakBadge
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrBottomBar
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrProgressBar
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.CurrentLesson
import ru.tatarcha.tatlearn.domain.model.HomeContent
import ru.tatarcha.tatlearn.domain.model.LearningModule
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.UserStats
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.home_chip_alphabet
import tatlearn.shared.generated.resources.home_chip_numbers
import tatlearn.shared.generated.resources.home_continue_tag
import tatlearn.shared.generated.resources.home_greeting
import tatlearn.shared.generated.resources.home_lesson_button
import tatlearn.shared.generated.resources.home_lesson_progress
import tatlearn.shared.generated.resources.home_level_left
import tatlearn.shared.generated.resources.home_module_done
import tatlearn.shared.generated.resources.home_modules
import tatlearn.shared.generated.resources.home_modules_all
import tatlearn.shared.generated.resources.home_nav_contest
import tatlearn.shared.generated.resources.home_nav_dictionary
import tatlearn.shared.generated.resources.home_nav_home
import tatlearn.shared.generated.resources.home_nav_profile
import tatlearn.shared.generated.resources.home_streak_days
import tatlearn.shared.generated.resources.home_xp

private val AvatarSize = 46.dp
private val ChipSize = 34.dp
private val LessonProgressHeight = 8.dp
private val ModuleProgressHeight = 6.dp
private val LessonButtonHeight = 46.dp

@Composable
fun HomeRoute(onStartLesson: (lessonId: String) -> Unit, viewModel: HomeViewModel = koinViewModel()) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToLesson -> onStartLesson(effect.lessonId)
            }
        }
    }
    val state by viewModel.state.collectAsState()
    HomeScreen(state, viewModel::onEvent)
}

@Composable
fun HomeScreen(state: HomeUiState, onEvent: (HomeUiEvent) -> Unit) {
    LoadStateContent(
        state = state.content,
        onRetry = { onEvent(HomeUiEvent.RetryClicked) },
        modifier = Modifier.fillMaxSize().background(TamyrTheme.colors.background).statusBarsPadding(),
    ) { content ->
        HomeContentLayout(state.userName, state.stats, content, onEvent)
    }
}

@Composable
private fun HomeContentLayout(userName: String, stats: UserStats, content: HomeContent, onEvent: (HomeUiEvent) -> Unit) {
    val spacing = TamyrTheme.spacing
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = spacing.screen),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Header(userName, Modifier.padding(top = spacing.l))
            StreakCard(stats)
            CurrentLessonCard(content.currentLesson) { onEvent(HomeUiEvent.StartLessonClicked) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(stringResource(Res.string.home_modules), style = TamyrTheme.typography.body17Bold, color = TamyrTheme.colors.textPrimary)
                // TODO(design): у «Все модули» нет целевого экрана.
                Text(stringResource(Res.string.home_modules_all), style = TamyrTheme.typography.body14Bold, color = TamyrTheme.colors.accentText)
            }
            ModulesGrid(content.modules) { onEvent(HomeUiEvent.ModuleClicked(it)) }
            Box(Modifier.height(spacing.s))
        }
        HomeBottomBar()
    }
}

@Composable
private fun Header(userName: String, modifier: Modifier = Modifier) {
    val colors = TamyrTheme.colors
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(Res.string.home_greeting), style = TamyrTheme.typography.body14, color = colors.textSecondary)
            Text(userName, style = TamyrTheme.typography.headline24, color = colors.textPrimary)
        }
        Box(
            Modifier.size(AvatarSize).clip(CircleShape).background(colors.successContainer).border(TamyrSizes.borderThin, colors.border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(userName.take(1).toUpperCase(Locale.current), style = TamyrTheme.typography.body17Bold, color = colors.primary)
        }
    }
}

@Composable
private fun StreakCard(stats: UserStats) {
    val colors = TamyrTheme.colors
    TamyrCard(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.l), verticalAlignment = Alignment.CenterVertically) {
            StreakBadge(stats.streak)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xs)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(stringResource(Res.string.home_streak_days, stats.streak), style = TamyrTheme.typography.body15Bold, color = colors.textPrimary)
                    Text(stringResource(Res.string.home_xp, stats.xp), style = TamyrTheme.typography.caption13Bold, color = colors.goldText)
                }
                TamyrProgressBar(
                    progress = (UserStats.XP_PER_LEVEL - stats.xpToNextLevel) / UserStats.XP_PER_LEVEL.toFloat(),
                    height = LessonProgressHeight,
                    color = colors.gold,
                )
                Text(
                    stringResource(Res.string.home_level_left, stats.level + 1, stats.xpToNextLevel),
                    style = TamyrTheme.typography.label12,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun CurrentLessonCard(current: CurrentLesson, onStart: () -> Unit) {
    val colors = TamyrTheme.colors
    TamyrCard(
        Modifier.fillMaxWidth(),
        containerColor = colors.successContainer,
        contentPadding = TamyrTheme.spacing.l,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.home_continue_tag).toUpperCase(Locale.current),
                    style = TamyrTheme.typography.tag.copy(fontSize = TamyrTheme.typography.caption13Bold.fontSize),
                    color = colors.primary,
                )
                Text(
                    stringResource(Res.string.home_lesson_progress, current.completedExercises, current.totalExercises),
                    style = TamyrTheme.typography.caption13,
                    color = colors.primary,
                )
            }
            Text("${current.topicTt} · ${current.topicRu}", style = TamyrTheme.typography.title19, color = colors.textPrimary)
            TamyrProgressBar(current.progress, height = LessonProgressHeight, trackColor = colors.surface)
            LessonButton(stringResource(Res.string.home_lesson_button, current.lesson.number, current.lesson.titleTt), onStart)
        }
    }
}

/** Кнопка в карточке урока — укороченный вариант главной кнопки (46dp, скругление 12dp). */
@Composable
private fun LessonButton(text: String, onClick: () -> Unit) {
    val colors = TamyrTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .height(LessonButtonHeight)
            .clip(TamyrTheme.shapes.chip)
            .background(colors.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = TamyrTheme.typography.body15Bold, color = colors.onPrimary)
    }
}

@Composable
private fun ModulesGrid(modules: List<LearningModule>, onClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m)) {
        modules.chunked(2).forEach { rowModules ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m)) {
                rowModules.forEach { module -> ModuleTile(module, Modifier.weight(1f).fillMaxHeight()) { onClick(module.id) } }
                if (rowModules.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ModuleTile(module: LearningModule, modifier: Modifier, onClick: () -> Unit) {
    val colors = TamyrTheme.colors
    TamyrCard(
        modifier,
        onClick = onClick,
        shape = TamyrTheme.shapes.tile,
        containerColor = if (module.isLocked) colors.surfaceMuted else colors.surface,
        contentPadding = 14.dp,
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
            ModuleChip(module)
            // В макете на закрытых плитках показано только татарское название.
            val title = if (module.isLocked || module.titleTt == module.titleRu) module.titleTt else "${module.titleTt} · ${module.titleRu}"
            Text(title, style = TamyrTheme.typography.body15Bold, color = if (module.isLocked) colors.textSecondary else colors.textPrimary)
            when {
                module.isLocked -> module.lockHint?.let {
                    Text(it, style = TamyrTheme.typography.label12, color = colors.textSecondary)
                }
                module.progressPercent >= 100 -> Text(
                    stringResource(Res.string.home_module_done, module.progressPercent),
                    style = TamyrTheme.typography.label12Bold,
                    color = colors.primary,
                )
                else -> TamyrProgressBar(
                    progress = module.progressPercent / 100f,
                    height = ModuleProgressHeight,
                    color = if (module.icon == ICON_FOOD) colors.accent else colors.primary,
                )
            }
        }
    }
}

// Ключи иконок приходят из слоя данных; каждому ключу соответствует один чип в дизайне.
private const val ICON_ALPHABET = "alphabet"
private const val ICON_FOOD = "food"
private const val ICON_NUMBERS = "numbers"

@Composable
private fun ModuleChip(module: LearningModule) {
    val colors = TamyrTheme.colors
    val background: Color = when {
        module.isLocked -> colors.background
        module.icon == ICON_FOOD -> colors.errorContainer
        else -> colors.successContainer
    }
    Box(Modifier.size(ChipSize).clip(TamyrTheme.shapes.chip).background(background), contentAlignment = Alignment.Center) {
        when {
            module.isLocked -> LockIcon(colors.textSecondary)
            module.icon == ICON_FOOD -> BowlIcon(colors.accent)
            else -> Text(
                when (module.icon) {
                    ICON_ALPHABET -> stringResource(Res.string.home_chip_alphabet)
                    ICON_NUMBERS -> stringResource(Res.string.home_chip_numbers)
                    else -> module.titleTt.take(1)
                },
                style = TamyrTheme.typography.body15Bold,
                color = colors.primary,
            )
        }
    }
}

@Composable
private fun HomeBottomBar() {
    // TODO(design): существует только «Главная»; у остальных вкладок пока нет экранов.
    TamyrBottomBar(
        listOf(
            BottomBarItem(stringResource(Res.string.home_nav_home), selected = true, onClick = {}) { HomeNavIcon(it) },
            BottomBarItem(stringResource(Res.string.home_nav_dictionary), selected = false, onClick = {}) { BookNavIcon(it) },
            BottomBarItem(stringResource(Res.string.home_nav_contest), selected = false, onClick = {}) { TrophyNavIcon(it) },
            BottomBarItem(stringResource(Res.string.home_nav_profile), selected = false, onClick = {}) { PersonNavIcon(it) },
        ),
    )
}

// region Превью

private const val Group = "Home"

private val previewModules = listOf(
    LearningModule("alphabet", "Алфавит", "Алфавит", "alphabet", progressPercent = 100, lockedBy = null, lockHint = null),
    LearningModule("food", "Ашлар", "Еда", "food", progressPercent = 35, lockedBy = null, lockHint = null),
    LearningModule("numbers", "Саннар", "Числа", "numbers", progressPercent = 10, lockedBy = null, lockHint = null),
    LearningModule("nature", "Табигать", "Природа", "nature", progressPercent = 0, lockedBy = "food", lockHint = "Откроется после «Еды»"),
)

private fun previewContent(
    modules: List<LearningModule> = previewModules,
    completed: Int = 3,
    total: Int = 5,
) = HomeContent(modules, CurrentLesson(Lesson("lesson_4", 4, "Туганнар", "Семья"), "Гаилә", "Семья", completed, total))

@Composable
private fun HomePreview(state: HomeUiState) = PreviewFrame { HomeScreen(state) {} }

@Preview(name = "Loading", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun HomeLoadingPreview() = HomePreview(HomeUiState(userName = "Алия"))

@Preview(name = "Error", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun HomeErrorPreview() = HomePreview(HomeUiState(userName = "Алия", content = LoadState.Error("NET_TIMEOUT")))

@Preview(name = "Content", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun HomeContentPreview() = HomePreview(
    HomeUiState("Алия", UserStats(xp = 340, streak = 12), LoadState.Content(previewContent())),
)

@Preview(name = "New user (no stats, lesson not started)", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun HomeNewUserPreview() = HomePreview(
    HomeUiState(
        "Алия",
        UserStats.Empty,
        LoadState.Content(previewContent(previewModules.map { it.copy(progressPercent = 0) }.take(2), completed = 0)),
    ),
)

@Preview(name = "All modules done, lesson finished", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun HomeAllDonePreview() = HomePreview(
    HomeUiState(
        "Алия",
        UserStats(xp = 1990, streak = 100),
        LoadState.Content(previewContent(previewModules.map { it.copy(progressPercent = 100, lockedBy = null, lockHint = null) }, completed = 5)),
    ),
)

@Preview(name = "Long name and titles", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun HomeLongTextPreview() = HomePreview(
    HomeUiState(
        "Александра-Мөхәммәдия",
        UserStats(xp = 99, streak = 1),
        LoadState.Content(
            previewContent(
                listOf(
                    LearningModule("a", "Бик озын исемле модуль", "Очень длинное название модуля", "alphabet", 50, null, null),
                    LearningModule("b", "Табигать һәм һава торышы", "Природа и погода", "nature", 0, "a", "Откроется после «Очень длинное название модуля»"),
                ),
            ),
        ),
    ),
)

// endregion

