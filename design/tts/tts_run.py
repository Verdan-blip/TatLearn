"""Мини-запуск моделей TatarTTS (Piper ONNX) без пакета piper: espeak-ng (голос tt) -> id фонем -> onnxruntime -> wav.

Зачем свой запуск: пакет piper-tts на macOS падает при загрузке espeak (путь к данным зашит при сборке),
а сам espeak теряет путь к данным длиннее ~230 символов, поэтому данные копируются по короткому пути.
Модели (male, female: .onnx и config.json) лежат в $TATARTTS_MODELS (по умолчанию ~/.cache/tatartts), см. README.md.
Результат синтеза каждый раз немного другой (случайный шум модели), поэтому лучший вариант выбирают на слух.
"""
import json
import os
import shutil
import unicodedata
import wave
from pathlib import Path

import espeakng_loader
import numpy as np
import onnxruntime as ort

MODELS = Path(os.environ.get("TATARTTS_MODELS", "~/.cache/tatartts")).expanduser()
ESPEAK_SHORT = "/tmp/tatartts-espeak"  # короткий путь к данным espeak (длинный espeak игнорирует)

if not os.path.isdir(f"{ESPEAK_SHORT}/espeak-ng-data"):
    shutil.copytree(espeakng_loader.get_data_path(), f"{ESPEAK_SHORT}/espeak-ng-data")
import ctypes  # noqa: E402

_lib = ctypes.CDLL(espeakng_loader.get_library_path())
assert _lib.espeak_Initialize(2, 0, ESPEAK_SHORT.encode(), 0) > 0, "espeak не инициализировался"
_lib.espeak_SetVoiceByName.restype = ctypes.c_int
assert _lib.espeak_SetVoiceByName(b"tt") == 0, "нет голоса tt"
_lib.espeak_TextToPhonemes.restype = ctypes.c_char_p


def espeak_ipa(text):
    """IPA-фонемы espeak-ng для текста (по клаузам)."""
    buf = ctypes.c_char_p(text.encode("utf-8"))
    ptr = ctypes.cast(ctypes.pointer(buf), ctypes.POINTER(ctypes.c_void_p))
    clauses = []
    while ptr[0]:
        ph = _lib.espeak_TextToPhonemes(ptr, 1, 0x02)  # 1 = UTF-8, 0x02 = IPA
        if ph:
            clauses.append(ph.decode("utf-8"))
    return " ".join(clauses)


class Voice:
    def __init__(self, name="male"):
        d = MODELS / name
        self.cfg = json.load(open(d / "config.json", encoding="utf-8"))
        self.ids = self.cfg["phoneme_id_map"]
        self.sess = ort.InferenceSession(str(d / f"{name}.onnx"))
        self.rate = self.cfg["audio"]["sample_rate"]
        inf = self.cfg["inference"]
        self.scales = np.array([inf["noise_scale"], inf["length_scale"], inf["noise_w"]], dtype=np.float32)

    def synth(self, text, ipa=None, noise_scale=None, length_scale=None, noise_w=None):
        """Возвращает (фонемы, пропущенные символы, int16-аудио). Если задан ipa, он заменяет фонемы espeak."""
        ph = unicodedata.normalize("NFD", ipa if ipa is not None else espeak_ipa(text))
        ids = list(self.ids["^"]) + list(self.ids["_"])
        missing = []
        for p in ph:
            if p in self.ids:
                ids += self.ids[p] + self.ids["_"]
            else:
                missing.append(p)
        ids += self.ids["$"]
        scales = self.scales.copy()
        for i, v in enumerate((noise_scale, length_scale, noise_w)):
            if v is not None:
                scales[i] = v
        out = self.sess.run(None, {
            "input": np.array([ids], dtype=np.int64),
            "input_lengths": np.array([len(ids)], dtype=np.int64),
            "scales": scales,
        })[0].squeeze()
        audio = out * (32767 / max(0.01, float(np.abs(out).max())))
        return ph, "".join(missing), audio.astype(np.int16)


def write_wav(path, audio, rate=22050):
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(rate)
        w.writeframes(audio.tobytes())


def read_wav(path):
    """Читает 16-бит моно wav (в том числе WAVE_FORMAT_EXTENSIBLE от afconvert, который стандартный wave не понимает)."""
    import struct
    b = open(path, "rb").read()
    pos, sr = 12, None
    while pos < len(b):
        cid, size = b[pos:pos + 4], struct.unpack("<I", b[pos + 4:pos + 8])[0]
        if cid == b"fmt ":
            sr = struct.unpack("<I", b[pos + 12:pos + 16])[0]
        if cid == b"data":
            return sr, np.frombuffer(b[pos + 8:pos + 8 + size], dtype=np.int16).astype(float) / 32768
        pos += 8 + size + (size & 1)
    raise ValueError("нет data")
