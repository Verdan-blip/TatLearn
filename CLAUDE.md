# Тамыр (TatLearn) — conventions

App for learning Tatar (Russian-speaking audience). Kotlin Multiplatform + Compose Multiplatform, Android + iOS, all UI/logic in `commonMain`.
Source of truth for UI: `design/Tatar App Screens.dc.html` (37 screens, 390×844). Skills: `design` (what "design" means here), `design-extend` (how to draw new screens/elements), `compose-style`.

## Modules / build
- `:shared` — all code (KMP library, iOS framework `Shared`); `:androidApp` — Android entry point; `iosApp/` — Xcode project (entry point only).
- Package root: `ru.tatarcha.tatlearn`. Versions only in `gradle/libs.versions.toml`.
- JDK: use Android Studio JBR: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- Check: `./gradlew :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64`.

## Stack
Koin (DI), Navigation 3 (JetBrains, `navigation3-ui`; runtime comes transitively), Ktor + Ktorfit (KSP), Coil 3, SQLDelight, multiplatform-settings, kotlinx.serialization.

## Package layout (`shared/src/commonMain/kotlin/ru/tatarcha/tatlearn`)
`core/{designsystem,network,database,common,notifications}`, `feature/{onboarding,auth,intro,profile,home,module,exercises,result}`, `data`, `domain`, `di`, `navigation`.

