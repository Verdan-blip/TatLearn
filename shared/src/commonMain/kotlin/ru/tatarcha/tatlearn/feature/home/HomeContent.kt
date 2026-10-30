package ru.tatarcha.tatlearn.feature.home

import androidx.compose.foundation.background
import tatlearn.shared.generated.resources.home_streak_invite
import tatlearn.shared.generated.resources.home_start_tag
import tatlearn.shared.generated.resources.home_repeat_lesson_button
import tatlearn.shared.generated.resources.home_new_module_tag
import tatlearn.shared.generated.resources.home_level_short
import tatlearn.shared.generated.resources.home_lesson_restart_hint
import tatlearn.shared.generated.resources.home_done_tag
import tatlearn.shared.generated.resources.home_all_done_title
import ru.tatarcha.tatlearn.core.designsystem.component.BrandDiamonds
import org.jetbrains.compose.resources.pluralStringResource
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import ru.tatarcha.tatlearn.domain.model.HomeCardKind
import ru.tatarcha.tatlearn.domain.model.AvatarPose
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.designsystem.component.BottomBarItem
import ru.tatarcha.tatlearn.core.designsystem.component.ComingSoonState
import ru.tatarcha.tatlearn.core.designsystem.component.LoadStateContent
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.StreakBadge
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrBottomBar
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrProgressBar
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastData
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastHost
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrToastKind
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.CurrentLesson
import ru.tatarcha.tatlearn.feature.profile.ProfileAvatar
import ru.tatarcha.tatlearn.domain.model.HomeContent
import ru.tatarcha.tatlearn.domain.model.CardGain
import ru.tatarcha.tatlearn.domain.model.LearningModule
import ru.tatarcha.tatlearn.domain.model.ProgressGain
import ru.tatarcha.tatlearn.domain.model.moduleTitle
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.UserStats
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.home_chip_alphabet
import tatlearn.shared.generated.resources.home_chip_numbers
import tatlearn.shared.generated.resources.home_contest_soon_message
import tatlearn.shared.generated.resources.home_contest_soon_title
import tatlearn.shared.generated.resources.home_continue_tag
import tatlearn.shared.generated.resources.home_dictionary_soon_message
import tatlearn.shared.generated.resources.home_dictionary_soon_title
import tatlearn.shared.generated.resources.home_greeting
import tatlearn.shared.generated.resources.home_lessons_count
import tatlearn.shared.generated.resources.home_lesson_button
import tatlearn.shared.generated.resources.home_lesson_progress
import tatlearn.shared.generated.resources.home_level_xp
import tatlearn.shared.generated.resources.home_module_done
import tatlearn.shared.generated.resources.module_locked_hint
import tatlearn.shared.generated.resources.home_modules
import tatlearn.shared.generated.resources.home_modules_all
import tatlearn.shared.generated.resources.home_nav_contest
import tatlearn.shared.generated.resources.home_nav_dictionary
import tatlearn.shared.generated.resources.home_nav_home
import tatlearn.shared.generated.resources.home_nav_profile
import tatlearn.shared.generated.resources.home_streak_days
import tatlearn.shared.generated.resources.home_toast_all_unavailable

private const val ICON_ALPHABET = "alphabet"
private const val ICON_FOOD = "food"
private const val ICON_NUMBERS = "numbers"
private val ModuleCardPadding = 14.dp

