# Проверка распознавания татарской речи (бета упражнения «Скажи вслух»)

Что и зачем: решение владельца 06.11.2026 — упражнение на говорение необязательное, распознаватель подсвечивает зелёным узнанные слова (неузнанные просто не подсвечиваются, красного нет), при щедром пороге упражнение засчитывается и даёт повышенный XP; на прохождение урока и точность оно не влияет. Перед тем как встраивать модель в приложение, нужно понять, сколько слов она узнаёт у носителей и у изучающих.

## Быстрый старт на готовых записях автора
У автора уже есть записи диктора (`design/tts/author`, 21 слово, носитель и чёткая речь). `python3 design/asr/import_author.py` переводит их в WAV 16 кГц и готовит `phrases_author.csv`; замер: `/tmp/asr-venv/bin/python design/asr/evaluate.py --model mcronomus/whisper-small-tt --phrases design/asr/phrases_author.csv`. Это верхняя граница качества (эталонный диктор); для реального случая нужны ещё записи изучающего и других носителей.

## Как проверить
1. `python3 design/asr/make_phrases.py` собирает `phrases.csv` (27 слов и 18 фраз из уроков; татарский текст из уроков, needs native-speaker review).
2. Записать (проще всего диктофоном: `/tmp/asr-venv/bin/pip install sounddevice soundfile numpy`, затем `/tmp/asr-venv/bin/python design/asr/record.py --voice mama`, Enter начинает и останавливает запись, повторный запуск продолжает с места остановки; голоса: `mama`, `papa`, `babushka`, `islam`). Можно и вручную: каждую строку (лучше все, минимум 20 слов и 10 фраз) голосами: мама, папа, бабушка и вы (как изучающий, с вашим произношением). Формат: WAV, 16 кГц, моно; имя `<голос>_<id>.wav`, например `mama_l1_w1.wav`, в папку `design/asr/recordings/`. Записи это личные данные, папка в `.gitignore`, в репозиторий не добавлять.
3. Окружение и запуск (модель скачивается и запускается на вашем компьютере, делайте в отдельном виртуальном окружении):
   ```
   python3 -m venv /tmp/asr-venv && /tmp/asr-venv/bin/pip install torch transformers librosa soundfile "optimum[onnxruntime]"
   /tmp/asr-venv/bin/python design/asr/evaluate.py --model mcronomus/whisper-small-tt
   /tmp/asr-venv/bin/python design/asr/evaluate.py --model yasalma/whisper-finetuned-tatartts-asr
   /tmp/asr-venv/bin/python design/asr/evaluate.py --model dhcppc0/soyle_onnx --backend onnx
   ```
   Для базовой линии можно добавить `--model openai/whisper-small` (татарский без дообучения).
4. Смотреть итог по голосам: среднюю долю узнанных слов и долю записей, где узнано не меньше 30, 50 и 70 процентов слов: по ним выбирается щедрый порог для беты и сама модель.

## Кандидаты (по страницам и API Hugging Face на 06.11.2026, лицензии не проверены юристом)
- `mcronomus/whisper-small-tt`: Apache-2.0, WER около 34,8% на Common Voice 16.1 (самооценка автора), около 0,2 млрд параметров; в карточке неясно, какая база.
- `yasalma/whisper-finetuned-tatartts-asr`: Apache-2.0, дообучен на TatarTTS (чтение вслух), WER и размер не указаны, модель новая.
- Söyle (`dhcppc0/soyle_onnx`, формат ONNX, ISSAI): на странице проекта CC BY 4.0 с просьбой указать атрибуцию; лицензия самих весов не подтверждена; обучен на Common Voice 13 и Tatar Speech Corpus.
- Общий Whisper (`openai/whisper-small`, `-base`, `-tiny`) как базовая линия; для телефона есть готовые форматы (`handy-computer/whisper-small-gguf`, `litert-community/whisper-tiny`), но они без татарского дообучения.
- Meta MMS не подходит: лицензия CC-BY-NC (некоммерческая).
