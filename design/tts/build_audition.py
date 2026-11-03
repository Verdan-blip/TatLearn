#!/usr/bin/env python3
"""Страница выбора озвучки: для слов без записи автора генерирует варианты мужским голосом TatarTTS.

Запуск из корня проекта (окружение и модели см. README.md):
    python3 design/tts/build_audition.py [--only слово,слово] [--out DIR]
Результат: DIR/index.html (по умолчанию ~/Downloads/TatarTTS-audition), DIR/audio/<slug>/<n>.wav, DIR/manifest.json.
Слова берутся из уроков (сопоставление, квиз, слушание), у которых нет записи автора в files/audio.
Для слов с систематической ошибкой произношения в overrides.json можно задать свои фонемы (IPA) вместо espeak.
"""
import argparse
import json
import os
import re
import statistics
import sys

import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from tts_run import Voice, write_wav  # noqa: E402

ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
FILES = f"{ROOT}/shared/src/commonMain/composeResources/files"
CANDIDATES, SHOWN = 8, 5
PROFILES = [(0.667, 0.8), (0.5, 0.6), (0.4, 0.5), (0.3, 0.4)]  # (noise_scale, noise_w): от «живого» к «стабильному»
# Имена файлов автора, у которых «ә» записана как «e» (остальные совпадают с транслитерацией).
AUTHOR_ALIASES = {"isanmesez": "isenmesez", "isanme": "isenme"}
TR = {"а": "a", "ә": "a", "б": "b", "в": "v", "г": "g", "д": "d", "е": "e", "ё": "yo", "ж": "zh", "җ": "j", "з": "z", "и": "i",
      "й": "y", "к": "k", "л": "l", "м": "m", "н": "n", "ң": "n", "о": "o", "ө": "o", "п": "p", "р": "r", "с": "s", "т": "t",
      "у": "u", "ү": "u", "ф": "f", "х": "h", "һ": "h", "ц": "ts", "ч": "ch", "ш": "sh", "щ": "sch", "ъ": "", "ы": "y",
      "ь": "", "э": "e", "ю": "yu", "я": "ya", " ": "_"}


def slug(text):
    return re.sub(r"_+", "_", "".join(TR.get(c, "") for c in text.lower())).strip("_")


def lesson_words():
    """Все татарские слова и фразы уроков в порядке курса: текст -> {ru, lessons}."""
    order = ["lesson_9"] + [f"lesson_{i}" for i in range(1, 9)]
    words = {}
    for lid in order:
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
    return words


def has_author_audio(sl):
    return AUTHOR_ALIASES.get(sl, sl) in {os.path.splitext(f)[0] for f in os.listdir(f"{FILES}/audio")} or \
        os.path.exists(f"{FILES}/audio/{sl}.m4a")


def trim(a):
    idx = np.where(np.abs(a.astype(float)) / 32767 > 0.02)[0]
    if len(idx) == 0:
        return a
    pad = int(0.06 * 22050)
    return a[max(0, idx[0] - pad): min(len(a), idx[-1] + pad)]


HTML = open(f"{HERE}/audition.html", encoding="utf-8").read()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--only", help="через запятую: только эти слова (текст)")
    ap.add_argument("--out", default=os.path.expanduser("~/Downloads/TatarTTS-audition"))
    args = ap.parse_args()
    overrides = json.load(open(f"{HERE}/overrides.json", encoding="utf-8")) if os.path.exists(f"{HERE}/overrides.json") else {}
    words = {t: e for t, e in lesson_words().items() if not has_author_audio(slug(t))}
    if args.only:
        keep = {w.strip() for w in args.only.split(",")}
        words = {t: e for t, e in words.items() if t in keep}
    print("слов без записи автора:", len(words))
    voice = Voice("male")
    os.makedirs(f"{args.out}/audio", exist_ok=True)
    manifest = []
    for n, (text, e) in enumerate(words.items(), 1):
        sl = slug(text)
        ipa = overrides.get(text, {}).get("ipa")
        cands = []
        for i in range(CANDIDATES):
            ns, nw = PROFILES[i % len(PROFILES)]
            _, missing, audio = voice.synth(text, ipa=ipa, noise_scale=ns, noise_w=nw)
            cands.append({"profile": i % len(PROFILES), "audio": trim(audio), "missing": missing})
        durs = [len(c["audio"]) / 22050 for c in cands]
        med = statistics.median(durs)
        good = sorted((c for c, d in zip(cands, durs) if 0.65 * med <= d <= 1.5 * med), key=lambda c: abs(len(c["audio"]) / 22050 - med))[:SHOWN]
        good = sorted(good, key=lambda c: c["profile"])
        os.makedirs(f"{args.out}/audio/{sl}", exist_ok=True)
        variants = []
        for k, c in enumerate(good, 1):
            write_wav(f"{args.out}/audio/{sl}/{k}.wav", c["audio"])
            variants.append({"n": k, "profile": c["profile"], "dur": round(len(c["audio"]) / 22050, 2)})
        manifest.append({"slug": sl, "text": text, "ru": e["ru"], "lessons": e["lessons"], "variants": variants, "ipa": ipa})
        print(f"{n:2d}/{len(words)} {text} -> {sl}: {len(good)} вариантов")
    json.dump({"profiles": PROFILES, "words": manifest}, open(f"{args.out}/manifest.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    open(f"{args.out}/index.html", "w", encoding="utf-8").write(HTML.replace("__DATA__", json.dumps(manifest, ensure_ascii=False)))
    print("готово:", args.out)


if __name__ == "__main__":
    main()
