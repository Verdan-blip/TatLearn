# feature/auth

Вход и регистрация. Спека для аналитика: `.docs/02-auth.md`, техническая: `.docs/tech/02-auth.md`. Макет: `design/screens/02.png`, уведомление: `design/screens/12.png`.

## Поток
`AuthScene` (stateful, Koin, эффекты навигации) -> `AuthContent` (stateless) <- `AuthViewModel` (`AuthUiState/UiEvent/UiEffect` в `AuthContract.kt`). Успех -> `NavigateToHome` (сброс стека на Home).

## Решения
- **Соцвход не входит в аккаунт, а показывает уведомление** `AuthToast.SocialUnavailable`. Раньше кнопки входили под фейковым аккаунтом; это скрывало, что OAuth нет, и в режиме `useRemote=true` ушло бы на сервер с общим паролем. `TODO(backend)`: настоящий OAuth.
- **Уведомление живёт в `uiState.toast`, таймер 5 с в ViewModel** (`TamyrMotion.TOAST_VISIBLE_MS`), чтобы его можно было тестировать на виртуальном времени. Новое заменяет старое. Закрытие: крестик, свайп вверх (внутри `TamyrToast`).
- **Ошибка запроса тоже уведомление** (`AuthToast.RequestFailed(mode, code)`, вид Error), а не строка под кнопкой: так она заметнее. Минус: пропадает через 5 с, перечитать нельзя. Тексты зависят от режима; сетевые коды объясняются словами, остальные показывают код.
- Компоненты уведомления лежат в `core/designsystem/component` (`TamyrToast`, `TamyrToastHost`, `TamyrToastData`, `TamyrToastKind`); здесь используются виды `Info` (соцвход) и `Error` (ошибка запроса).
- Имена `AuthScene`/`AuthContent` по скиллу `compose-style` (в остальных фичах пока `XRoute`/`XScreen`).

## Открытые вопросы
- Нет серверных кодов авторизации (неверный пароль, занятый e-mail): `TODO(backend)`.
- «Забыли пароль?» не кликабельна (`TODO(design)`).
- Логотипы провайдеров в макете цветные метки (`TODO(design)`).
- Текст «Скоро · Тиздән» в обсуждавшихся вариантах не использован; татарский текст в уведомлении отсутствует, проверка носителем не требуется.
