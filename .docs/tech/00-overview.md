# 00. Общее: навигация, архитектура, правила

## 1. Что за приложение
Мобильное приложение для изучения татарского языка (аудитория — русскоязычные). Kotlin Multiplatform + Compose Multiplatform, Android и iOS, весь UI и логика в `commonMain`. Маскот — «Мыраужан» (иллюстрации пока не нарисованы).

## 2. Карта экранов и переходов

```
Старт (StartDestinationResolver)
 ├─ есть сессия ─────────────► Home
 ├─ онбординг уже виден ─────► Auth (вход)
 └─ иначе ───────────────────► Onboarding
Onboarding ──«Начать»────────► Auth (режим «Регистрация»)
Onboarding ──«Уже есть акк.»─► Auth (режим «Вход»)
Auth ──успех─────────────────► Home (стек очищается)
Auth ──←─────────────────────► назад (pop)
Home ──«Урок N»──────────────► Lesson(lessonId)
Lesson ──последний шаг──► Result (Lesson заменяется на Result)
Lesson ──✕/Назад + «Выйти»───► назад (pop)
Result ──«Продолжить»────────► Home (стек очищается)
Result ──«Повторить ошибки»──► Lesson(lessonId, stepIds = ошибочные шаги)
```
Реализация: `…/navigation/AppNavigation.kt`, ключи — `Routes.kt` (Navigation 3, типизированные `NavKey`). Результат урока заменяет сам урок в стеке, поэтому «Назад» из Result не вернёт в урок.

**Стартовый экран выбирается один раз** при запуске (`StartDestinationResolver`): сессия → Home; флаг `onboarding_seen` → Auth; иначе Onboarding.

## 3. Архитектура экранов
- MVI/UDF: `UiState` (неизменяемый) / `UiEvent` / `UiEffect` + базовый `MviViewModel` (`…/core/common/MviViewModel.kt`).
- Каждый экран = stateful `XRoute` (подписка на state и effects, DI через Koin) + stateless `XScreen` (чистая отрисовка).
- `LoadState<T>` = `Loading | Content | Error(code)`; общие компоненты `LoadingState` / `ErrorState` / `LoadStateContent` (экраны 07/08, см. отдельный документ).
- Раскладка файлов фичи: `XContract.kt`, `XViewModel.kt`, `XScreen.kt`.

## 4. Данные
| Слой | Что | Где |
|---|---|---|
| Контент (fake) | `home.json`, `lessons/lesson_4.json` из `composeResources/files`, задержка 300–800 мс | `…/data/fake/FakeRepositories.kt` |
| Сеть (опц.) | Ktorfit `LessonsApi`/`AuthApi`, включается `tamyr.useRemote=true` в `gradle.properties` | `…/data/remote/` |
| Прогресс | SQLDelight: `LessonProgress`, `UserStats` (XP, серия), `CompletedExercise` | `…/data/local/SqlDelightProgressRepository.kt`, `Progress.sq` |
| Сессия | имя+email в multiplatform-settings | `…/data/local/SessionStorage.kt` |
| Флаг онбординга | `onboarding_seen` | `…/data/local/OnboardingStorage.kt` |

Ошибки: `AppException(code)` внутри `Result<T>`; коды `NET_TIMEOUT`, `NET_ERROR`, `NOT_FOUND`, `UNKNOWN`, `NO_EXERCISES`. UI показывает код пользователю.
`FakeBackendConfig.setSimulateErrors(true)` заставляет fake-репозитории падать с `NET_TIMEOUT` (для проверки экранов 07/08; переключатель — в debug UI kit).

## 5. Игровые правила (сквозные)
Файл: `…/domain/exercise/XpRules.kt`, `…/domain/model/User.kt`.
- **Жизней нет.** Ошибки не прерывают урок. Урок помечается завершённым, если точность >= `XpRules.PASS_THRESHOLD_PERCENT` (80).
- **XP:** 3 за каждый верно решённый шаг (`PER_CORRECT_STEP`), сохраняется сразу после проверки шага.
- **Уровень:** `xp / 100 + 1`; до следующего уровня `100 − xp % 100` (формула временная — открытый вопрос).
- **Серия дней (streak):** тот же день — не меняется; следующий день — +1; пропуск — сброс на 1. Дни считаются по **UTC** (`StreakCalculator`, `TimeProvider`).
- **Точность:** верных шагов / проверенных шагов.
- **Статус урока:** `NotStarted → InProgress` (после первого шага) `→ Completed` (если точность >= 80 %), хранится лучший процент.

## 6. Тексты и дизайн-правила
- Все строки интерфейса — в `composeResources/values/strings.xml` (без хардкода). Татарские слова упражнений — данные, а не строки UI.
- Цвета/размеры — только токены `TamyrTheme.colors/typography/shapes/spacing`, `TamyrSizes`, `TamyrMotion`.
- Весь татарский контент не проверен носителем (`needsNativeReview` в JSON).
- Пробелы дизайна помечены `// TODO(design)`.

## 7. Платформенные куски (`expect/actual`)
- `createPlatformAudioPlayer()` — Android: `MediaPlayer`; iOS: `AVPlayer`.
- `platformModule` в Koin — SqlDriver, Settings, HttpClientEngine.

## 8. Тесты
`./gradlew :shared:testAndroidHostTest :shared:iosSimulatorArm64Test` — логика ответов (`AnswerNormalizer`), серия и планировщик, `LessonViewModel`, ViewModel остальных экранов, fake-репозитории.
Проверка iOS без тапов: аргументы запуска `-onboarding_seen YES`, `-session_name X -session_email Y`.
