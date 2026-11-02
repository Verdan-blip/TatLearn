#!/usr/bin/env python3
"""Сборка PNG маскота Мыраужана из исходников Nano Banana (Gemini): design/mascot/gemini/<поза>.jpg.

Запуск (нужны Pillow и numpy, лучше в отдельном окружении):
    python3 -m venv /tmp/mascot-venv && /tmp/mascot-venv/bin/pip install pillow numpy
    /tmp/mascot-venv/bin/python design/mascot/build_mascot_png.py
Результат: shared/src/commonMain/composeResources/drawable/mascot_<поза>.png (768x768, прозрачный фон).

Шаги для каждой позы:
  1. Выравнивание цвета: у старых поз шерсть ушла в более тёмный оранжевый, а внутренность ушей в бордовый;
     приводим к цвету новых поз (NORMALIZE = False у поз 6, 11, 12: они сгенерированы уже в целевом цвете).
  2. Вырезка: заливка фона от углов, белая стикерная обводка её останавливает (разрывы обводки замыкаются барьером).
  3. Обрезка по контуру кота с небольшим полем, квадрат, масштаб 768.
Орнамент и сама картинка: нужна проверка носителем (тюбетейка), права на сгенерированные картинки: нужна проверка юриста.
"""
import os

import numpy as np
from PIL import Image, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "gemini")
OUT = os.path.join(HERE, "..", "..", "shared", "src", "commonMain", "composeResources", "drawable")
SIZE = 768
MARGIN = 0.035  # поле вокруг кота, доля от стороны
TARGET = (231, 142, 45)  # медианный цвет шерсти новых поз и эталона

POSES = {  # поза: нужно ли выравнивать цвет
    "greeting": True, "timer": True, "progress": True, "sad": True, "searching": True, "cheer": False,
    "correct": True, "wrong": True, "tired": True, "teacher": True, "sleeping": False, "head-icon": False,
}


def fur_mask(a):
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    return (r > 150) & (g > 60) & (g < 190) & (b < 110) & (r - g > 35) & (r >= g) & (g >= b)


def normalize(im):
    """Шерсть к TARGET, красно-бордовая внутренность ушей к розовой."""
    rgb = np.asarray(im).astype(float)
    hsv = np.asarray(im.convert("HSV")).astype(float)
    m = fur_mask(rgb)
    sel = hsv[m]
    med = np.median(sel[sel[:, 2] > np.percentile(sel[:, 2], 40)], axis=0)
    th = np.asarray(Image.fromarray(np.uint8([[list(TARGET)]])).convert("HSV")).astype(float)[0, 0]
    dh, ss, vs = th[0] - med[0], th[1] / med[1], th[2] / med[2]
    out = hsv.copy()
    out[..., 0] = np.where(m, (out[..., 0] + dh) % 256, out[..., 0])
    out[..., 1] = np.where(m, np.clip(out[..., 1] * ss, 0, 255), out[..., 1])
    out[..., 2] = np.where(m, np.clip(out[..., 2] * vs, 0, 255), out[..., 2])
    h, s, v = hsv[..., 0] * 360 / 255, hsv[..., 1] / 255, hsv[..., 2] / 255
    ear = ((h > 335) | (h < 10)) & (s > 0.45) & (v > 0.28) & (v < 0.78)
    out[..., 0] = np.where(ear, 3 * 255 / 360, out[..., 0])
    out[..., 1] = np.where(ear, np.clip(s * 0.62, 0, 1) * 255, out[..., 1])
    out[..., 2] = np.where(ear, np.clip(v * 1.55, 0, 1) * 255, out[..., 2])
    return Image.fromarray(out.astype("uint8"), "HSV").convert("RGB")


def cut(im):
    """Прозрачный фон: заливка от углов, белая стикерная обводка (min(R,G,B) >= 244) останавливает заливку."""
    a = np.asarray(im).astype(int)
    ring = a.min(axis=2) >= 244
    barrier = np.asarray(Image.fromarray((ring * 255).astype("uint8")).filter(ImageFilter.MaxFilter(9))) > 0
    h, w = ring.shape
    outside = np.zeros((h, w), bool)
    stack = [(0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)]
    while stack:
        x, y = stack.pop()
        if x < 0 or y < 0 or x >= w or y >= h or outside[y, x] or barrier[y, x]:
            continue
        outside[y, x] = True
        stack += [(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)]
    grown = np.asarray(Image.fromarray((outside * 255).astype("uint8")).filter(ImageFilter.MaxFilter(9))) > 0
    outside = grown & ~ring | outside
    alpha = Image.fromarray(np.where(outside, 0, 255).astype("uint8")).filter(ImageFilter.MinFilter(3)).filter(ImageFilter.GaussianBlur(0.8))
    rgba = im.convert("RGBA")
    rgba.putalpha(alpha)
    return rgba


def square(rgba):
    """Обрезка по контуру кота с полем и вписывание в квадрат SIZE."""
    box = rgba.getchannel("A").point(lambda v: 255 if v > 8 else 0).getbbox()
    rgba = rgba.crop(box)
    side = int(max(rgba.size) * (1 + 2 * MARGIN))
    canvas = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    canvas.alpha_composite(rgba, ((side - rgba.width) // 2, (side - rgba.height) // 2))
    return canvas.resize((SIZE, SIZE), Image.LANCZOS)


if __name__ == "__main__":
    for pose, needs_norm in POSES.items():
        im = Image.open(os.path.join(SRC, pose + ".jpg")).convert("RGB")
        if needs_norm:
            im = normalize(im)
        out = os.path.join(OUT, "mascot_" + pose.replace("-", "_") + ".png")
        square(cut(im)).save(out, optimize=True)
        print(pose, os.path.getsize(out) // 1024, "КБ")
