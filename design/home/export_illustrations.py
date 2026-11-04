#!/usr/bin/env python3
"""Экспорт иллюстраций модулей из design/home/illustrations.py в PNG для приложения.

Запуск из корня проекта (нужны только Python 3 и Google Chrome, путь можно задать переменной CHROME):
    python3 design/home/export_illustrations.py
Результат: shared/src/commonMain/composeResources/drawable/module_<образ>.png (открытый модуль) и
module_<образ>_locked.png (закрытый, приглушённая палитра), 360x315, прозрачный фон.
Почему PNG: Compose Multiplatform не читает SVG из ресурсов на Android. Образы (дом с наличниками, калитка,
самовар, эчпочмаки): needs native-speaker review.
"""
import os
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from illustrations import ILLUS, illus  # noqa: E402

CHROME = os.environ.get("CHROME", "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
OUT = os.path.join(HERE, "..", "..", "shared", "src", "commonMain", "composeResources", "drawable")
WIDTH, HEIGHT = 360, 315


def render(kind, locked, target):
    html = (f'<!doctype html><meta charset="utf-8"><style>html,body{{margin:0;background:transparent}}</style>'
            f'<body>{illus(kind, WIDTH, HEIGHT, locked=locked)}</body>')
    with tempfile.NamedTemporaryFile("w", suffix=".html", delete=False, encoding="utf-8") as f:
        f.write(html)
        path = f.name
    try:
        subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--default-background-color=00000000",
                        f"--window-size={WIDTH},{HEIGHT}", f"--screenshot={target}", f"file://{path}"],
                       check=True, capture_output=True)
    finally:
        os.unlink(path)


def main():
    for kind in ILLUS:
        for locked in (False, True):
            name = f"module_{kind}{'_locked' if locked else ''}.png"
            target = os.path.abspath(os.path.join(OUT, name))
            render(kind, locked, target)
            print("OK" if os.path.exists(target) else "НЕТ", name, os.path.getsize(target) if os.path.exists(target) else 0)


if __name__ == "__main__":
    main()