@Composable
fun HomeContent(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    /** Содержимое вкладки «Профиль»; нижнее меню рисует главная. */
    profileTab: @Composable (Modifier) -> Unit = {},
    /** Включена системная настройка «уменьшить анимацию»: «доезд» прогресса сразу показывает итог. */
    reduceMotion: Boolean = false,
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
                    avatar = state.avatar,
                    stats = state.stats,
                    content = content,
                    progressGain = state.progressGain,
                    reduceMotion = reduceMotion,
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
    avatar: Avatar,
    stats: UserStats,
    content: HomeContent,
    progressGain: ProgressGain?,
    reduceMotion: Boolean,
    onEvent: (HomeUiEvent) -> Unit,
) {
    val gainAnimation = rememberGainAnimation(
        gain = progressGain,
        reduceMotion = reduceMotion,
        onStarted = { onEvent(HomeUiEvent.ProgressGainStarted) },
        onFinished = { onEvent(HomeUiEvent.ProgressGainFinished) },
    )
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = TamyrTheme.spacing.screen,
                    end = TamyrTheme.spacing.screen,
                    bottom = TamyrTheme.spacing.s,
                ),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.blockGap),
        ) {
            Header(
                userName = userName,
                avatar = avatar,
                onAvatarClick = { onEvent(HomeUiEvent.NavItemClicked(HomeNavItem.Profile)) },
                modifier = Modifier.padding(top = TamyrTheme.spacing.l),
            )
            CurrentLessonCard(
                current = content.currentLesson,
                animation = gainAnimation,
                onStart = { onEvent(HomeUiEvent.StartLessonClicked) },
            )
            if (content.currentLesson.kind == HomeCardKind.Start) StreakInviteCard(stats = stats) else StreakCard(stats = stats)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = stringResource(Res.string.home_modules),
                    style = TamyrTheme.typography.body17Bold,
                    color = TamyrTheme.colors.textPrimary,
                )
                Text(
                    text = stringResource(Res.string.home_modules_all),
                    style = TamyrTheme.typography.body14Bold,
                    color = TamyrTheme.colors.accentText,
                    modifier = Modifier.clickable(role = Role.Button) { onEvent(HomeUiEvent.AllModulesClicked) },
                )
            }
            ModulesGrid(
                modules = content.modules,
                highlightedId = content.modules.firstOrNull { !it.isLocked }?.id.takeIf { content.currentLesson.kind == HomeCardKind.Start },
                animation = gainAnimation,
                onClick = { moduleId -> onEvent(HomeUiEvent.ModuleClicked(moduleId)) },
            )
        }
        HomeBottomBar(selected = HomeNavItem.Home, onItemClick = { item -> onEvent(HomeUiEvent.NavItemClicked(item)) })
    }
}

@Composable
private fun homeToastData(toast: HomeToast): TamyrToastData = TamyrToastData(
    text = when (toast) {
        HomeToast.AllModulesUnavailable -> stringResource(Res.string.home_toast_all_unavailable)
    },
    kind = TamyrToastKind.Info,
)

@Composable
private fun Header(
    userName: String,
    avatar: Avatar,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
            Text(
                text = stringResource(Res.string.home_greeting),
                style = TamyrTheme.typography.body14,
                color = TamyrTheme.colors.textSecondary,
            )
            Text(
                text = userName,
                style = TamyrTheme.typography.headline24,
                color = TamyrTheme.colors.textPrimary,
            )
        }
        // Аватар из профиля; нажатие открывает вкладку «Профиль».
        ProfileAvatar(
            avatar = avatar,
            name = userName,
            size = TamyrSizes.homeAvatar,
            modifier = Modifier.clickable(role = Role.Button, onClick = onAvatarClick),
        )
    }
}

/** Новичку вместо «Серия: 0 көн» приглашение: нулевая серия выглядит упрёком (макет 34). */
@Composable
private fun StreakInviteCard(stats: UserStats) {
    val colors = TamyrTheme.colors
    TamyrCard(modifier = Modifier.fillMaxWidth(), contentPadding = TamyrTheme.spacing.m) {
        Row(
            modifier = Modifier.padding(horizontal = TamyrTheme.spacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.blockGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(TamyrSizes.streakBadgeCompact).clip(TamyrTheme.shapes.chip).background(colors.goldContainer),
                contentAlignment = Alignment.Center,
            ) { DiamondOutline(colors.goldText) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xs)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(stringResource(Res.string.home_streak_invite), style = TamyrTheme.typography.body15Bold, color = colors.textPrimary)
                    Text(stringResource(Res.string.home_level_short, stats.level), style = TamyrTheme.typography.caption13Bold, color = colors.goldText)
                }
                TamyrProgressBar(
                    progress = (UserStats.XP_PER_LEVEL - stats.xpToNextLevel) / UserStats.XP_PER_LEVEL.toFloat(),
                    height = TamyrSizes.moduleProgress,
                    color = colors.gold,
                )
            }
        }
    }
}

