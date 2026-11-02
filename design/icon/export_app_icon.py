#!/usr/bin/env python3
"""Экспорт финальной иконки Тамыр (вариант k4: кот на большом тональном круге) в проекты Android и iOS.

Запуск из корня проекта: python3 design/icon/export_app_icon.py
Источник один: функция `ginger_face` из generate_icon_drafts.py. Скрипт создаёт:
  - design/icon/app-icon.svg (мастер, 1024x1024);
  - Android adaptive: drawable/ic_launcher_background.xml (фон и круг), drawable-v24/ic_launcher_foreground.xml (кот),
    drawable/ic_launcher_monochrome.xml (силуэт для тематических иконок Android 13+), mipmap-anydpi-v26/*.xml;
  - Android для API 24-25: mipmap-*/ic_launcher.png и ic_launcher_round.png;
  - iOS: Assets.xcassets/AppIcon.appiconset/app-icon-1024.png (квадрат без прозрачности).
Нужны Google Chrome (CHROME) и sips (macOS).
"""
import os
import re
import subprocess
import tempfile
import xml.etree.ElementTree as ET

from generate_icon_drafts import CHROME, GREEN, REF, disc, ginger_face

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
RES = os.path.join(ROOT, "androidApp", "src", "main", "res")
IOS_SET = os.path.join(ROOT, "iosApp", "iosApp", "Assets.xcassets", "AppIcon.appiconset")
MASTER = os.path.join(ROOT, "design", "icon", "app-icon.svg")
NS = "{http://www.w3.org/2000/svg}"
HEADER = '<?xml version="1.0" encoding="utf-8"?>\n<!-- Сгенерировано design/icon/export_app_icon.py, правьте скрипт, а не этот файл. -->\n'


def master_svg():
    return ginger_face(GREEN, tilt=-5, scale=1.0, behind=disc("#4A7B54", 440))


def ellipse_d(cx, cy, rx, ry):
    return f"M{cx - rx},{cy} a{rx},{ry} 0 1,0 {2 * rx},0 a{rx},{ry} 0 1,0 {-2 * rx},0 Z"


def shape_d(el):
    """Строка pathData для path, ellipse или circle."""
    tag = el.tag.replace(NS, "")
    if tag == "path":
        return el.get("d")
    if tag == "ellipse":
        return ellipse_d(float(el.get("cx")), float(el.get("cy")), float(el.get("rx")), float(el.get("ry")))
    if tag == "circle":
        r = float(el.get("r"))
        return ellipse_d(float(el.get("cx")), float(el.get("cy")), r, r)
    raise ValueError(tag)


def vd_path(el):
    """Элемент SVG в <path> векторного рисунка Android."""
    fill, stroke = el.get("fill"), el.get("stroke")
    attrs = [f'android:pathData="{shape_d(el)}"']
    if fill and fill != "none":
        attrs.append(f'android:fillColor="{fill}"')
        if el.get("fill-opacity"):
            attrs.append(f'android:fillAlpha="{el.get("fill-opacity")}"')
    if stroke:
        attrs.append(f'android:strokeColor="{stroke}"')
        attrs.append(f'android:strokeWidth="{el.get("stroke-width")}"')
        if el.get("stroke-linecap"):
            attrs.append(f'android:strokeLineCap="{el.get("stroke-linecap")}"')
        if el.get("stroke-linejoin"):
            attrs.append(f'android:strokeLineJoin="{el.get("stroke-linejoin")}"')
    return "<path " + " ".join(attrs) + "/>"


def vector(body):
    return (
        HEADER
        + '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n    android:width="108dp"\n    android:height="108dp"\n'
        + '    android:viewportWidth="1024"\n    android:viewportHeight="1024">\n'
        + body + "\n</vector>\n"
    )


def group_open(g):
    """Группа кота: поворот и масштаб вокруг центра (из transform вида translate rotate scale translate)."""
    m = re.search(r"rotate\((-?[\d.]+)\) scale\(([\d.]+)\)", g.get("transform"))
    return f'<group android:pivotX="512" android:pivotY="512" android:rotation="{m.group(1)}" android:scaleX="{m.group(2)}" android:scaleY="{m.group(2)}">'


