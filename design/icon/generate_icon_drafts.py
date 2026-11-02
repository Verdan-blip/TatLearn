#!/usr/bin/env python3
"""Черновики иконки приложения Тамыр: 6 концепций дизайнера, SVG 1024x1024 и сравнительная доска.

Запуск из корня проекта: python3 design/icon/generate_icon_drafts.py [префикс имени ...]  (например, icon_r: только серия «корень»)
Результат: design/icon/drafts/icon_*.svg и board.png (нужен Google Chrome, путь задаёт CHROME).
Каждая иконка рисуется на весь квадрат без скруглений (как требует iOS); знак держится в центральных ~66%
(безопасная зона Android adaptive icon). Палитра из design/mascot-prompt.md. Голова Мыраужана берётся из
design/mascot/head-icon.svg. Орнамент тюбетейки не придумываем: только простые ромбы (needs native-speaker review).
"""
import os
import re
import subprocess

ROOT = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(ROOT, "drafts")
CHROME = os.environ.get("CHROME", "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")

CREAM = "#F3EEDF"
CREAM_DARK = "#EAE3CB"
GREEN = "#3B6B45"
GREEN_DARK = "#2C5234"
TERRA = "#B5522F"
GOLD = "#C99A34"

HEAD = re.search(r"<svg[^>]*>(.*)</svg>", open(os.path.join(ROOT, "..", "mascot", "head-icon.svg")).read(), re.S).group(1)


def svg(body, defs=""):
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="1024" height="1024"><defs>{defs}</defs>{body}</svg>'


def diamond(cx, cy, r, fill):
    return f'<path d="M{cx} {cy - r} L{cx + r} {cy} L{cx} {cy + r} L{cx - r} {cy} Z" fill="{fill}"/>'


def head(cx, cy, size):
    """Голова Мыраужана, вписанная в квадрат size с центром по виду головы в (cx, cy)."""
    return f'<svg x="{cx - size / 2}" y="{cy - size / 2 - size * 0.0025}" width="{size}" height="{size}" viewBox="0 0 400 400">{HEAD}</svg>'


def bg(color):
    return f'<rect width="1024" height="1024" fill="{color}"/>'


ICONS = {}

# 1. Голова в кремовом ромбе на терракоте.
ICONS["icon_1_head_in_diamond"] = svg(bg(TERRA) + diamond(512, 512, 335, CREAM) + head(512, 520, 470))

# 1б. Голова крупно на тёмно-зелёном (терракота и коричневая шерсть сливаются, поэтому фон зелёный).
ICONS["icon_1b_head_on_green"] = svg(bg(GREEN_DARK) + head(512, 520, 700))

# 2. Тройка ромбов на кремовом, и её вариант на зелёном.
def three_diamonds(back, colors):
    r = 132
    items = [(512, 350 + 41), (372, 592 + 41), (652, 592 + 41)]
    return svg(bg(back) + "".join(diamond(x, y, r, c) for (x, y), c in zip(items, colors)))


ICONS["icon_2_three_diamonds"] = three_diamonds(CREAM, [GREEN, GOLD, TERRA])
ICONS["icon_2b_three_diamonds_green"] = three_diamonds(GREEN, [CREAM, GOLD, TERRA])

# 3. Корень «тамыр»: зелёный ромб, золотое семя и толстые корешки.
roots = "".join(
    f'<path d="{d}" fill="none" stroke="{GREEN}" stroke-width="52" stroke-linecap="round"/>'
    for d in ["M512 640 V850", "M512 640 Q512 740 380 820", "M512 640 Q512 740 644 820"]
)
ICONS["icon_3_root"] = svg(bg(CREAM) + roots + diamond(512, 430, 225, GREEN) + diamond(512, 430, 75, GOLD))

# 4. Кот выглядывает из ромба-окна: нижняя часть головы обрезана краями ромба.
clip = '<clipPath id="w"><path d="M512 172 L852 512 L512 852 L172 512 Z M0 0 H1024 V460 H0 Z" clip-rule="nonzero"/></clipPath>'
ICONS["icon_4_peek"] = svg(
    bg(GREEN) + diamond(512, 512, 340, CREAM) + f'<g clip-path="url(#w)">{head(512, 640, 560)}</g>', clip,
)

