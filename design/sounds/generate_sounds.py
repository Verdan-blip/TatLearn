#!/usr/bin/env python3
"""Синтез черновиков звуковых эффектов Тамыр (без внешних зависимостей).

Запуск: python3 design/sounds/generate_sounds.py
Результат: design/sounds/drafts/<событие>_<вариант>.wav и .m4a (m4a через afconvert, macOS).
Все звуки строятся кодом (синусы с обертонами и затуханием), чужих сэмплов нет.
"""
import math
import os
import struct
import subprocess
import wave

SR = 44100
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "drafts")
PEAK = 0.35  # тихо: эффекты не должны перебивать слова


def hz(name):
    """Частота ноты по имени, например 'E5'."""
    notes = {"C": 0, "D": 2, "E": 4, "F": 5, "G": 7, "A": 9, "B": 11}
    octave = int(name[-1])
    n = notes[name[0]] + (1 if "#" in name else 0)
    return 440.0 * 2 ** ((n - 9 + (octave - 4) * 12) / 12)


def tone(freq, dur, partials, decay, glide=0.0, attack=0.006, vib=0.0, vib_rate=6.0):
    """Одна нота: сумма обертонов (отношение, громкость) с экспоненциальным затуханием."""
    n = int(SR * dur)
    out = []
    phases = [0.0] * len(partials)
    for i in range(n):
        t = i / SR
        f = freq * (1 + glide * t / dur + vib * math.sin(2 * math.pi * vib_rate * t))
        env = math.exp(-t * decay) * min(1.0, t / attack)
        s = 0.0
        for k, (ratio, amp) in enumerate(partials):
            phases[k] += 2 * math.pi * f * ratio / SR
            # верхние обертоны затухают быстрее основного тона
            s += amp * math.sin(phases[k]) * math.exp(-t * decay * 0.5 * ratio)
        out.append(s * env)
    return out


def mix(parts, total):
    """Складывает ноты со сдвигами (в секундах) в один буфер."""
    buf = [0.0] * int(SR * total)
    for start, samples in parts:
        off = int(SR * start)
        for i, v in enumerate(samples):
            if off + i < len(buf):
                buf[off + i] += v
    return buf


def finish(buf):
    """Нормализация до PEAK и плавное затухание краёв (без щелчков)."""
    m = max(abs(v) for v in buf) or 1.0
    k = PEAK / m
    fade = int(SR * 0.02)
    for i in range(fade):
        buf[-1 - i] *= i / fade
    return [v * k for v in buf]


BELL = [(1, 1.0), (2.0, 0.35), (3.0, 0.15), (5.4, 0.08)]
MARIMBA = [(1, 1.0), (4.0, 0.3), (9.2, 0.05)]
SOFT = [(1, 1.0), (2, 0.12)]

SOUNDS = {}

# Правильно: только высокая нота, разные ноты и тембры
SOUNDS["correct_a"] = lambda: mix([(0, tone(hz("B5"), 0.6, BELL, 7))], 0.6)
SOUNDS["correct_b"] = lambda: mix([(0, tone(hz("E6"), 0.55, BELL, 8))], 0.55)
SOUNDS["correct_c"] = lambda: mix([(0, tone(hz("G6"), 0.5, BELL, 9))], 0.5)
SOUNDS["correct_d"] = lambda: mix([(0, tone(hz("B5"), 0.4, MARIMBA, 14))], 0.4)

# Неправильно: мягкий низкий, без диссонанса и резкой атаки (выбран вариант a)
SOUNDS["wrong_a"] = lambda: mix([(0, tone(hz("D4"), 0.35, SOFT, 9, glide=-0.12, attack=0.015))], 0.4)

# Урок пройден: новые идеи, короче (около 1 с)
CELESTA = [(1, 1.0), (3.0, 0.25), (6.0, 0.08)]
# a: музыкальная шкатулка, быстрый подъём и звон на последней ноте
SOUNDS["complete_a"] = lambda: mix(
    [(i * 0.07, tone(hz(n), 0.35, CELESTA, 10)) for i, n in enumerate(["G5", "B5", "D6"])]
    + [(0.21, tone(hz("G6"), 0.8, CELESTA, 5))], 1.0)
# b: «та-дам»: две ноты колокольчика поверх тёплой низкой
SOUNDS["complete_b"] = lambda: mix(
    [(0, tone(hz("C4"), 0.9, SOFT, 4)), (0, tone(hz("C6"), 0.5, BELL, 8)),
     (0.13, tone(hz("G6"), 0.8, BELL, 5))], 1.0)
# c: искристый бег по пентатонике вверх и долгая последняя нота
SOUNDS["complete_c"] = lambda: mix(
    [(i * 0.05, tone(hz(n), 0.3, CELESTA, 12)) for i, n in enumerate(["C5", "D5", "E5", "G5", "A5", "C6"])]
    + [(0.3, tone(hz("E6"), 0.8, BELL, 5))], 1.1)
# d: грубое приближение к кубызу: богатый обертонами «дребезжащий» звук с вибрато, затем колокольчик
TWANG = [(k, 1.0 / k) for k in range(1, 9)]
SOUNDS["complete_d"] = lambda: mix(
    [(0, tone(hz("D4"), 0.45, TWANG, 5, vib=0.02, vib_rate=14)),
     (0.22, tone(hz("A4"), 0.4, TWANG, 6, vib=0.02, vib_rate=14)),
     (0.45, tone(hz("D6"), 0.7, BELL, 6))], 1.1)


def write_wav(path, buf):
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, v)) * 32767)) for v in buf))


def report(name, buf):
    rms = math.sqrt(sum(v * v for v in buf) / len(buf))
    print(f"{name:12s} {len(buf) / SR:.2f} с  пик {max(abs(v) for v in buf):.2f}  rms {rms:.3f}  "
          f"края {buf[0]:.3f}/{buf[-1]:.3f}")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for name, make in SOUNDS.items():
        buf = finish(make())
        wav = os.path.join(OUT, name + ".wav")
        write_wav(wav, buf)
        subprocess.run(["afconvert", "-f", "m4af", "-d", "aac", "-b", "64000", wav,
                        os.path.join(OUT, name + ".m4a")], check=True)
        report(name, buf)
