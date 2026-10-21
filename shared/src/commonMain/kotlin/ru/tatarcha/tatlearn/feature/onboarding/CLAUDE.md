# Онбординг

Три слайда при первом запуске; ведут к регистрации или входу. ТЗ для аналитика: `.docs/01-onboarding.md`. Макет `design/screens/01.png` устарел (там ещё ромбы и старые кнопки).

## Поток
- Запуск без сессии и без флага `onboarding_seen` -> онбординг (решает `StartDestinationResolver`).
- «Далее» листает, на последнем слайде «Начать учиться» -> `Auth(SignUp)`.
- «Пропустить» и «✕» с любого слайда -> `Auth(SignIn)`.
- Любой выход вызывает `OnboardingStorage.markSeen()`; без выхода (закрыли приложение) онбординг покажется снова.

## Файлы
- `OnboardingContract.kt` — `OnboardingUiState(pages, currentPage)` (+ `canGoBack`, `isLastPage`), события `PageChanged / NextClicked / BackClicked / SkipClicked`, эффект `NavigateToAuth(mode)`.
- `OnboardingViewModel.kt` — вся логика переходов; `finish(mode)` помечает онбординг виденным и шлёт эффект.
- `OnboardingScene.kt` — stateful: ViewModel, эффекты, системный «Назад» (`NavigationBackHandler`, включён со 2-го слайда).
- `OnboardingContent.kt` — stateless: верхняя панель, `HorizontalPager`, точки, кнопки. Превью рисуют только его.
- `OnboardingPageContent.kt` (слайд), `OnboardingTopBar.kt` (стрелка и крестик), `PagerDots.kt`, `OnboardingPage.kt` (модель и список слайдов).
- Тексты — `composeResources/values/strings.xml`, ключи `onboarding_*`.

## Решения и причины
- **Пейджер синхронизирован с `uiState.currentPage` в обе стороны** (свайп -> `PageChanged`, смена состояния -> `animateScrollToPage`). Источник правды — состояние (правило compose-style).
- **Свайп оставлен вместе с кнопками:** кнопка — главная подсказка, жест — привычка пользователей.
- **Точки и кнопки вынесены из слайда** и не зависят от высоты текста; у блока текста `heightIn(min = 160.dp)`, чтобы картинка не прыгала между слайдами.
- **Иконки «назад» и «✕» рисованные** (`core/designsystem/component/icon`): PT Sans не содержит символов «←/✕», запасной шрифт давал маленькие сдвинутые знаки.
- Ромбов над картинкой нет — это решение пользователя, они были чистым декором.

## Открытые вопросы
- Слайды 2 и 3 отсутствуют в макете; тексты слайдов — маркетинговый черновик, **needs native-speaker review** (в т. ч. «Исәнме»).
- Нет пробного урока без регистрации: последний слайд сразу ведёт к регистрации (главный риск конверсии).
- Подпись главной кнопки меняется без анимации.
- Макет и PNG надо привести к текущему экрану.

## Тесты
`ScreenViewModelsTest`: листание и выход на SignUp, «Назад» на 1-й странице, «Пропустить» -> SignIn, `PageChanged`.