# 5. Буква «Ә» (схема: перевёрнутая «е») в терракотовом ромбе с золотым акцентом.
schwa = f'<path d="M682 540 H342 A170 170 0 1 0 392 420" fill="none" stroke="{CREAM}" stroke-width="74" stroke-linecap="round" stroke-linejoin="round"/>'
ICONS["icon_5_letter_a"] = svg(bg(GREEN) + diamond(512, 512, 340, TERRA) + schwa + diamond(660, 340, 38, GOLD))

# 6. Ромб из четырёх ромбов в цветах бренда, в центре золотой ромб.
four = "".join(diamond(x, y, 150, c) for (x, y), c in [((512, 300), TERRA), ((724, 512), GREEN), ((512, 724), TERRA), ((300, 512), GREEN)])
ICONS["icon_6_four_diamonds"] = svg(bg(CREAM) + f'<g transform="translate(512 512) scale(0.88) translate(-512 -512)">{four}</g>' + diamond(512, 512, 62, GOLD))


# --- Серия «корень» (название «Тамыр» значит «корень»): толстые корни, чтобы читались на 48 px ---
def roots(x, y, color, w=54, scale=1.0):
    """Корневая система из трёх ветвей, растущая вниз от точки (x, y)."""
    paths = ["M0 0 V300", "M0 130 Q0 210 -140 290", "M0 130 Q0 210 140 290"]
    inner = "".join(f'<path d="{d}" fill="none" stroke="{color}" stroke-width="{w}" stroke-linecap="round"/>' for d in paths)
    return f'<g transform="translate({x} {y}) scale({scale})">{inner}</g>'


# R1. Ромб-семя с корнями на зелёном.
ICONS["icon_r1_seed_roots_green"] = svg(
    bg(GREEN) + roots(512, 590, CREAM, 56, 0.85) + diamond(512, 400, 215, CREAM) + diamond(512, 400, 72, GOLD)
)

# R2. То же на терракоте: ромб кремовый, семя золотое, корни кремовые.
ICONS["icon_r2_seed_roots_terra"] = svg(
    bg(TERRA) + roots(512, 590, CREAM, 56, 0.85) + diamond(512, 400, 215, CREAM) + diamond(512, 400, 72, GREEN)
)

# R3. Буква «Т» (Тамыр), ножка которой уходит корнями.
t_bar = f'<rect x="302" y="240" width="420" height="96" rx="40" fill="{CREAM}"/>'
t_stem = f'<path d="M512 300 V470" fill="none" stroke="{CREAM}" stroke-width="96" stroke-linecap="round"/>'
ICONS["icon_r3_letter_t_roots"] = svg(
    bg(GREEN) + t_bar + t_stem + roots(512, 470, CREAM, 62, 0.95) + diamond(512, 170, 46, GOLD)
)

# R4. Росток над землёй и корни под землёй: верх кремовый, низ терракотовый.
leaf = lambda cx, cy, rot: f'<ellipse cx="{cx}" cy="{cy}" rx="105" ry="48" transform="rotate({rot} {cx} {cy})" fill="{GREEN}"/>'
ICONS["icon_r4_sprout_and_roots"] = svg(
    bg(CREAM) + f'<rect y="500" width="1024" height="524" fill="{TERRA}"/>'
    + f'<path d="M512 500 V330" stroke="{GREEN}" stroke-width="40" stroke-linecap="round"/>'
    + leaf(420, 330, -28) + leaf(604, 330, 28) + roots(512, 500, CREAM, 52, 0.78) + diamond(512, 230, 40, GOLD)
)

# R5. Мыраужан над корнями: маскот и смысл названия вместе, на тёмно-зелёном.
ICONS["icon_r5_head_and_roots"] = svg(bg(GREEN_DARK) + roots(512, 600, CREAM_DARK, 52, 0.7) + head(512, 400, 520))

# R6. Корни как орнамент внутри бренд-ромба: кремовый ромб, зелёные корни, золотое семя.
ICONS["icon_r6_roots_in_diamond"] = svg(
    bg(TERRA) + diamond(512, 512, 335, CREAM) + roots(512, 340, GREEN, 46, 0.62) + diamond(512, 335, 44, GOLD)
)


