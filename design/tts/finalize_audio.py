#!/usr/bin/env python3
"""Итоговая сборка озвучки по выбору на странице прослушивания.

Запуск из корня проекта:
    python3 design/tts/finalize_audio.py ~/Downloads/audition-choices.json [--audition DIR]
Для каждого выбранного слова: берёт без потерь wav выбранного варианта, выравнивает громкость под записи автора
(RMS активной части -16.3 дБ, пик не выше -1 дБ), апсемплит до 44,1 кГц, кодирует в m4a (AAC) и кладёт в files/audio.
Исходные wav выбранных вариантов копируются в design/tts/chosen (случайный синтез воспроизвести нельзя), выбор
дописывается в design/tts/choices.json. Затем вопросам квиза, у которых есть файл озвучки, проставляется audioUrl.
Нужны afconvert (macOS) и numpy.
"""
import argparse
import json
import os
import shutil
import subprocess
import sys
import tempfile

import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from wire_lessons import wire_lessons  # noqa: E402
from words import FILES  # noqa: E402
from tts_run import read_wav, write_wav  # noqa: E402

TARGET_RMS_DB, PEAK_LIMIT_DB = -16.3, -1.0


def master(sr, a):
    """Громкость как у записей автора, мягкие края, 44,1 кГц."""
    act = a[np.abs(a) > 0.02]
    gain = 10 ** ((TARGET_RMS_DB - 20 * np.log10(np.sqrt((act ** 2).mean()) + 1e-9)) / 20)
    peak = np.abs(a).max() * gain
    if peak > 10 ** (PEAK_LIMIT_DB / 20):
        gain *= 10 ** (PEAK_LIMIT_DB / 20) / peak
    a = a * gain
    fi, fo = int(0.003 * sr), int(0.012 * sr)
    a[:fi] *= np.linspace(0, 1, fi)
    a[-fo:] *= np.linspace(1, 0, fo)
    t = np.arange(len(a) * 2) / 2
    up = np.interp(t, np.arange(len(a)), a)
    return (np.clip(up, -1, 1) * 32767).astype(np.int16)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("choices")
    ap.add_argument("--audition", default=os.path.expanduser("~/Downloads/TatarTTS-audition"))
    args = ap.parse_args()
    choices = json.load(open(os.path.expanduser(args.choices), encoding="utf-8"))
    mpath = f"{args.audition}/manifest.json"
    manifest = {w["slug"]: w for w in json.load(open(mpath, encoding="utf-8"))["words"]} if os.path.exists(mpath) else {}
    for sl, c in choices.items():  # какой вариант фонем и профиль шума выбран: пригодится при следующих запусках
        v = next((x for x in manifest.get(sl, {}).get("variants", []) if str(x["n"]) == str(c["choice"])), None)
        if v:
            c["profile"], c["ipa"] = v["profile"], v.get("ipa")
    os.makedirs(f"{HERE}/chosen", exist_ok=True)
    made, skipped = [], []
    for sl, c in choices.items():
        if c["choice"] in (None, "none"):
            skipped.append(c["text"])
            continue
        src = f"{args.audition}/audio/{sl}/{c['choice']}.wav"
        sr, a = read_wav(src)
        shutil.copy(src, f"{HERE}/chosen/{sl}.wav")
        tmp = tempfile.mktemp(suffix=".wav")
        write_wav(tmp, master(sr, a), rate=sr * 2)
        subprocess.run(["afconvert", "-f", "m4af", "-d", "aac", "-b", "56000", tmp, f"{FILES}/audio/{sl}.m4a"], check=True)
        os.unlink(tmp)
        made.append(sl)
    path = f"{HERE}/choices.json"
    allc = json.load(open(path, encoding="utf-8")) if os.path.exists(path) else {}
    allc.update(choices)
    json.dump(allc, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=1)

    patched = wire_lessons()
    print(f"озвучено слов: {len(made)}; без выбора («ни один»): {len(skipped)}: {skipped}; проставлен audioUrl: {patched}")


if __name__ == "__main__":
    main()