/** Контурный ромб: серия ещё не началась. */
@Composable
private fun DiamondOutline(color: Color) {
    Canvas(Modifier.size(DiamondOutlineSize)) {
        val w = size.width
        val path = Path().apply {
            moveTo(w / 2, w * 0.125f)
            lineTo(w * 0.875f, w / 2)
            lineTo(w / 2, w * 0.875f)
            lineTo(w * 0.125f, w / 2)
            close()
        }
        drawPath(path, color, style = Stroke(width = w * 0.1f, join = StrokeJoin.Round))
    }
}

private val DiamondOutlineSize = 22.dp

@Composable
private fun StreakCard(stats: UserStats) {
    TamyrCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = TamyrTheme.spacing.m,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = TamyrTheme.spacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.blockGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StreakBadge(
                days = stats.streak,
                size = TamyrSizes.streakBadgeCompact,
                textStyle = TamyrTheme.typography.title18,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xs),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = stringResource(Res.string.home_streak_days, stats.streak),
                        style = TamyrTheme.typography.body15Bold,
                        color = TamyrTheme.colors.textPrimary,
                    )
                    Text(
                        text = stringResource(Res.string.home_level_xp, stats.level, stats.xp),
                        style = TamyrTheme.typography.caption13Bold,
                        color = TamyrTheme.colors.goldText,
                    )
                }
                TamyrProgressBar(
                    progress = (UserStats.XP_PER_LEVEL - stats.xpToNextLevel) / UserStats.XP_PER_LEVEL.toFloat(),
                    height = TamyrSizes.moduleProgress,
                    color = TamyrTheme.colors.gold,
                )
            }
        }
    }
}

/** Карточка «Продолжить» и поверх неё подпись «+1 урок», когда после урока прибавился ровно один урок этого модуля. */
@Composable
private fun CurrentLessonCard(
    current: CurrentLesson,
    animation: GainAnimation,
    onStart: () -> Unit,
) {
    val cardGain = animation.gain?.takeIf { it.moduleId == current.moduleId }?.card
    Box {
        CurrentLessonCardBody(
            current = current,
            cardGain = cardGain,
            animation = animation,
            onStart = onStart,
        )
        if (cardGain?.showsChip == true) {
            GainChip(
                animation = animation,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = TamyrTheme.spacing.l + TamyrSizes.gainChipTop, end = TamyrTheme.spacing.l),
            )
        }
    }
}