# --- Серия «лицо-персонаж» по принципу сильных иконок-маскотов: одно лицо на весь квадрат, огромные глаза с бликами,
# 2-3 цвета, простые формы. Это новое упрощённое лицо Мыраужана, а не head-icon.svg. ---
def face(fur, fur_light, bg_color, band=GOLD):
    ears = (
        f'<path d="M236 470 L226 262 Q226 232 254 246 L430 360 Z" fill="{fur}"/>'
        f'<path d="M788 470 L798 262 Q798 232 770 246 L594 360 Z" fill="{fur}"/>'
        f'<path d="M262 420 L254 300 L384 372 Z" fill="{fur_light}"/>'
        f'<path d="M762 420 L770 300 L640 372 Z" fill="{fur_light}"/>'
    )
    head_shape = f'<ellipse cx="512" cy="580" rx="320" ry="260" fill="{fur}"/>'
    hat = (
        f'<path d="M290 400 Q290 215 512 205 Q734 215 734 400 Z" fill="{GREEN}"/>'
        f'<rect x="282" y="372" width="460" height="44" rx="18" fill="{band}"/>'
        f'<path d="M512 228 L546 262 L512 296 L478 262 Z" fill="{GOLD}"/>'
    )
    eyes = "".join(
        f'<circle cx="{x}" cy="545" r="84" fill="#FFFCF3"/><circle cx="{px}" cy="556" r="46" fill="#3D3A2D"/>'
        f'<circle cx="{px + 16}" cy="538" r="15" fill="#FFFCF3"/>'
        for x, px in [(390, 404), (634, 620)]
    )
    muzzle = (
        '<ellipse cx="462" cy="690" rx="66" ry="52" fill="#F0DDB8"/><ellipse cx="562" cy="690" rx="66" ry="52" fill="#F0DDB8"/>'
        f'<path d="M512 612 L542 642 L512 672 L482 642 Z" fill="{TERRA}"/>'
        '<path d="M470 704 Q491 730 512 704 Q533 730 554 704" fill="none" stroke="#3D3A2D" stroke-width="9" stroke-linecap="round" stroke-linejoin="round"/>'
    )
    cheeks = f'<ellipse cx="318" cy="652" rx="40" ry="24" fill="{TERRA}" fill-opacity="0.35"/><ellipse cx="706" cy="652" rx="40" ry="24" fill="{TERRA}" fill-opacity="0.35"/>'
    body = ears + head_shape + hat + eyes + muzzle + cheeks
    return svg(bg(bg_color) + f'<g transform="translate(512 512) scale(0.92) translate(-512 -512)">{body}</g>')


ICONS["icon_d1_face_green"] = face("#A8693A", "#C98B55", GREEN_DARK)
ICONS["icon_d2_face_deep_terra"] = face("#D79A55", "#E7B77B", "#8A4326")
ICONS["icon_d3_face_gold"] = face("#7A4A28", "#9A6438", GOLD, band=CREAM)
ICONS["icon_d4_face_terra"] = face("#E3A862", "#F0C98E", TERRA)


# --- Серия «эталон»: перерисовка головы с эталонной сгенерированной картинки (рыжий кот, тонкий контур, закрытые глаза) ---
REF = {
    "bg": "#FBF1D6", "fur": "#E18B35", "cream": "#FDE9BF", "hat": "#135246", "gold": "#F4B81D", "band": "#EDB126",
    "ear": "#F1736C", "stripe": "#B6551F", "cheek": "#E9735B", "nose": "#640D0E", "line": "#87331A", "eye": "#5A1E0F",
}


