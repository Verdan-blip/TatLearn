# 09. Упражнение «Аудирование»

Макет: `design/screens/09.png`. Код: `…/feature/lesson/ListeningContent.kt`, `…/core/audio/AudioPlayer.kt`, платформенные `PlatformAudioPlayer.android.kt` / `.ios.kt`. Общий каркас — в [04-lesson-matching.md](04-lesson-matching.md), часть A.

<img src="../screens/09.png" alt="Макет 09" width="220">

## Назначение
Послушать запись и выбрать, что было сказано.

## Элементы UI
1. Тег «Тыңла · аудирование», заголовок «Что ты услышал?».
2. Карточка с большой кнопкой воспроизведения с «волной» (`WavePlayButton`) и ссылкой «Помедленнее».
3. Варианты ответа (жирный текст, без номеров).
4. Футер.

## Функционал (как работает сейчас)
| Функция | Поведение |
|---|---|
| Воспроизведение | `PlayAudioClicked()` → `StubbingAudioPlayer.play(audioUrl, 1.0)`. |
| «Помедленнее» | `PlayAudioClicked(slow = true)` → скорость 0.75. |
| Состояние плеера | `AudioPlayerState` (`Idle / Playing / Completed / Error`) прокидывается в `LessonUiState.audio`: при `Playing` столбики `WavePlayButton` колеблются, при `Error` под ссылкой «Помедленнее» показывается подпись (`TODO(design)`). |
| Нет `audioUrl` | `LessonPlanner` не включает задание в урок; в других местах плеер-заглушка «играет» 1.2 с. `TODO(audio)`. |
| Есть `audioUrl` | `http(s)` играет платформа напрямую (Android — `MediaPlayer`, iOS — `AVPlayer`); путь вида `audio/oy.m4a` читается из `composeResources/files` и играется из памяти. |
| Остановка | При проверке ответа, подтверждении выхода и закрытии экрана (`player.release()`). |
| Выбор / проверка | Как в квизе: выбрать вариант → «Проверить»; верный — зелёный; неверный — красный + правильный зелёным. Деталь верного ответа — сам вариант; при ошибке в панели добавляется `explanation` из JSON. |

## Данные
`listening[]`: `audioUrl`, `options[]`, `correctIndex`, `explanation`. В каждом из `lesson_1..8.json` по 2 задания, записи в `files/audio/`. Тест `ContentFilesTest` проверяет, что файл есть, а `explanation` упоминает каждый вариант.

## Не реализовано / ограничения
- Озвучены только правильные ответы (16 слов), без проверки носителем.
- Нет автовоспроизведения при показе шага, нет счётчика прослушиваний. Ошибка воспроизведения показывается подписью, но iOS пока не следит за сбоями `AVPlayerItem` (`TODO(audio)`).
- Проверить iOS-часть: запустить симулятор, открыть урок, нажать play с URL (`expect/actual` — комментарий в `PlatformAudioPlayer.ios.kt`).