@Composable
private fun CurrentLessonCardBody(
    current: CurrentLesson,
    cardGain: CardGain?,
    animation: GainAnimation,
    onStart: () -> Unit,
) {
    val colors = TamyrTheme.colors
    // До конца «доезда» карточка показывает старые значения прироста, затем настоящие.
    val barCompleted = if (cardGain != null && !animation.barsReached) cardGain.fromCompleted else current.completedLessons
    val counterCompleted = if (cardGain != null && !animation.counterReached) cardGain.fromCompleted else current.completedLessons
    val allDone = current.kind == HomeCardKind.AllDone
    val tag = when (current.kind) {
        HomeCardKind.Start -> Res.string.home_start_tag
        HomeCardKind.Continue -> Res.string.home_continue_tag
        HomeCardKind.NewModule -> Res.string.home_new_module_tag
        HomeCardKind.AllDone -> Res.string.home_done_tag
    }
    TamyrCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (cardGain != null) Modifier.gainRing(animation, TamyrTheme.shapes.card) else Modifier),
        containerColor = colors.successContainer,
        contentPadding = TamyrTheme.spacing.l,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(tag).toUpperCase(Locale.current),
                    style = TamyrTheme.typography.tag.copy(fontSize = TamyrTheme.typography.caption13Bold.fontSize),
                    color = colors.primary,
                )
                val counter = when (current.kind) {
                    HomeCardKind.Continue -> stringResource(Res.string.home_lesson_progress, counterCompleted, current.totalLessons)
                    HomeCardKind.Start, HomeCardKind.NewModule -> pluralStringResource(Res.plurals.home_lessons_count, current.totalLessons, current.totalLessons)
                    HomeCardKind.AllDone -> null
                }
                if (counter != null) Text(text = counter, style = TamyrTheme.typography.caption13, color = colors.primary)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
                    Text(
                        text = if (allDone) {
                            stringResource(Res.string.home_all_done_title)
                        } else if (current.topicTt == current.topicRu) {
                            current.topicTt
                        } else {
                            "${current.topicTt} · ${current.topicRu}"
                        },
                        style = TamyrTheme.typography.title19,
                        color = colors.textPrimary,
                    )
                    if (current.restartsLesson) {
                        Text(stringResource(Res.string.home_lesson_restart_hint), style = TamyrTheme.typography.caption13, color = colors.textSecondary)
                    }
                }
                // Новичку и тому, кто прошёл всё, Мыраужан; новому модулю три ромба; обычному «Продолжить» ничего (без «обоев»).
                when (current.kind) {
                    HomeCardKind.Start -> ProfileAvatar(Avatar.Pose(AvatarPose.Greeting), "", CardMascotSize)
                    HomeCardKind.AllDone -> ProfileAvatar(Avatar.Pose(AvatarPose.Cheer), "", CardMascotSize)
                    HomeCardKind.NewModule -> BrandDiamonds()
                    HomeCardKind.Continue -> Unit
                }
            }
            // Полосу показываем только при продолжении: пустая полоса у новичка выглядит упрёком.
            if (current.kind == HomeCardKind.Continue) {
                TamyrProgressBar(
                    progress = if (current.totalLessons == 0) 0f else barCompleted.toFloat() / current.totalLessons,
                    height = TamyrSizes.lessonProgress,
                    trackColor = colors.surface,
                    animationMillis = animation.barMillis,
                )
            }
            LessonButton(
                text = if (allDone) {
                    stringResource(Res.string.home_repeat_lesson_button, current.lesson.number)
                } else {
                    stringResource(Res.string.home_lesson_button, current.lesson.number, current.lesson.titleTt)
                },
                onClick = onStart,
            )
        }
    }
}

private val CardMascotSize = 56.dp

/** Кнопка в карточке урока: укороченный вариант главной кнопки (46dp, скругление 12dp, без «3D»-тени). */
@Composable
private fun LessonButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TamyrSizes.lessonButton)
            .clip(TamyrTheme.shapes.chip)
            .background(TamyrTheme.colors.primary)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TamyrTheme.typography.body15Bold,
            color = TamyrTheme.colors.onPrimary,
        )
    }
}

