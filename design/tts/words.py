"""Слова уроков и пути к озвучке (без тяжёлых зависимостей: не нужны ни numpy, ни espeak)."""
import json
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
FILES = f"{ROOT}/shared/src/commonMain/composeResources/files"
# Имена файлов автора, у которых «ә» записана как «e» (остальные совпадают с транслитерацией).
AUTHOR_ALIASES = {"isanmesez": "isenmesez", "isanme": "isenme"}
TR = {"а": "a", "ә": "a", "б": "b", "в": "v", "г": "g", "д": "d", "е": "e", "ё": "yo", "ж": "zh", "җ": "j", "з": "z", "и": "i",
      "й": "y", "к": "k", "л": "l", "м": "m", "н": "n", "ң": "n", "о": "o", "ө": "o", "п": "p", "р": "r", "с": "s", "т": "t",
      "у": "u", "ү": "u", "ф": "f", "х": "h", "һ": "h", "ц": "ts", "ч": "ch", "ш": "sh", "щ": "sch", "ъ": "", "ы": "y",
      "ь": "", "э": "e", "ю": "yu", "я": "ya", " ": "_"}
LESSON_ORDER = ["lesson_9"] + [f"lesson_{i}" for i in range(1, 9)]


def slug(text):
    """Имя файла озвучки: транслитерация (как у записей автора), пробелы в подчёркивание."""
    return re.sub(r"_+", "_", "".join(TR.get(c, "") for c in text.lower())).strip("_")


def audio_path(text):
    """Относительный путь озвучки слова или фразы (`audio/<slug>.m4a`), если файл есть, иначе None."""
    sl = AUTHOR_ALIASES.get(slug(text), slug(text))
    return f"audio/{sl}.m4a" if os.path.exists(f"{FILES}/audio/{sl}.m4a") else None


def lesson_words():
    """Все татарские слова и фразы уроков в порядке курса: текст -> {ru, lessons}.

    Сюда входят слова сопоставления, квиза, слушания, слова банков сборки предложений и сами предложения (ответы).
    """
    words = {}
    for lid in LESSON_ORDER:
        d = json.load(open(f"{FILES}/lessons/{lid}.json", encoding="utf-8"))

        def add(t, ru=None):
            t = t.strip()
            if t:
                e = words.setdefault(t, {"ru": None, "lessons": []})
                e["ru"] = e["ru"] or ru
                if lid not in e["lessons"]:
                    e["lessons"].append(lid)

        for pr in d.get("matching", {}).get("pairs", []):
            add(pr["tatar"], pr["russian"])
        for q in d.get("quiz", []):
            add(q["word"], q["options"][q["correctIndex"]])
        for lt in d.get("listening", []):
            for o in lt.get("options", []):
                add(o)
        for s in d.get("sentences", []):
            for w in s["wordBank"]:
                add(w)
            add(" ".join(s["answer"]), s.get("promptRu"))
    return words
