#!/usr/bin/env python3
"""Пилот Silero TTS (v5_cis_base_nostress, татарские голоса tat_marat и tat_albina, лицензия MIT): слепое прослушивание.

Запуск из корня проекта (нужен torch; модель лежит в ~/.cache/silero/v5_cis_base_nostress.pt, URL в README Silero):
    <python с torch> design/tts/silero_pilot.py
Модель Silero — пакет PyTorch: при загрузке выполняется код из файла (перед запуском файл просмотрен без исполнения:
внутри TorchScript и один обычный python-файл без сети и запуска процессов). Результат: ~/Downloads/Silero-pilot/index.html:
для 21 слова с записью автора четыре записи в случайном порядке (автор, TatarTTS, tat_marat, tat_albina), ответы в ANSWERS.txt.
"""
import json
import os
import random
import shutil
import subprocess
import wave

import numpy as np
import torch

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
AUTHOR = f"{HERE}/author"  # оригиналы записей автора (в приложении заменены синтезом)
TATARTTS = os.path.expanduser("~/Downloads/TatarTTS-pilot/male")  # мужской голос TatarTTS из первого пилота (может не быть)
OUT = os.path.expanduser("~/Downloads/Silero-pilot")
MODEL = os.path.expanduser("~/.cache/silero/v5_cis_base_nostress.pt")
SPEAKERS = ["tat_marat", "tat_albina"]
WORDS = {
    "alma": "алма", "ati": "әти", "babay": "бабай", "egerme": "егерме", "ike": "ике", "isenme": "Исәнме",
    "isenmesez": "Исәнмесез", "it": "ит", "jyr": "җыр", "kuz": "күз", "kysh": "кыш", "men": "мең", "oy": "өй",
    "pesi": "песи", "rahmat": "Рәхмәт", "sau_bulygyz": "Сау булыгыз", "syer": "сыер", "tugyz": "тугыз",
    "yahshy": "Яхшы", "yangyr": "яңгыр", "yoz": "йөз",
}

model = torch.package.PackageImporter(MODEL).load_pickle("tts_models", "model")
model.to("cpu")
print("татарские голоса в модели:", [s for s in getattr(model, "speakers", []) if s.startswith("tat")])

for d in ["author"] + SPEAKERS + ["tatartts"]:
    os.makedirs(f"{OUT}/{d}", exist_ok=True)
sources = ["author"] + (["tatartts"] if os.path.isdir(TATARTTS) else []) + SPEAKERS
for key, text in WORDS.items():
    shutil.copy(f"{AUTHOR}/{key}.m4a", f"{OUT}/author/{key}.m4a")
    if os.path.isdir(TATARTTS) and os.path.exists(f"{TATARTTS}/{key}.m4a"):
        shutil.copy(f"{TATARTTS}/{key}.m4a", f"{OUT}/tatartts/{key}.m4a")
    for sp in SPEAKERS:
        audio = model.apply_tts(text=text, speaker=sp, sample_rate=48000)
        a = audio.detach().numpy() if hasattr(audio, "detach") else np.asarray(audio)
        wav = f"{OUT}/{sp}/{key}.wav"
        with wave.open(wav, "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(48000)
            w.writeframes((np.clip(a, -1, 1) * 32767).astype(np.int16).tobytes())
        subprocess.run(["afconvert", "-f", "m4af", "-d", "aac", "-b", "64000", wav, f"{OUT}/{sp}/{key}.m4a"], check=True)
        os.unlink(wav)
    print("готово:", text)

random.seed(11)
rows, answers = [], []
for i, (key, text) in enumerate(WORDS.items(), 1):
    order = list(sources)
    random.shuffle(order)
    answers.append(f"{i:2d}. {text:12s} ({key}): " + ", ".join(f"{'ABCD'[n]}={src}" for n, src in enumerate(order)))
    buttons = "".join(f'<span class="p"><b>{"ABCD"[n]}</b><button data-src="{src}/{key}.m4a">▶</button></span>' for n, src in enumerate(order))
    rows.append(f'<div class="row"><div class="w">{i}. <span>{text}</span></div>{buttons}</div>')
html = ("<!doctype html><meta charset=utf-8><title>Silero pilot</title><style>body{font:16px sans-serif;max-width:900px;margin:24px auto;padding:0 12px}"
        ".row{display:flex;align-items:center;gap:16px;padding:8px 0;border-bottom:1px solid #ddd}.w{width:190px;font-size:18px}.w span{font-weight:bold}"
        ".p{display:inline-flex;align-items:center;gap:5px}button{font-size:17px;padding:4px 12px;min-width:44px;cursor:pointer}</style>"
        "<h2>Silero против автора и TatarTTS: слепой тест</h2><p>У каждого слова записи A, B, C, D в случайном порядке. Ответы в <code>ANSWERS.txt</code>.</p>"
        + "".join(rows) + "<script>const p=new Audio();let cur=null;p.onended=()=>{if(cur)cur.textContent='▶';cur=null};"
        "document.querySelectorAll('button').forEach(b=>b.onclick=()=>{if(cur===b){p.pause();b.textContent='▶';cur=null;return}"
        "if(cur)cur.textContent='▶';p.src=b.dataset.src;p.currentTime=0;cur=b;b.textContent='■';p.play().catch(()=>{b.textContent='▶';cur=null})});</script>")
open(f"{OUT}/index.html", "w", encoding="utf-8").write(html)
open(f"{OUT}/ANSWERS.txt", "w", encoding="utf-8").write("\n".join(answers) + "\n")
print("готово:", OUT)
