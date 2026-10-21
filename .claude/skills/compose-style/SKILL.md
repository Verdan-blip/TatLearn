---
name: compose-style
description: Код-стайл Compose в проекте Тамыр (TatLearn). Применять при написании или изменении любого Compose-кода (composable-функций, экранов, компонентов, превью) в shared/src/commonMain.
---

# Compose: код-стайл проекта Тамыр

Правила обязательны для всего нового и изменяемого Compose-кода. Пополняются по ходу разработки: когда пользователь просит «зафиксировать в скилл», добавляй правило в конец раздела «Правила» с примером.

## Правила

### 1. Именованные параметры, каждый в отдельной строке
При вызове composable-функций все аргументы именованные, каждый на своей строке (в том числе `modifier`). Исключение — единственный очевидный `content`-лямбда-параметр в трейлинг-форме.

```kotlin
// Плохо
TamyrButton(stringResource(Res.string.onboarding_start), onClick, enabled = true)

// Хорошо
TamyrButton(
    text = stringResource(Res.string.onboarding_start),
    onClick = onClick,
    enabled = true,
)
```

### 2. Цепочки вызовов
Если в цепочке через точку больше одного вызова, каждый вызов начинается с новой строки. Касается и `Modifier`.

```kotlin
// Плохо
Modifier.fillMaxWidth().padding(16.dp)

// Хорошо
Modifier
    .fillMaxWidth()
    .padding(TamyrTheme.spacing.screen)
```
Один вызов остаётся в строку: `Modifier.fillMaxSize()`.

### 3. Превью у каждой composable
У каждой composable-функции обязательно есть `@Preview` (для экранов — на разные состояния, см. правило 5). Превью рисуют `*Content`, а не `*Scene`. Превью оборачиваются в `PreviewFrame`. В `@Preview` не указываем размеры устройства (`widthDp`, `heightDp`, `device`); допустим только `name` (и `group`), если нужно различать состояния. Мелкие private-вспомогательные composable можно покрыть превью их родителя, если они не выделены в отдельную сущность.

### 4. Одна сущность — один файл
Каждый класс, composable-компонент, enum, интерфейс лежит в своём файле, названном по сущности. Исключение — маленькие one-shot private-сущности, используемые только в этом файле (private-хелпер, private-enum на пару значений, private-константа).

### 5. Структура экрана
Экран `X` состоит из:
- `XScene` — stateful-слой: получает `ViewModel`, подписывается на `uiState`, собирает эффекты (`UiEffect`: навигация, диалоги и т. п.), передаёт события в `ViewModel`. Сам ничего не рисует, кроме вызова `XContent`.
- `XContent` — stateless-слой: получает на вход `uiState: XUiState` и `onEvent: (XUiEvent) -> Unit` (или `uiEvent`-колбэки) и только рисует.
- Превью рисуют именно `XContent` (с набором состояний: загрузка, ошибка, пусто, разные варианты контента и т. п.).
- Файлы: `XContract.kt` (UiState/UiEvent/UiEffect), `XViewModel.kt`, `XScene.kt`, `XContent.kt`, превью — в `XContent.kt` или рядом, по правилу 3.

> Переходный момент: в существующем коде пока используются имена `XRoute`/`XScreen`. При правке экрана переименовывай их в `XScene`/`XContent` и приводи структуру к этому правилу.

### 6. Всё состояние — в `uiState`
Любое состояние экрана хранится в `UiState` и меняется через `UiEvent` во `ViewModel`, в том числе позиция пейджера, выбранная вкладка, видимость диалога, введённый текст. Не использовать `remember`/`rememberPagerState`-состояние как источник правды экрана. (`remember` допустим только для чисто визуальных вещей без смысла для логики, например состояний анимации; если сомневаешься — клади в `uiState`.)

### 7. Тема — напрямую
К токенам темы обращаться напрямую: `TamyrTheme.colors.primary`, `TamyrTheme.typography.body16`, `TamyrTheme.spacing.screen`, `TamyrTheme.shapes.card`. Не создавать локальные переменные вроде `val colors = TamyrTheme.colors`.

```kotlin
// Плохо
val colors = TamyrTheme.colors
Text(
    text = title,
    color = colors.textPrimary,
)

// Хорошо
Text(
    text = title,
    color = TamyrTheme.colors.textPrimary,
)
```

### 8. Комментарии — только когда действительно необходимы
Код должен быть понятен без комментариев: имена и структура объясняют «что». Пиши комментарий только если без него неочевидно «почему» (обходной путь, неочевидное ограничение платформы, нетривиальная причина решения). Не добавлять KDoc к очевидным функциям, классам и полям, не пересказывать код, не ставить разделители `// region`. Метки `TODO(design)`/`TODO(audio)`/`TODO(backend)`/`TODO(content)` и обязательный комментарий у `expect/actual` (см. CLAUDE.md) остаются. Язык комментариев — русский.

## Общие напоминания (из CLAUDE.md)
- Комментарии (редкие, см. правило 8) на русском, идентификаторы и `TODO(...)`-метки на английском.
- Строки — только из `composeResources/values/strings.xml`; цвета и размеры — только из токенов темы.
- После изменений проверять: `./gradlew :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64`.