def ginger_face(bg_color, sticker=False, tilt=0, behind="", scale=1.0, dy=0):
    c = REF
    sw = 11  # толщина контура: на 48 px должна оставаться видимой
    ear_l = "M246 430 Q214 300 246 236 Q262 218 286 232 L440 330 Z"
    ear_r = "M778 430 Q810 300 778 236 Q762 218 738 232 L584 330 Z"
    hat = "M272 400 Q268 205 512 188 Q756 205 752 400 Q512 360 272 400 Z"
    head = '<ellipse cx="512" cy="560" rx="310" ry="260"'
    silhouette = ""
    if sticker:
        # Белая «стикерная» обводка вокруг всего силуэта, как на эталоне.
        silhouette = (
            f'<g fill="#FFFFFF" stroke="#FFFFFF" stroke-width="48" stroke-linejoin="round">'
            f'<path d="{ear_l}"/><path d="{ear_r}"/>{head}/><path d="{hat}"/></g>'
        )
    line = f'stroke="{c["line"]}" stroke-width="{sw}" stroke-linejoin="round"'
    ears = (
        f'<path d="{ear_l}" fill="{c["fur"]}" {line}/><path d="{ear_r}" fill="{c["fur"]}" {line}/>'
        f'<path d="M268 392 Q248 300 262 262 L368 330 Z" fill="{c["ear"]}"/>'
        f'<path d="M756 392 Q776 300 762 262 L656 330 Z" fill="{c["ear"]}"/>'
    )
    face = (
        f'{head} fill="{c["fur"]}" {line}/>'
        f'<ellipse cx="512" cy="652" rx="200" ry="110" fill="{c["cream"]}"/>'
        + "".join(f'<path d="M{x} {y1} V{y2}" stroke="{c["stripe"]}" stroke-width="13" stroke-linecap="round"/>' for x, y1, y2 in [(484, 426, 452), (512, 420, 458), (540, 426, 452)])
    )
    hat_svg = (
        f'<path d="{hat}" fill="{c["hat"]}" {line}/>'
        f'<path d="M280 390 Q512 344 744 390" fill="none" stroke="{c["band"]}" stroke-width="38" stroke-linecap="round"/>'
        f'<path d="M512 222 L552 262 L512 302 L472 262 Z" fill="{c["gold"]}" stroke="{c["line"]}" stroke-width="6" stroke-linejoin="round"/>'
    )
    eyes = "".join(
        f'<path d="{d}" fill="none" stroke="{c["eye"]}" stroke-width="13" stroke-linecap="round"/>'
        for d in ["M390 548 Q420 506 454 548", "M570 548 Q604 506 634 548"]
    )
    nose_mouth = (
        f'<path d="M486 592 Q512 580 538 592 Q530 622 512 630 Q494 622 486 592 Z" fill="{c["nose"]}"/>'
        f'<path d="M512 630 V648 M512 648 Q488 680 456 654 M512 648 Q536 680 568 654" fill="none" stroke="{c["eye"]}" stroke-width="10" stroke-linecap="round"/>'
        f'<ellipse cx="356" cy="612" rx="32" ry="22" fill="{c["cheek"]}" fill-opacity="0.8"/><ellipse cx="668" cy="612" rx="32" ry="22" fill="{c["cheek"]}" fill-opacity="0.8"/>'
    )
    whiskers = "".join(
        f'<path d="{d}" stroke="{c["line"]}" stroke-width="7" stroke-linecap="round" fill="none"/>'
        for d in ["M336 628 L224 604", "M336 650 L220 654", "M688 628 L800 604", "M688 650 L804 654"]
    )
    body = silhouette + ears + face + hat_svg + eyes + nose_mouth + whiskers
    return svg(bg(bg_color) + behind + f'<g transform="translate(512 {512 + dy}) rotate({tilt}) scale({scale}) translate(-512 -512)">{body}</g>')


ICONS["icon_f1_cream"] = ginger_face(REF["bg"])
ICONS["icon_f2_terra"] = ginger_face(TERRA)
ICONS["icon_f3_terra_sticker"] = ginger_face(TERRA, sticker=True)
ICONS["icon_f4_sunny"] = ginger_face("#F2C14E")
ICONS["icon_f5_teal_sticker"] = ginger_face("#2E7D6E", sticker=True, tilt=-5)


# --- f5 на фонах из темы приложения (TamyrColors): основной зелёный, тёмный зелёный, терракота, золото, ромб-мотив ---
ICONS["icon_g1_theme_green"] = ginger_face(GREEN, sticker=True, tilt=-5)
ICONS["icon_g2_theme_green_dark"] = ginger_face(GREEN_DARK, sticker=True, tilt=-5)
ICONS["icon_g3_theme_terra"] = ginger_face(TERRA, sticker=True, tilt=-5)
ICONS["icon_g4_theme_gold"] = ginger_face(GOLD, sticker=True, tilt=-5)
ICONS["icon_g5_theme_green_diamond"] = ginger_face(GREEN, sticker=True, tilt=-5, behind=diamond(512, 512, 470, "#4A7B54"))
ICONS["icon_g6_theme_terra_diamond"] = ginger_face(TERRA, sticker=True, tilt=-5, behind=diamond(512, 512, 470, "#C4633F"))


# --- Корни за котом в стиле ромба-мотива: тот же приглушённый тон, толстые округлые линии ---
def root_motif(color, w=46, extra=""):
    paths = [
        "M512 560 V1010",
        "M512 800 Q360 820 250 900 Q180 950 140 1030",
        "M512 800 Q664 820 774 900 Q844 950 884 1030",
        "M300 700 Q150 660 110 520 Q90 440 110 360",
        "M724 700 Q874 660 914 520 Q934 440 914 360",
    ]
    return "".join(f'<path d="{d}" fill="none" stroke="{color}" stroke-width="{w}" stroke-linecap="round"/>' for d in paths) + extra


