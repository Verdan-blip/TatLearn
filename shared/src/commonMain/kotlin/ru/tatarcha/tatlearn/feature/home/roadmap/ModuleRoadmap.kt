package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.CurrentLesson
import ru.tatarcha.tatlearn.domain.model.HomeCardKind
import ru.tatarcha.tatlearn.domain.model.LearningModule
import ru.tatarcha.tatlearn.domain.model.Lesson
import ru.tatarcha.tatlearn.domain.model.ModuleState
import ru.tatarcha.tatlearn.domain.model.RoadmapEntry
import ru.tatarcha.tatlearn.domain.model.RoadmapRow
import ru.tatarcha.tatlearn.domain.model.UserStats

private const val HEADER_KEY = "header"
private const val CURRENT_KEY = "current"
private const val END_KEY = "end"
private const val ITEMS_PER_ROW = 2
private val BottomReserve = 96.dp
private val TileLinkTop = 66.dp

/**
 * Лента модулей (макет 03, 45–49): шапка, ряды по фиксированной сетке, тропа между рядами, в конце «Финиш A1» или карточка
 * «Курс пройден». Каждый ряд занимает два элемента списка (тропа к ряду и сам ряд), поэтому индексы считаются без обхода.
 */
@Composable
fun ModuleRoadmap(
    rows: List<RoadmapRow>,
    current: CurrentLesson,
    totalModules: Int,
    header: @Composable () -> Unit,
    onModuleClick: (moduleId: String) -> Unit,
    onStartLesson: () -> Unit,
    state: LazyListState,
    animation: RoadmapAnimation,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val courseDone = current.kind == HomeCardKind.AllDone
    // Пульсация «волна к цели» только в покое: не при прокрутке, не во время анимации урока и не при «уменьшить анимацию».
    val pulsing = !reduceMotion && !animation.running && !state.isScrollInProgress
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(
            start = TamyrTheme.spacing.screen,
            end = TamyrTheme.spacing.screen,
            top = TamyrTheme.spacing.l,
            bottom = BottomReserve,
        ),
    ) {
        item(key = HEADER_KEY) { header() }
        rows.forEachIndexed { index, row ->
            // Тропа идёт от модуля к модулю; к первому модулю сверху её нет (она никуда не ведёт).
            if (index > 0) item(key = "connector_$index") {
                val toCurrent = row is RoadmapRow.Current
                RoadmapConnector(
                    from = rows[index - 1].exitSide(),
                    to = row.entrySide(),
                    passed = rows[index - 1].lastState() == ModuleState.Done,
                    reveal = if (toCurrent) ({ animation.trail.value }) else ({ 1f }),
                    pulsing = toCurrent && pulsing,
                )
            }
            item(key = if (row is RoadmapRow.Current) CURRENT_KEY else "row_$index") {
                RoadmapRowContent(
                    row = row,
                    current = current,
                    totalModules = totalModules,
                    onModuleClick = onModuleClick,
                    onStartLesson = onStartLesson,
                    animation = animation,
                    reduceMotion = reduceMotion,
                )
            }
        }
        item(key = "connector_end") {
            RoadmapConnector(
                from = rows.lastOrNull()?.exitSide() ?: TrailSide.Center,
                to = TrailSide.Center,
                passed = rows.lastOrNull()?.lastState() == ModuleState.Done,
            )
        }
        item(key = END_KEY) {
            if (courseDone) RoadmapDoneCard(onRepeat = onStartLesson) else RoadmapFinish()
        }
    }
}

