# Тамыр (TatLearn)

Приложение для изучения татарского языка (русскоязычная аудитория). Kotlin Multiplatform + Compose Multiplatform, Android и iOS; вся логика и весь UI лежат в `commonMain`.
Макеты: `design/Tatar App Screens.dc.html` и `design/screens/01–11.png`.

## Структура
- `shared/` — весь код (iOS-фреймворк `Shared`), пакет `ru.tatarcha.tatlearn`: `core`, `domain`, `data`, `feature`, `navigation`, `di`.
- `androidApp/` — точка входа Android. `iosApp/` — Xcode-проект (только точка входа).
- Версии библиотек — только в `gradle/libs.versions.toml`.

## Стек
Koin, Navigation 3 (JetBrains), Ktor + Ktorfit (KSP), Coil 3, SQLDelight, multiplatform-settings, kotlinx.serialization. Архитектура MVI/UDF: `UiState` / `UiEvent` / `UiEffect` + `ViewModel`, экраны делятся на stateful `Route` и stateless `Screen`.

## Запуск
Нужен JDK из Android Studio: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.

- Android: `./gradlew :androidApp:assembleDebug` или запуск `androidApp` из Android Studio.
- iOS: откройте `iosApp/iosApp.xcodeproj` в Xcode, выберите симулятор и нажмите Run. Фреймворк собирается автоматически. Проверка без Xcode: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`.
- Тесты: `./gradlew :shared:testAndroidHostTest :shared:iosSimulatorArm64Test`.

## Данные и бэкенд
По умолчанию работают фейковые репозитории (JSON из `composeResources/files`, задержка 300–800 мс). Реальный API включается в `gradle.properties`: `tamyr.useRemote=true` и `tamyr.baseUrl=…`. Прогресс (XP, серия, уроки) хранится локально в SQLDelight, сессия — в multiplatform-settings.

## Отладка состояний
- Кнопка «UI kit» на Главной (только debug) открывает витрину компонентов, там же переключатель симуляции ошибок сети.
- iOS без нажатий: аргументы запуска `-onboarding_seen YES`, `-session_name Алия -session_email a@b.ru`.

## Контент и открытые вопросы
- Весь татарский контент не проверен носителем (`needsNativeReview` в JSON, `TODO(content)` в коде): слова, транскрипции, пословица, правила сопоставления букв при вводе.
- Состояния без макета помечены `TODO(design)`; маскот намеренно не нарисован.
- Аудио: пока заглушка (`TODO(audio)`), пока у заданий нет `audioUrl`.

Подробные правила для разработки — в `CLAUDE.md`.
