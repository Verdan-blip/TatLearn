#!/usr/bin/env python3
"""Проставляет audioUrl в уроках по файлам озвучки в files/audio (идемпотентно).

Запуск из корня проекта: python3 design/tts/wire_lessons.py
  - квиз: audioUrl слова;
  - сопоставление: audioUrl каждой пары (татарская карточка);
  - сборка предложения: wordAudio (слово банка -> путь) и audioUrl предложения целиком.
Записи автора и синтез не различаются: берётся любой файл `audio/<slug>.m4a` (см. words.py).
"""
import json
import os

from words import FILES, audio_path


def wire_lessons():
    counts = {"quiz": 0, "pairs": 0, "bank_words": 0, "sentences": 0}
    for lid in [f"lesson_{i}" for i in range(1, 10)]:
        p = f"{FILES}/lessons/{lid}.json"
        d = json.load(open(p, encoding="utf-8"))
        for q in d.get("quiz", []):
            path = audio_path(q["word"])
            if path and not q.get("audioUrl"):
                q["audioUrl"] = path
                counts["quiz"] += 1
        for pr in d.get("matching", {}).get("pairs", []):
            path = audio_path(pr["tatar"])
            if path and not pr.get("audioUrl"):
                pr["audioUrl"] = path
                counts["pairs"] += 1
        for s in d.get("sentences", []):
            wa = s.get("wordAudio", {})
            for w in s["wordBank"]:
                path = audio_path(w)
                if path and w not in wa:
                    wa[w] = path
                    counts["bank_words"] += 1
            if wa:
                s["wordAudio"] = wa
            path = audio_path(" ".join(s["answer"]))
            if path and not s.get("audioUrl"):
                s["audioUrl"] = path
                counts["sentences"] += 1
        open(p, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")
    return counts


if __name__ == "__main__":
    print("проставлено:", wire_lessons())