@Composable
private fun RoadmapRowContent(
    row: RoadmapRow,
    current: CurrentLesson,
    totalModules: Int,
    onModuleClick: (moduleId: String) -> Unit,
    onStartLesson: () -> Unit,
    animation: RoadmapAnimation,
    reduceMotion: Boolean,
) {
    when (row) {
        is RoadmapRow.Current -> RoadmapCurrentCard(
            entry = row.entry,
            current = current,
            totalModules = totalModules,
            onStart = onStartLesson,
            animation = animation,
        )
        is RoadmapRow.Tiles -> Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                TileSlot(
                    entry = row.left,
                    totalModules = totalModules,
                    onModuleClick = onModuleClick,
                    animation = animation,
                    reduceMotion = reduceMotion,
                    modifier = Modifier.weight(1f),
                )
                TileSlot(
                    entry = row.right,
                    totalModules = totalModules,
                    onModuleClick = onModuleClick,
                    animation = animation,
                    reduceMotion = reduceMotion,
                    modifier = Modifier.weight(1f),
                )
            }
            if (row.left != null && row.right != null) {
                RoadmapTileLink(
                    passed = row.left.state == ModuleState.Done,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = TileLinkTop),
                )
            }
        }
    }
}

@Composable
private fun TileSlot(
    entry: RoadmapEntry?,
    totalModules: Int,
    onModuleClick: (moduleId: String) -> Unit,
    animation: RoadmapAnimation,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        if (entry != null) {
            val stamping = animation.isModuleDone && animation.gain?.moduleId == entry.module.id
            RoadmapTile(
                entry = entry,
                totalModules = totalModules,
                onClick = { onModuleClick(entry.module.id) },
                sealProgress = if (stamping) ({ animation.stamp.value }) else ({ 1f }),
                reduceMotion = reduceMotion,
            )
        }
    }
}

private fun RoadmapRow.exitSide(): TrailSide = when (this) {
    is RoadmapRow.Tiles -> if (right != null) TrailSide.Right else TrailSide.Left
    is RoadmapRow.Current -> TrailSide.Left
}

private fun RoadmapRow.entrySide(): TrailSide = when (this) {
    is RoadmapRow.Tiles -> if (left != null) TrailSide.Left else TrailSide.Right
    is RoadmapRow.Current -> TrailSide.Right
}

private fun RoadmapRow.lastState(): ModuleState = when (this) {
    is RoadmapRow.Tiles -> (right ?: left)?.state ?: ModuleState.Locked
    is RoadmapRow.Current -> entry.state
}

/** Индекс элемента списка, где стоит карточка текущего модуля (или конец ленты, если курс пройден): шапка, ряд 0, затем пары «тропа, ряд». */
internal fun roadmapCurrentItemIndex(rows: List<RoadmapRow>): Int {
    val cardRow = rows.indexOfFirst { it is RoadmapRow.Current }
    return if (cardRow >= 0) ITEMS_PER_ROW * cardRow + 1 else ITEMS_PER_ROW * rows.size + 1
}

/** Элемент списка, с которого лента открывается: над карточкой текущего модуля остаётся ряд пройденного (или шапка). */
internal fun roadmapScrollTarget(rows: List<RoadmapRow>): Int {
    val cardIndex = roadmapCurrentItemIndex(rows)
    return if (cardIndex >= PREVIOUS_ROW_OFFSET + 1) cardIndex - PREVIOUS_ROW_OFFSET else 0
}

/** Состояние прокрутки: лента открывается сразу на текущем модуле, без анимации. */
@Composable
fun rememberRoadmapListState(rows: List<RoadmapRow>): LazyListState =
    rememberLazyListState(initialFirstVisibleItemIndex = roadmapScrollTarget(rows))

// Элементы перед карточкой: тропа к ней и предыдущий ряд.
private const val PREVIOUS_ROW_OFFSET = 3

/**
 * Где карточка текущего модуля относительно экрана; от этого зависит плавающая панель и направление её стрелки.
 * Гистерезис [HYSTERESIS]: панель прячется, когда кнопка урока в карточке видна с запасом, а появляется, только когда кнопка вышла за край,
 * поэтому на границе не мигает.
 */
