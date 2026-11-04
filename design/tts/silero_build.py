#!/usr/bin/env python3
"""Озвучка слов и фраз уроков моделью Silero (v5_cis_base_nostress, татарские голоса tat_marat и tat_albina, лицензия MIT).

Запуск из корня проекта (нужны torch и numpy, модель в ~/.cache/silero/v5_cis_base_nostress.pt, URL в README Silero):
    <python с torch> design/tts/silero_build.py
Два голоса диктора (выбор в профиле, по умолчанию мужской), оба для ВСЕХ слов, фраз и предложений уроков:
  - мужской tat_marat: files/audio/<slug>.m4a;
  - женский tat_albina: files/audio/female/<slug>.m4a.
Записи автора (21 слово) с 04.11.2026 заменены синтезом ради единого голоса курса; оригиналы лежат в design/tts/author.
Для каждого: подрезка тишины, громкость как у записей автора (RMS -16.3 дБ, пик не выше -1 дБ), AAC. Silero даёт
одинаковый результат при повторных запусках, поэтому исходные wav не храним: достаточно повторного запуска.
Модель — пакет PyTorch: при загрузке выполняется код из файла (файл просмотрен без исполнения, см. README).
После запуска: `python3 design/tts/wire_lessons.py` (если появились новые слова) и сборка проекта.
"""
import os
import subprocess
import sys
import tempfile

import numpy as np
import torch

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from audio_utils import master, trim_silence, write_wav  # noqa: E402
from words import AUTHOR_ALIASES, FILES, lesson_words, slug  # noqa: E402

MODEL = os.path.expanduser("~/.cache/silero/v5_cis_base_nostress.pt")
MALE, FEMALE = "tat_marat", "tat_albina"  # оба произносят все слова уроков верно (слепой тест 04.11.2026)
RATE = 48000

all_items = {}
for text in lesson_words():
    sl = AUTHOR_ALIASES.get(slug(text), slug(text))
    if sl not in all_items:  # регистр («Минем» и «минем») даёт один файл
        all_items[sl] = text
male_items = all_items
os.makedirs(f"{FILES}/audio/female", exist_ok=True)
print(f"слов и фраз в уроках: {len(all_items)}; мужской голос: {len(male_items)}; женский голос: {len(all_items)}")

model = torch.package.PackageImporter(MODEL).load_pickle("tts_models", "model")
model.to("cpu")


def synth(text, speaker):
    audio = model.apply_tts(text=text, speaker=speaker, sample_rate=RATE)
    return audio.detach().numpy() if hasattr(audio, "detach") else np.asarray(audio, dtype=np.float32)


# Проверка воспроизводимости на нескольких словах (если разное, исходники придётся хранить).
for text in list(all_items.values())[:3]:
    a, b = synth(text, MALE), synth(text, MALE)
    same = len(a) == len(b) and float(np.abs(a - b).max()) < 1e-4
    print(f"повтор «{text}»: {'одинаково' if same else 'ОТЛИЧАЕТСЯ'}")


def build(items, speaker, out_dir):
    for i, (sl, text) in enumerate(items.items(), 1):
        a = trim_silence(synth(text, speaker).astype(float), RATE)
        tmp = tempfile.mktemp(suffix=".wav")
        write_wav(tmp, master(a, RATE), rate=RATE)
        subprocess.run(["afconvert", "-f", "m4af", "-d", "aac", "-b", "56000", tmp, f"{out_dir}/{sl}.m4a"], check=True)
        os.unlink(tmp)
        print(f"{speaker} {i:3d}/{len(items)} {text} -> {sl}.m4a ({len(a) / RATE:.2f} с)")


build(male_items, MALE, f"{FILES}/audio")
build(all_items, FEMALE, f"{FILES}/audio/female")
print("готово")