MOTIF = "#4A7B54"
ICONS["icon_h1_roots_and_diamond"] = ginger_face(GREEN, sticker=True, tilt=-5, behind=diamond(512, 512, 470, MOTIF) + root_motif(MOTIF))
ICONS["icon_h2_roots_only"] = ginger_face(GREEN, sticker=True, tilt=-5, behind=root_motif("#5A8A62", 52))
ICONS["icon_h3_roots_terra"] = ginger_face(TERRA, sticker=True, tilt=-5, behind=root_motif("#C4633F", 52))
# Кот меньше и выше: корни целиком под ним, внутри видимой зоны Android.
low_roots = (
    f'<path d="M512 700 V930" fill="none" stroke="#6C9C74" stroke-width="56" stroke-linecap="round"/>'
    f'<path d="M512 800 Q420 840 340 920" fill="none" stroke="#6C9C74" stroke-width="56" stroke-linecap="round"/>'
    f'<path d="M512 800 Q604 840 684 920" fill="none" stroke="#6C9C74" stroke-width="56" stroke-linecap="round"/>'
)
ICONS["icon_h4_cat_over_roots"] = ginger_face(GREEN, sticker=True, tilt=-5, behind=low_roots, scale=0.8, dy=-90)
ICONS["icon_h5_cat_over_roots_diamond"] = ginger_face(GREEN, sticker=True, tilt=-5, behind=diamond(512, 470, 330, MOTIF) + low_roots, scale=0.8, dy=-90)


# --- Дерево за котом в том же приглушённом тоне: крона из ромбов, ветви с ромбовыми листьями, круглая крона, ромб-дерево ---
def stroke_path(d, color, w):
    return f'<path d="{d}" fill="none" stroke="{color}" stroke-width="{w}" stroke-linecap="round" stroke-linejoin="round"/>'


crown_diamonds = "".join(diamond(x, y, 110, MOTIF) for x, y in [(512, 190), (350, 270), (674, 270), (250, 430), (774, 430), (300, 640), (724, 640)])
ICONS["icon_t1_crown_of_diamonds"] = ginger_face(GREEN, sticker=True, tilt=-5, behind=stroke_path("M512 600 V1024", MOTIF, 80) + crown_diamonds)

branches = (
    stroke_path("M512 1024 V740", MOTIF, 70)
    + stroke_path("M512 780 Q380 720 300 580 Q240 450 262 310", MOTIF, 40)
    + stroke_path("M512 780 Q644 720 724 580 Q784 450 762 310", MOTIF, 40)
    + stroke_path("M262 310 Q330 150 512 120 Q694 150 762 310", MOTIF, 40)
    + "".join(diamond(x, y, 52, MOTIF) for x, y in [(262, 290), (762, 290), (512, 110), (330, 170), (694, 170), (240, 480), (784, 480), (290, 640), (734, 640)])
)
ICONS["icon_t2_branches_and_leaves"] = ginger_face(GREEN, sticker=True, tilt=-5, behind=branches)

ICONS["icon_t3_round_crown"] = ginger_face(
    GREEN, sticker=True, tilt=-5,
    behind=f'<rect x="452" y="760" width="120" height="270" rx="30" fill="{MOTIF}"/><circle cx="512" cy="450" r="410" fill="{MOTIF}"/>',
)

ICONS["icon_t4_diamond_tree"] = ginger_face(GREEN, sticker=True, tilt=-5, behind=stroke_path("M512 760 V1024", MOTIF, 84) + diamond(512, 430, 400, MOTIF))


# --- Кот в кроне: кот меньше и выше, крона светлее фона, ствол терракотовый (читается как дерево) ---
trunk = f'<path d="M512 700 V1024" stroke="{TERRA}" stroke-width="96" stroke-linecap="round" fill="none"/><path d="M512 880 Q420 900 360 960 M512 880 Q604 900 664 960" stroke="{TERRA}" stroke-width="44" stroke-linecap="round" fill="none"/>'
CROWN = "#4A7B54"
ICONS["icon_u1_cat_in_round_crown"] = ginger_face(GREEN, sticker=True, tilt=-5, scale=0.82, dy=-60,
    behind=trunk + f'<circle cx="512" cy="440" r="400" fill="{CROWN}"/>')
ICONS["icon_u2_cat_in_diamond_crown"] = ginger_face(GREEN, sticker=True, tilt=-5, scale=0.82, dy=-60,
    behind=trunk + diamond(512, 430, 420, CROWN))
