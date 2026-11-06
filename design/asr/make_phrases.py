#!/usr/bin/env python3
"""Собирает список слов и фраз для проверки распознавания татарской речи: design/asr/phrases.csv.

Запуск из корня проекта: python3 design/asr/make_phrases.py
Берёт из уроков (composeResources/files/lessons/*.json) по 3 слова сопоставления и до 2 предложений из сборки на урок.
Татарский текст здесь взят из уроков и не проверен носителем: needs native-speaker review.
"""
import csv
import glob
import json
import os

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..")
LESSONS = os.path.join(ROOT, "shared", "src", "commonMain", "composeResources", "files", "lessons")
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "phrases.csv")


def main():
    rows = []
    for path in sorted(glob.glob(os.path.join(LESSONS, "lesson_*.json"))):
        lesson = json.load(open(path, encoding="utf-8"))
        lid = lesson["lesson"]["id"].replace("lesson_", "l")
        for i, pair in enumerate(lesson["matching"]["pairs"][:3]):
            rows.append((f"{lid}_w{i + 1}", "word", pair["tatar"], pair["russian"]))
        for i, sentence in enumerate(lesson["sentences"][:2]):
            rows.append((f"{lid}_s{i + 1}", "phrase", " ".join(sentence["answer"]), sentence["promptRu"]))
    with open(OUT, "w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["id", "kind", "tatar", "russian"])
        writer.writerows(rows)
    print(f"{len(rows)} строк: {sum(1 for r in rows if r[1] == 'word')} слов и {sum(1 for r in rows if r[1] == 'phrase')} фраз -> {OUT}")


if __name__ == "__main__":
    main()
