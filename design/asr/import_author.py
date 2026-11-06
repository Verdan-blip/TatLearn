#!/usr/bin/env python3
"""Берёт готовые записи автора (design/tts/author/*.m4a, 21 слово) и готовит их для замера распознавания.

Запуск из корня проекта (macOS, нужен системный afconvert): python3 design/asr/import_author.py
Результат: design/asr/recordings/islam_<имя файла>.wav (16 кГц, моно) и design/asr/phrases_author.csv (слово из урока по имени файла).
Дальше: python3 design/asr/evaluate.py --model <модель> --phrases design/asr/phrases_author.csv
Записи автора это эталонный диктор (носитель, чёткая речь), а не изучающий: это верхняя граница качества, а не типичный случай.
"""
import csv
import glob
import json
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
sys.path.insert(0, os.path.join(ROOT, "design", "tts"))
from words import AUTHOR_ALIASES, slug  # noqa: E402

LESSONS = os.path.join(ROOT, "shared", "src", "commonMain", "composeResources", "files", "lessons")
ARCHIVE = os.path.join(ROOT, "design", "tts", "author")
OUT = os.path.join(HERE, "recordings")


def lesson_words():
    found = {}
    for path in sorted(glob.glob(os.path.join(LESSONS, "lesson_*.json"))):
        lesson = json.load(open(path, encoding="utf-8"))
        texts = [p["tatar"] for p in lesson["matching"]["pairs"]]
        texts += [q["word"] for q in lesson["quiz"]]
        texts += [w for s in lesson["sentences"] for w in s["wordBank"]]
        for text in texts:
            key = AUTHOR_ALIASES.get(slug(text), slug(text))
            found.setdefault(key, text)
    return found


def main():
    os.makedirs(OUT, exist_ok=True)
    words = lesson_words()
    rows = []
    for path in sorted(glob.glob(os.path.join(ARCHIVE, "*.m4a"))):
        name = os.path.splitext(os.path.basename(path))[0]
        if name not in words:
            print("нет слова в уроках для файла:", name)
            continue
        target = os.path.join(OUT, f"islam_{name}.wav")
        subprocess.run(["afconvert", "-f", "WAVE", "-d", "LEI16@16000", "-c", "1", path, target], check=True)
        rows.append((name, "word", words[name], ""))
    with open(os.path.join(HERE, "phrases_author.csv"), "w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["id", "kind", "tatar", "russian"])
        writer.writerows(rows)
    print(f"готово: {len(rows)} записей автора -> {OUT}")


if __name__ == "__main__":
    main()