def build_android():
    root = ET.fromstring(master_svg())
    children = [c for c in root if c.tag != NS + "defs"]
    background = [c for c in children if c.tag != NS + "g"]
    cat = next(c for c in children if c.tag == NS + "g")
    bg_body = f'<path android:pathData="M0,0h1024v1024h-1024z" android:fillColor="{background[0].get("fill")}"/>\n' + "\n".join(vd_path(c) for c in background[1:])
    fg_body = group_open(cat) + "\n" + "\n".join(vd_path(c) for c in cat) + "\n</group>"

    # Монохромный силуэт: оттенки задаёт прозрачность, «дырки» (evenOdd) отделяют мордочку, внутреннюю часть ушей и ромб.
    by_fill = {}
    for el in cat:
        by_fill.setdefault(el.get("fill"), []).append(el)
    fur, ear, cream = by_fill[REF["fur"]], by_fill[REF["ear"]], by_fill[REF["cream"]]
    ears_fur = [e for e in fur if e.tag == NS + "path"]
    head = [e for e in fur if e.tag == NS + "ellipse"]
    hat, gold, nose = by_fill[REF["hat"]], by_fill[REF["gold"]], by_fill[REF["nose"]]
    face_lines = [e for e in cat if e.get("stroke") == REF["eye"]]
    white = 'android:fillColor="#FFFFFF" android:fillType="evenOdd"'
    mono = [
        f'<path {white} android:fillAlpha="0.55" android:pathData="{" ".join(shape_d(e) for e in ears_fur + ear)}"/>',
        f'<path {white} android:fillAlpha="0.55" android:pathData="{" ".join(shape_d(e) for e in head + cream)}"/>',
        f'<path {white} android:pathData="{" ".join(shape_d(e) for e in hat + gold)}"/>',
        f'<path android:fillColor="#FFFFFF" android:pathData="{shape_d(nose[0])}"/>',
    ] + [
        f'<path android:strokeColor="#FFFFFF" android:strokeWidth="{e.get("stroke-width")}" android:strokeLineCap="round" android:pathData="{shape_d(e)}"/>'
        for e in face_lines
    ]
    mono_body = group_open(cat) + "\n" + "\n".join(mono) + "\n</group>"

    def write(path, text):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w") as f:
            f.write(text)

    write(os.path.join(RES, "drawable", "ic_launcher_background.xml"), vector(bg_body))
    write(os.path.join(RES, "drawable-v24", "ic_launcher_foreground.xml"), vector(fg_body))
    write(os.path.join(RES, "drawable", "ic_launcher_monochrome.xml"), vector(mono_body))
    adaptive = (
        HEADER + '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@drawable/ic_launcher_background" />\n'
        '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
        '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n</adaptive-icon>\n'
    )
    for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
        write(os.path.join(RES, "mipmap-anydpi-v26", name), adaptive)


def chrome_png(size, out, radius="0", transparent=False):
    html = (
        f'<!doctype html><body style="margin:0;background:transparent"><div style="width:{size}px;height:{size}px;'
        f'border-radius:{radius};overflow:hidden"><img src="file://{MASTER}" width="{size}" height="{size}" style="display:block"></div>'
    )
    with tempfile.NamedTemporaryFile("w", suffix=".html", delete=False) as f:
        f.write(html)
    cmd = [CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--allow-file-access-from-files",
           f"--window-size={size},{size}", f"--screenshot={out}", "file://" + f.name]
    if transparent:
        cmd.insert(2, "--default-background-color=00000000")
    subprocess.run(cmd, check=True, capture_output=True)
    os.unlink(f.name)


def build_png():
    # API 24-25 (adaptive icon там не работает): квадрат со скруглением и круг, прозрачные углы.
    for folder, px in {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}.items():
        out = os.path.join(RES, f"mipmap-{folder}")
        chrome_png(px, os.path.join(out, "ic_launcher.png"), radius="22%", transparent=True)
        chrome_png(px, os.path.join(out, "ic_launcher_round.png"), radius="50%", transparent=True)
    # iOS: квадрат 1024 без скруглений; убираем альфа-канал (App Store его не принимает) через BMP и обратно.
    tmp = os.path.join(tempfile.mkdtemp(), "icon.png")
    chrome_png(1024, tmp)
    bmp = tmp.replace(".png", ".bmp")
    subprocess.run(["sips", "-s", "format", "bmp", tmp, "--out", bmp], check=True, capture_output=True)
    subprocess.run(["sips", "-s", "format", "png", bmp, "--out", os.path.join(IOS_SET, "app-icon-1024.png")], check=True, capture_output=True)


if __name__ == "__main__":
    with open(MASTER, "w") as f:
        f.write(master_svg())
    build_android()
    build_png()
    print("готово")