@Composable
private fun ModulesGrid(
    modules: List<LearningModule>,
    highlightedId: String?,
    animation: GainAnimation,
    onClick: (moduleId: String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m)) {
        modules.chunked(2).forEach { rowModules ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
            ) {
                rowModules.forEach { module ->
                    ModuleTile(
                        module = module,
                        highlighted = module.id == highlightedId,
                        animation = animation,
                        onClick = { onClick(module.id) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
                if (rowModules.size == 1) Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ModuleTile(
    module: LearningModule,
    highlighted: Boolean,
    animation: GainAnimation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gain = animation.gain?.takeIf { it.moduleId == module.id }
    val shownPercent = if (gain != null && !animation.barsReached) gain.fromPercent else module.progressPercent
    TamyrCard(
        modifier = if (gain != null) modifier.gainRing(animation, TamyrTheme.shapes.tile) else modifier,
        onClick = onClick,
        shape = TamyrTheme.shapes.tile,
        containerColor = if (module.isLocked) TamyrTheme.colors.surfaceMuted else TamyrTheme.colors.surface,
        // Новичку первый модуль подсвечен рамкой: «начни отсюда».
        borderColor = if (highlighted) TamyrTheme.colors.primary else TamyrTheme.colors.border,
        borderWidth = if (highlighted) TamyrSizes.borderAccent else TamyrSizes.borderThin,
        contentPadding = ModuleCardPadding,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
        ) {
            ModuleChip(module = module)
            // В макете на закрытых плитках показано только татарское название.
            val title = if (module.isLocked) module.titleTt else moduleTitle(module.titleTt, module.titleRu)
            Text(
                text = title,
                style = TamyrTheme.typography.body15Bold,
                color = if (module.isLocked) TamyrTheme.colors.textSecondary else TamyrTheme.colors.textPrimary,
            )
            when {
                module.isLocked -> Text(
                    text = stringResource(
                        Res.string.module_locked_hint,
                        moduleTitle(module.lockedByTitleTt.orEmpty(), module.lockedByTitleRu.orEmpty()),
                    ),
                    style = TamyrTheme.typography.label12,
                    color = TamyrTheme.colors.textSecondary,
                )
                module.progressPercent >= 100 -> Text(
                    text = stringResource(Res.string.home_module_done, module.progressPercent),
                    style = TamyrTheme.typography.label12Bold,
                    color = TamyrTheme.colors.primary,
                )
                else -> TamyrProgressBar(
                    progress = shownPercent / 100f,
                    height = TamyrSizes.moduleProgress,
                    color = if (module.icon == ICON_FOOD) TamyrTheme.colors.accent else TamyrTheme.colors.primary,
                    animationMillis = animation.barMillis,
                )
            }
        }
    }
}

@Composable
private fun ModuleChip(module: LearningModule) {
    val background: Color = when {
        module.isLocked -> TamyrTheme.colors.background
        module.icon == ICON_FOOD -> TamyrTheme.colors.errorContainer
        else -> TamyrTheme.colors.successContainer
    }
    Box(
        modifier = Modifier
            .size(TamyrSizes.moduleChip)
            .clip(TamyrTheme.shapes.chip)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        when {
            module.isLocked -> LockIcon(TamyrTheme.colors.textSecondary)
            module.icon == ICON_FOOD -> BowlIcon(TamyrTheme.colors.accent)
            else -> Text(
                text = when (module.icon) {
                    ICON_ALPHABET -> stringResource(Res.string.home_chip_alphabet)
                    ICON_NUMBERS -> stringResource(Res.string.home_chip_numbers)
                    else -> module.titleTt.take(1)
                },
                style = TamyrTheme.typography.body15Bold,
                color = TamyrTheme.colors.primary,
            )
        }
    }
}

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
    LearningModule("alphabet", "Алфавит", "Алфавит", "alphabet", progressPercent = 100, lockedBy = null, lockedByTitleTt = null, lockedByTitleRu = null),
    LearningModule("food", "Ашлар", "Еда", "food", progressPercent = 35, lockedBy = null, lockedByTitleTt = null, lockedByTitleRu = null),
    LearningModule("numbers", "Саннар", "Числа", "numbers", progressPercent = 10, lockedBy = null, lockedByTitleTt = null, lockedByTitleRu = null),
    LearningModule("nature", "Табигать", "Природа", "nature", progressPercent = 0, lockedBy = "food", lockedByTitleTt = "Ашлар", lockedByTitleRu = "Еда"),
)

private fun previewContent(
    modules: List<LearningModule> = previewModules,
    completed: Int = 3,
    total: Int = 5,
    kind: HomeCardKind = HomeCardKind.Continue,
    restartsLesson: Boolean = false,
) = HomeContent(
    modules,
    CurrentLesson(Lesson("lesson_4", 4, "Туганнар", "Семья"), "Гаилә", "Семья", completed, total, kind, restartsLesson),
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

@Preview(name = "Content", group = PREVIEW_GROUP)
@Composable
private fun HomeContentPreview() = HomePreview(
    HomeUiState("Алия", UserStats(xp = 340, streak = 12), LoadState.Content(previewContent())),
)

@Preview(name = "Progress gain (before animation)", group = PREVIEW_GROUP)
@Composable
private fun HomeProgressGainPreview() = HomePreview(
    HomeUiState(
        userName = "Алия",
        stats = UserStats(xp = 340, streak = 12),
        content = LoadState.Content(previewContent(completed = 3)),
        progressGain = ProgressGain(moduleId = "food", fromPercent = 40, toPercent = 60, card = CardGain(fromCompleted = 2, toCompleted = 3, total = 5)),
    ),
)

@Preview(name = "Stub toast", group = PREVIEW_GROUP)
@Composable
private fun HomeStubToastPreview() = HomePreview(
    HomeUiState(
        userName = "Алия",
        stats = UserStats(xp = 340, streak = 12),
        content = LoadState.Content(previewContent()),
        toast = HomeToast.AllModulesUnavailable,
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

@Preview(name = "New user (no stats, lesson not started)", group = PREVIEW_GROUP)
@Composable
private fun HomeNewUserPreview() = HomePreview(
    HomeUiState(
        "Алия",
        UserStats.Empty,
        LoadState.Content(previewContent(previewModules.map { it.copy(progressPercent = 0) }.take(2), completed = 0)),
    ),
)

@Preview(name = "All modules done, lesson finished", group = PREVIEW_GROUP)
@Composable
private fun HomeAllDonePreview() = HomePreview(
    HomeUiState(
        "Алия",
        UserStats(xp = 1990, streak = 100),
        LoadState.Content(previewContent(previewModules.map { it.copy(progressPercent = 100, lockedBy = null, lockedByTitleTt = null, lockedByTitleRu = null) }, completed = 5)),
    ),
)

@Preview(name = "First launch (start card)", group = PREVIEW_GROUP)
@Composable
private fun HomeStartPreview() = HomePreview(
    HomeUiState("Алия", UserStats.Empty, LoadState.Content(previewContent(previewModules.map { it.copy(progressPercent = 0) }, completed = 0, total = 3, kind = HomeCardKind.Start))),
)

@Preview(name = "New module card", group = PREVIEW_GROUP)
@Composable
private fun HomeNewModulePreview() = HomePreview(
    HomeUiState("Алия", UserStats(xp = 200, streak = 3), LoadState.Content(previewContent(completed = 0, total = 2, kind = HomeCardKind.NewModule))),
)

@Preview(name = "All done card", group = PREVIEW_GROUP)
@Composable
private fun HomeAllDoneCardPreview() = HomePreview(
    HomeUiState(
        "Алия",
        UserStats(xp = 1990, streak = 100),
        LoadState.Content(previewContent(previewModules.map { it.copy(progressPercent = 100, lockedBy = null, lockedByTitleTt = null, lockedByTitleRu = null) }, completed = 5, kind = HomeCardKind.AllDone)),
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
                listOf(
                    LearningModule("a", "Бик озын исемле модуль", "Очень длинное название модуля", "alphabet", 50, null, null, null),
                    LearningModule("b", "Табигать һәм һава торышы", "Природа и погода", "nature", 0, "a", "Бик озын исемле модуль", "Очень длинное название модуля"),
                ),
            ),
        ),
    ),
)
