"""Лёгкие утилиты звука (только numpy): чтение/запись wav и обработка громкости под записи автора."""
import struct
import wave

import numpy as np

TARGET_RMS_DB, PEAK_LIMIT_DB = -16.3, -1.0  # как у записей автора: RMS активной части и потолок пиков


def write_wav(path, audio, rate=22050):
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(rate)
        w.writeframes(audio.tobytes())


def read_wav(path):
    """Читает 16-бит моно wav (в том числе WAVE_FORMAT_EXTENSIBLE от afconvert, который стандартный wave не понимает)."""
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


def trim_silence(a, sr, threshold=0.02, pad_s=0.06):
    """Подрезает тишину по краям, оставляя небольшой запас."""
    idx = np.where(np.abs(a) > threshold)[0]
    if len(idx) == 0:
        return a
    pad = int(pad_s * sr)
    return a[max(0, idx[0] - pad): min(len(a), idx[-1] + pad)]


def master(a, sr, upsample=1):
    """Громкость как у записей автора, мягкие края; upsample=2 удваивает частоту (22,05 -> 44,1 кГц)."""
    act = a[np.abs(a) > 0.02]
    gain = 10 ** ((TARGET_RMS_DB - 20 * np.log10(np.sqrt((act ** 2).mean()) + 1e-9)) / 20)
    peak = np.abs(a).max() * gain
    if peak > 10 ** (PEAK_LIMIT_DB / 20):
        gain *= 10 ** (PEAK_LIMIT_DB / 20) / peak
    a = a * gain
    fi, fo = int(0.003 * sr), int(0.012 * sr)
    a[:fi] *= np.linspace(0, 1, fi)
    a[-fo:] *= np.linspace(1, 0, fo)
    if upsample > 1:
        t = np.arange(len(a) * upsample) / upsample
        a = np.interp(t, np.arange(len(a)), a)
    return (np.clip(a, -1, 1) * 32767).astype(np.int16)