## UI rules
- Compose code style (named params, Scene/Content, previews, theme access): skill `.claude/skills/compose-style`; it is extended on request.
- Each feature has its own `feature/<name>/CLAUDE.md` (flow, files, decisions and reasons, open questions): read it before working on the feature and update it in the same change when the behavior changes. Analyst-facing spec stays in `.docs/`.
- MVI/UDF: `UiState` (immutable) / `UiEvent` / `UiEffect` + `MviViewModel`. Each screen = stateful `Route` + stateless `Screen`.
- Loading/Error are shared components over a `Loading/Content/Error(retry)` state, not routes. Status-bar inset is applied per screen (`statusBarsPadding`/`safeDrawingPadding` inside the screen's own background, also in `LoadingState`/`ErrorState`), not at the root: a root inset would leave the status bar area outside bottom sheet scrims. Text buttons (`TamyrTextButton`) have a 48 dp minimum tap target. Loading skeletons follow the shape of the destination screen (`HomeSkeleton`, `ExerciseSkeleton` for lessons, module skeleton).
- No hardcoded strings (use `composeResources/values/strings.xml`) and no hardcoded colors/sizes (theme tokens only). Tatar exercise words are data, not UI strings.
- Mark unverified Tatar content "needs native-speaker review"; mark missing design states `// TODO(design)`.
- Compare each finished screen with its PNG in `design/screens/`.
- Comments (KDoc, `//`, gradle, XML) are always written in Russian; identifiers, commit-style tags (`TODO(design)`, `TODO(audio)`, `TODO(backend)`, `TODO(content)`) and code stay in English. Talk to the user in Russian.
- Every `expect/actual` gets a comment explaining the iOS side and how to verify it.

## Design system (`core/designsystem`)
- Access tokens via `TamyrTheme.colors/typography/shapes/spacing`; sizes of fixed components in `TamyrSizes`. Wrap UI in `TamyrTheme`.
- Components: `TamyrButton` (`loading` -> `TamyrSpinner`; design screen 13), `TamyrCard`, `TamyrProgressBar`, `TamyrTag`, `Diamond/BrandDiamonds/PulsingDiamonds`, `TamyrConfirmSheet` (bottom sheet, design screen 15), `TamyrToast/TamyrToastHost` (Info/Error/Success, 5 s, close button, swipe up; design screen 12), `LoadingState`, `ErrorState`, `LoadStateContent` over `core/common/LoadState`.
- Font: PT Sans in `composeResources/font` (OFL license: `design/OFL-PTSans.txt`). Design PNGs: `design/screens/NN.png`, re-render with `python3 design/render_screens.py [NN ...]` (system Chrome, no Playwright).
- Debug UI kit: `ShowcaseScreen`, reached from the "Профиль" tab placeholder via the "Инструменты отладки" button (debug builds only): data actions with confirmation (reset lesson progress, reset XP/streak, sign out, reset onboarding), reminder checks (test notification, reset the reminder offer), UI kit, simulated network errors.
- Mascot Myrauzhan: `TamyrMascot(MascotPose, size)` over vector drawables `mascot_*.xml` (sources: `design/mascot/*.svg`, brief: `design/mascot-prompt.md`). Used in onboarding, error state, answer panel, result, matching hint. Not wired yet: Loading (Searching), Tired (wired: below-threshold result), Sleeping, HeadIcon; the cap/diamond ornament needs native/cultural review.

## Data layer
- `domain/{model,repository}`: interfaces return `Result<T>` with `AppException(code)`; UI maps to `LoadState.Error(code)`.
- `data/fake`: `Fake*Repository` read API-shaped JSON from `composeResources/files` (`modules.json` = catalog of modules with their lessons, `lessons/<id>.json` = lesson tasks; DTOs are shared with the Ktorfit API, mappers in `data/mapper`), 300-800 ms delay, `FakeBackendConfig.setSimulateErrors` for screens 07/08.
- `data/remote`: Ktorfit `LessonsApi`/`AuthApi` + `Remote*Repository`; enabled by `tamyr.useRemote=true` in gradle.properties (`tamyr.baseUrl` too) -> generated `BuildConfig`.
- `data/local`: `SqlDelightProgressRepository` (Progress.sq), `SessionStorage` and `SettingsProfileRepository` (guest profile: name, avatar, local UUID; multiplatform-settings).
- Koin starts in `TatLearnApplication` (Android) / `MainViewController` (iOS) via `initKoin`; `platformModule` is expect/actual (SqlDriver, Settings, HttpClientEngine).
- iOS static framework needs `-lsqlite3` (Config.xcconfig + linkerOpts).
- All lesson content is unverified by a native speaker (`needsNativeReview` in JSON).

## Screens / flow
- Feature flag `tamyr.registrationEnabled` (gradle.properties -> `BuildConfig.REGISTRATION_ENABLED`, default false = guest mode): Onboarding -> Intro (name + avatar, `feature/intro`) -> Home; sign-in/sign-up (`feature/auth`) are hidden. Flag on: legacy flow. `StartDestinationResolver`: guest mode: profile exists -> Home; onboarding seen -> Intro; else Onboarding; a signed-in user without profile gets one from the session name. Flag on: signed in -> Home; onboarding seen -> Auth; else Onboarding. Auth success and Result "continue" reset the back stack to Home. Home module tile -> `ModuleKey` (module screen, `feature/module`).
- Feature layout: `XContract.kt` (UiState/UiEvent/UiEffect), `XViewModel`, `XScreen.kt` (`XRoute` stateful + `XScreen` stateless).
- Compose Resources strings do not support `%%` or zero-padded specifiers (`%02d`): use a single `%` and format numbers in code.
- Testing iOS states without taps: launch with `-onboarding_seen YES`, `-session_name X -session_email Y` (NSUserDefaults args domain).

## Lessons / exercises
- One route `LessonKey(lessonId, stepIds)` and one `LessonViewModel` run the whole lesson; `LessonPlanner` turns content into `ExerciseStep`s (matching, quiz, listening, sentence, translation). `stepIds` restricts the lesson to given steps ("repeat mistakes").
- Rules: no lives (mistakes never end a lesson); a lesson is completed when accuracy >= `XpRules.PASS_THRESHOLD_PERCENT` (80); XP = 3 per correct step, saved per step; accuracy = correct / checked steps.
- Typed answers go through `AnswerNormalizer`: lower-case + punctuation stripped, then Russian-keyboard folding (ә→а, ө→о, ү→у, җ→ж, ң→н, һ→х) gives `Approximate` = accepted with the proper spelling shown.
- Daily reminder (30.10.2026): `core/notifications` (`Reminders` over `PlatformReminders`: Android WorkManager + `NotificationPermissionRequester` bridged from `MainActivity`; iOS `UNCalendarNotificationTrigger`), settings in `ReminderSettings` (off by default, 19:00, presets 08/13/19/21). Offered once in a sheet on the first passed lesson's result screen (slides in `TamyrMotion.REMINDER_PROMPT_DELAY_MS` after the screen is shown, not with the transition), toggle + presets in the profile; schedule is restored on app start. Local only, no backend. Notification texts are Russian and unverified; notification icon is the app icon (`TODO(design)`); sheet and profile row are not drawn in `design/` (`TODO(design)`). The test notification was checked by the author on a device (platform not recorded); check the daily schedule on both platforms.
- Mistake explanation: optional `explanation` on quiz/listening/sentence/translation tasks (DTO -> domain -> `StepFeedback.explanation`) is shown in the answer panel only after a wrong answer. Filled for all 16 listening tasks (translations of every option; needs native-speaker review); `ContentFilesTest` checks it.
- Back/close asks for confirmation (`NavigationBackHandler` from navigationevent-compose; `BackHandler` is deprecated).
- Audio: `AudioPlayer` (expect fun `createPlatformAudioPlayer`: MediaPlayer / AVPlayer; `play(url)` and `play(bytes)`) wrapped by `StubbingAudioPlayer`, which picks the source by `audioUrl`: `http(s)` -> platform player directly; any other path (`audio/oy.m4a`) -> read from `composeResources/files` and played from memory (Android `MediaDataSource`, iOS a temp file for AVPlayer); no `audioUrl` -> fakes playback (TODO(audio)). `LessonPlanner` skips listening steps without `audioUrl`. Recorded so far: 16 words in `files/audio/*.m4a` (correct answers of the 2 listening tasks in each of lessons 1-8; recorded by the author, trimmed/normalized, needs native-speaker review); quiz/translation words have no audio yet. Playback and the mistake explanation were checked by hand by the author (which platform: not recorded); check the other platform too.
- Tests (130 Android host, 128 iOS; `ContentFilesTest` reads lesson/audio files from disk and runs only on Android host): `./gradlew :shared:testAndroidHostTest :shared:iosSimulatorArm64Test`. Settings fake = `MapSettings` from `multiplatform-settings-test` (test-only).
- Haptics: `core/haptics` (`Haptics` semantic events correct/wrong/celebrate over `PlatformHaptics`; `PatternHaptics` adds patterns, 150 ms debounce and the profile setting `HapticsSettings`, default on). Called from `LessonViewModel.finishStep` and `ResultViewModel` (passed lessons only); no haptics on taps, pairs, buttons. Android needs `VIBRATE` permission; iOS uses Taptic Engine (verify on real devices, simulator does not vibrate).
- Motion tokens: `TamyrMotion` (step slide, feedback panel slide-up, progress bar tween).

## Methodology
- Decisions that affect learning (module/lesson structure, exercise types and order, pass threshold, unlock rules, motivation: streak/XP/lives, repetition, audio, feedback on mistakes) are discussed together with the subagent `language-teacher` (`.claude/agents/language-teacher.md`, a language teacher/methodist). Ask it before settling; its report is advice, the decision stays with the user. Unverified Tatar content stays marked "needs native-speaker review".
- What user data to collect and why (profile, questionnaire, registration/auth, consents, metrics, MVP scope) is discussed with the subagent `business-analyst` (`.claude/agents/business-analyst.md`, a mobile-app business analyst). Its report is advice, the decision stays with the user; legal points are marked "needs lawyer review".
- Как приложение зарабатывает и как привлекать пользователей (монетизация, пейвол, цены, ASO, каналы, партнёрства, рост, позиционирование) обсуждается с сабагентом `marketer` (`.claude/agents/marketer.md`, маркетолог и продакт по монетизации). Вызывать и тогда, когда новая идея может повлиять на доход или рост. Его отчёт это совет, решение за пользователем; платёжные, налоговые и юридические пункты помечаются «нужна проверка юриста/бухгалтера». Методические границы монетизации (мотивация, «жизни», доступ к урокам) согласуются ещё и с `language-teacher`.
- Каждая новая идея пользователя про продукт (фича, механика, контент, экран, мотивация, данные, монетизация) проходит через скилл `.claude/skills/idea-review`: четыре сабагента (`language-teacher`, `business-analyst`, `marketer`, `ux-designer`) запускаются параллельно, высказывается только тот, кого идея реально затрагивает (иначе отвечает `ПАС` и его не показывают), коротко и по делу. Не применять к багфиксам, рефакторингу, сборке и вопросам по коду. Решение остаётся за пользователем.
- Screens and UI elements drawn in `design/` (new or changed) are reviewed by the subagent `ux-designer` (`.claude/agents/ux-designer.md`, a mobile UI/UX expert): clarity, navigation, colors/contrast, comfort, accessibility; it may propose bold ideas that keep the brand. Its report is advice, the decision stays with the user.

## Status
Stages 1-6 done. Open: XP/level formula, native-speaker review of content.
