#!/usr/bin/env python3
"""Диктофон для замера распознавания: показывает слово или фразу, записывает ответ в design/asr/recordings/<голос>_<id>.wav.

Запуск (из корня проекта, нужны sounddevice и soundfile):
    python3 design/asr/record.py --voice mama
    python3 design/asr/record.py --voice mama --kind word --only l3
Управление: Enter начинает запись, Enter останавливает; пустая строка перед Enter не нужна. «p» и Enter пропускают слово,
«r» и Enter записывает заново, «q» и Enter выходит. Уже записанные файлы по умолчанию пропускаются (повторный запуск продолжает с места остановки).
Формат: WAV, 16 кГц, моно. Записи голосов это личные данные, каталог recordings в git не попадает.
"""
import argparse
import csv
import os
import queue
import sys

SAMPLE_RATE = 16_000


def record_until_enter(sd):
    chunks = queue.Queue()

    def callback(indata, frames, time, status):
        chunks.put(indata.copy())

    with sd.InputStream(samplerate=SAMPLE_RATE, channels=1, dtype="float32", callback=callback):
        input("   ● идёт запись, Enter чтобы остановить... ")
    parts = []
    while not chunks.empty():
        parts.append(chunks.get())
    return parts


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--voice", required=True, help="имя голоса в названии файла, например mama, papa, babushka, islam")
    parser.add_argument("--kind", choices=["word", "phrase"], help="записывать только слова или только фразы")
    parser.add_argument("--only", help="только id, начинающиеся с этого префикса, например l3")
    parser.add_argument("--redo", action="store_true", help="перезаписывать уже записанное")
    parser.add_argument("--phrases", default=os.path.join(os.path.dirname(__file__), "phrases.csv"))
    args = parser.parse_args()

    import numpy as np
    import sounddevice as sd
    import soundfile as sf

    out_dir = os.path.join(os.path.dirname(__file__), "recordings")
    os.makedirs(out_dir, exist_ok=True)
    rows = [r for r in csv.DictReader(open(args.phrases, encoding="utf-8")) if (not args.kind or r["kind"] == args.kind) and (not args.only or r["id"].startswith(args.only))]
    for number, row in enumerate(rows, 1):
        path = os.path.join(out_dir, f"{args.voice}_{row['id']}.wav")
        if os.path.exists(path) and not args.redo:
            continue
        while True:
            print(f"\n[{number}/{len(rows)}] {row['id']}  ({row['kind']})\n   {row['tatar']}\n   ({row['russian']})")
            answer = input("   Enter: записать, p: пропустить, q: выйти > ").strip().lower()
            if answer == "q":
                return
            if answer == "p":
                break
            parts = record_until_enter(sd)
            if not parts:
                print("   ничего не записано")
                continue
            audio = np.concatenate(parts)
            sf.write(path, audio, SAMPLE_RATE)
            print(f"   сохранено: {path} ({len(audio) / SAMPLE_RATE:.1f} с)")
            if input("   Enter: дальше, r: перезаписать > ").strip().lower() != "r":
                break
    print("\nГотово.")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        sys.exit(130)