ICONS["icon_u3_cat_in_diamonds_crown"] = ginger_face(GREEN, sticker=True, tilt=-5, scale=0.82, dy=-60,
    behind=trunk + "".join(diamond(x, y, 130, CROWN) for x, y in [(512, 150), (340, 240), (684, 240), (230, 430), (794, 430), (290, 640), (734, 640)]))


# --- Кот на светлом круге (круг радиусом ~350 целиком помещается в видимую зону Android adaptive icon) ---
def disc(color, r=350):
    return f'<circle cx="512" cy="512" r="{r}" fill="{color}"/>'


ICONS["icon_c1_green_tonal_disc"] = ginger_face(GREEN, sticker=True, tilt=-5, scale=0.92, behind=disc("#4A7B54"))
ICONS["icon_c2_green_cream_disc"] = ginger_face(GREEN, tilt=-5, scale=0.92, behind=disc("#F3EEDF"))
ICONS["icon_c3_terra_cream_disc"] = ginger_face(TERRA, tilt=-5, scale=0.92, behind=disc("#F3EEDF"))
ICONS["icon_c4_dark_green_tonal_disc"] = ginger_face(GREEN_DARK, sticker=True, tilt=-5, scale=0.92, behind=disc(GREEN))
ICONS["icon_c5_green_warm_disc"] = ginger_face(GREEN, tilt=-5, scale=0.92, behind=disc("#EFE6C8"))
ICONS["icon_c6_green_big_tonal_disc"] = ginger_face(GREEN, sticker=True, tilt=-5, scale=1.0, behind=disc("#5A8A62", 420))


# --- c1 без стикерной обводки и с кругом покрупнее (на Android круг радиусом больше ~340 заполняет всю видимую зону) ---
ICONS["icon_k1_disc_400"] = ginger_face(GREEN, tilt=-5, scale=0.92, behind=disc("#4A7B54", 400))
ICONS["icon_k2_disc_440"] = ginger_face(GREEN, tilt=-5, scale=0.92, behind=disc("#4A7B54", 440))
ICONS["icon_k3_disc_400_lighter"] = ginger_face(GREEN, tilt=-5, scale=0.92, behind=disc("#5A8A62", 400))
ICONS["icon_k4_disc_440_bigger_cat"] = ginger_face(GREEN, tilt=-5, scale=1.0, behind=disc("#4A7B54", 440))


def board(names):
    """Доска: каждая иконка в формах iOS и Android и в размерах 180, 120, 48 и 29 на светлом и тёмном фоне."""
    rows = []
    for name in names:
        cells = []
        for theme in ("#F3EEDF", "#16181b"):
            cells.append(
                f'<div class="panel" style="background:{theme}">'
                f'<img class="ios" src="{name}.svg" width="180" height="180">'
                f'<div class="rw"><img src="{name}.svg" width="180" height="180" style="margin:-30px"></div>'
                f'<img class="ios" src="{name}.svg" width="48" height="48">'
                f'<img class="ios" src="{name}.svg" width="29" height="29"></div>'
            )
        rows.append(f'<div class="row"><div class="label">{name}</div>{"".join(cells)}</div>')
    css = (
        "body{margin:0;padding:20px;background:#fff;font:14px sans-serif}.row{margin-bottom:14px}"
        ".label{margin:0 0 4px 4px;color:#333}.panel{display:inline-flex;align-items:center;gap:20px;padding:16px;"
        "border-radius:12px;margin-right:12px}.ios{border-radius:22.4%}.rw{width:120px;height:120px;border-radius:50%;overflow:hidden}"
    )
    return f"<!doctype html><meta charset=utf-8><style>{css}</style>{''.join(rows)}"


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for name, content in ICONS.items():
        with open(os.path.join(OUT, name + ".svg"), "w") as f:
            f.write(content)
    # Аргументы запуска: префиксы имён для доски, например `icon_r` (по умолчанию все иконки).
    import sys
    names = [n for n in ICONS if not sys.argv[1:] or any(n.startswith(a) for a in sys.argv[1:])]
    page = os.path.join(OUT, "board.html")
    with open(page, "w") as f:
        f.write(board(names))
    height = 40 + len(names) * 262
    subprocess.run(
        [CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars", "--allow-file-access-from-files",
         f"--window-size=1100,{height}", f"--screenshot={os.path.join(OUT, 'board.png')}", "file://" + page],
        check=True, capture_output=True,
    )
    print("готово:", OUT)
