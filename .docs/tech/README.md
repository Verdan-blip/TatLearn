# Тамыр (TatLearn) — ТЗ по экранам и описание текущей работы

Документы описывают **что экран должен делать (ТЗ)** и **как он работает сейчас в коде**.
Актуально на состояние проекта: этапы 1–6 (см. `CLAUDE.md`). Источник правды по внешнему виду — `design/Tatar App Screens.dc.html` и `design/screens/NN.png`.

## Список документов

| Файл | Экран | Макет |
|---|---|---|
| [00-overview.md](00-overview.md) | Навигация, общие механики, данные, правила | — |
| [01-onboarding.md](01-onboarding.md) | Онбординг | `01.png` |
| [02-auth.md](02-auth.md) | Вход / Регистрация | `02.png` |
| [03-home.md](03-home.md) | Главная | `03.png` |
| [18-module.md](../18-module.md) | Экран модуля: `ModuleKey`, `ModuleViewModel`, `ModuleDetailsBuilder` | `18.png`–`23.png` |
| [04-lesson-matching.md](04-lesson-matching.md) | Упражнение «Сопоставление» | `04.png` |
| [05-lesson-quiz.md](05-lesson-quiz.md) | Упражнение «Квиз» | `05.png` |
| [06-result.md](06-result.md) | Результат урока | `06.png` |
| [16-coming-soon.md](../16-coming-soon.md) | Заглушка раздела: `ComingSoonState`, вкладки главной | `16.png` |
| [07-08-loading-error.md](07-08-loading-error.md) | Загрузка и ошибка (общие состояния) | `07.png`, `08.png` |
| [09-lesson-listening.md](09-lesson-listening.md) | Упражнение «Аудирование» | `09.png` |
| [10-lesson-sentence.md](10-lesson-sentence.md) | Упражнение «Сборка предложения» | `10.png` |
| [11-lesson-translation.md](11-lesson-translation.md) | Упражнение «Письменный перевод» | `11.png` |
| [12-showcase-debug.md](12-showcase-debug.md) | Debug UI kit | — |

## Как читать каждый документ
1. **Назначение** — зачем экран.
2. **Вход / выход** — откуда пришли и куда уходим.
3. **Элементы UI** — что видит пользователь.
4. **Функционал** — каждая функция и как она работает сейчас.
5. **Состояния** — loading / content / error и прочее.
6. **Данные и файлы** — где в коде лежит логика.
7. **Не реализовано / ограничения** — честный список пробелов.

Пути к коду даны от `shared/src/commonMain/kotlin/ru/tatarcha/tatlearn/` (в тексте — `…/`).