@Composable
fun rememberCurrentCardVisibility(state: LazyListState, cardIndex: Int): State<CurrentCardVisibility> {
    val margin = with(LocalDensity.current) { HYSTERESIS.toPx() }
    val buttonZone = with(LocalDensity.current) { BUTTON_ZONE.toPx() }
    return produceState(CurrentCardVisibility.Visible, state, cardIndex, margin, buttonZone) {
        snapshotFlow {
            val info = state.layoutInfo
            val card = info.visibleItemsInfo.firstOrNull { it.key == CURRENT_KEY }
            if (card == null) {
                if (state.firstVisibleItemIndex > cardIndex) CardPlace.Above else CardPlace.Below
            } else {
                // Кнопка урока стоит внизу карточки: пока она на экране, плавающая панель её не дублирует.
                val bottom = (card.offset + card.size).toFloat()
                val buttonTop = bottom - buttonZone
                val end = info.viewportEndOffset.toFloat()
                when {
                    buttonTop >= margin && bottom <= end - margin -> CardPlace.Inside
                    buttonTop >= 0f && bottom <= end -> CardPlace.Edge
                    buttonTop < 0f -> CardPlace.Above
                    else -> CardPlace.Below
                }
            }
        }.collect { place ->
            when (place) {
                CardPlace.Inside -> value = CurrentCardVisibility.Visible
                CardPlace.Edge -> Unit
                CardPlace.Above -> value = CurrentCardVisibility.Above
                CardPlace.Below -> value = CurrentCardVisibility.Below
            }
        }
    }
}

private enum class CardPlace { Inside, Edge, Above, Below }

private val HYSTERESIS = 24.dp

// Высота кнопки урока с отступами внизу карточки.
private val BUTTON_ZONE = 80.dp

private fun previewRow(number: Int, state: ModuleState, hint: Boolean = false) = RoadmapEntry(
    module = LearningModule(
        id = "m$number",
        titleTt = "Модуль",
        titleRu = "Модуль $number",
        icon = listOf("meeting", "alphabet", "family", "numbers", "food", "city", "verbs", "shop", "nature")[number - 1],
        progressPercent = if (state == ModuleState.Done) 100 else 0,
        lockedBy = if (state == ModuleState.Locked) "prev" else null,
        lockedByTitleTt = "Модуль",
        lockedByTitleRu = "Модуль ${number - 1}",
        completedLessons = when (state) {
            ModuleState.Done -> 4
            ModuleState.Current -> 3
            else -> 0
        },
        totalLessons = if (state == ModuleState.Done || state == ModuleState.Current) 5 else 4,
    ),
    number = number,
    state = state,
    showsUnlockHint = hint,
)

private val previewRows = listOf(
    RoadmapRow.Tiles(previewRow(1, ModuleState.Done), previewRow(2, ModuleState.Done)),
    RoadmapRow.Current(previewRow(3, ModuleState.Current)),
    RoadmapRow.Tiles(left = null, right = previewRow(4, ModuleState.Locked, hint = true)),
    RoadmapRow.Tiles(previewRow(5, ModuleState.Locked), previewRow(6, ModuleState.Locked)),
    RoadmapRow.Tiles(previewRow(7, ModuleState.Locked), previewRow(8, ModuleState.Locked)),
    RoadmapRow.Tiles(previewRow(9, ModuleState.Locked), right = null),
)

private val previewCurrent = CurrentLesson(
    lesson = Lesson("lesson_4", 4, "Туганнар", "Родственники"),
    topicTt = "Гаилә",
    topicRu = "Семья",
    completedLessons = 3,
    totalLessons = 5,
    kind = HomeCardKind.Continue,
    moduleId = "m3",
)

@Preview(name = "Roadmap, current 3")
@Composable
private fun ModuleRoadmapPreview() = PreviewFrame {
    ModuleRoadmap(
        rows = previewRows,
        current = previewCurrent,
        totalModules = 9,
        header = { RoadmapHeaderPlaceholder() },
        onModuleClick = {},
        onStartLesson = {},
        state = rememberLazyListState(),
        animation = RoadmapAnimation(gain = null, reduceMotion = false),
        reduceMotion = false,
    )
}

@Composable
private fun RoadmapHeaderPlaceholder() {
    RoadmapHeader(
        userName = "Алия",
        stats = UserStats(xp = 340, streak = 12),
    )
}
