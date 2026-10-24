# feature/auth

Вход и регистрация. Спека для аналитика: `.docs/02-auth.md`, техническая: `.docs/tech/02-auth.md`. Макет: `design/screens/02.png`, уведомление: `design/screens/12.png`.

## Поток
`AuthScene` (stateful, Koin, эффекты навигации) -> `AuthContent` (stateless) <- `AuthViewModel` (`AuthUiState/UiEvent/UiEffect` в `AuthContract.kt`). Успех -> `NavigateToHome` (сброс стека на Home).

## Решения
- **Соцвход не входит в аккаунт, а показывает уведомление** `AuthToast.SocialUnavailable`. Раньше кнопки входили под фейковым аккаунтом; это скрывало, что OAuth нет, и в режиме `useRemote=true` ушло бы на сервер с общим паролем. `TODO(backend)`: настоящий OAuth.
- **Уведомление живёт в `uiState.toast`, таймер 5 с в ViewModel** (`TamyrMotion.TOAST_VISIBLE_MS`), чтобы его можно было тестировать на виртуальном времени. Новое заменяет старое. Закрытие: крестик, свайп вверх (внутри `TamyrToast`).
- Компоненты уведомления лежат в `core/designsystem/component` (`TamyrToast`, `TamyrToastHost`, `TamyrToastData`, `TamyrToastKind`); здесь используется только вид `Info`.
- Имена `AuthScene`/`AuthContent` по скиллу `compose-style` (в остальных фичах пока `XRoute`/`XScreen`).

## Открытые вопросы
- Ошибка запроса всё ещё строка под кнопкой, а не `TamyrToastKind.Error` (кандидат на перенос, нужно решение по дизайну).
- Нет разных текстов для неверных данных, занятого e-mail и сети; «Забыли пароль?» не кликабельна (`TODO(design)`).
- Логотипы провайдеров в макете цветные метки (`TODO(design)`).
- Текст «Скоро · Тиздән» в обсуждавшихся вариантах не использован; татарский текст в уведомлении отсутствует, проверка носителем не требуется.
