# Векторные иллюстрации модулей Тамыр: только токены бренда, контур #3D3A2D, свет сверху-слева.
# Все татарские образы: needs native-speaker review.
# Нарисованы агентом ux-designer (04.11.2026); в приложение идут как PNG (SVG в ресурсах Compose на Android не поддерживается),
# экспорт: python3 design/home/export_illustrations.py
import math

ACTIVE = dict(K="#3D3A2D", G="#3B6B45", Gd="#2C5234", A="#C99A34", Ad="#7A6119",
              T="#B5522F", Td="#8A4326", C="#FFFCF3", C1="#F3EEDF", C2="#EFE6C8",
              C3="#EAE3CB", L="#D9CFB2", L2="#E5DEC6", sh=0.14, hl=0.5)
LOCKED = dict(K="#A39D85", G="#D9CFB2", Gd="#C9C0A4", A="#E5DEC6", Ad="#C9C0A4",
              T="#D9CFB2", Td="#C9C0A4", C="#FFFCF3", C1="#F3EEDF", C2="#F3EEDF",
              C3="#EAE3CB", L="#D9CFB2", L2="#E5DEC6", sh=0.06, hl=0.6)


def _svg(body, w, h, vb="0 0 160 140"):
    return (f'<svg width="{w}" height="{h}" viewBox="{vb}" xmlns="http://www.w3.org/2000/svg" '
            f'style="display:block;overflow:visible" stroke-linejoin="round" stroke-linecap="round">{body}</svg>')


def _ground(P, cx=80, cy=128, rx=62):
    return f'<ellipse cx="{cx}" cy="{cy}" rx="{rx}" ry="7" fill="{P["K"]}" opacity="{P["sh"]*0.9}"/>'


def _dia(cx, cy, r, fill, P, sw=1.2):
    return (f'<path d="M{cx} {cy-r} L{cx+r} {cy} L{cx} {cy+r} L{cx-r} {cy} Z" fill="{fill}" '
            f'stroke="{P["K"]}" stroke-width="{sw}"/>')


def _sun(cx, cy, r, P, inner=0.5):
    # полусолнце фронтона: золотой полукруг, лучи, терракотовая сердцевина
    s = f'<path d="M{cx-r} {cy} A{r} {r} 0 0 1 {cx+r} {cy} Z" fill="{P["A"]}" stroke="{P["K"]}" stroke-width="1.5"/>'
    for i in range(1, 8):
        a = math.pi * i / 8
        x1, y1 = cx - math.cos(a) * r * inner, cy - math.sin(a) * r * inner
        x2, y2 = cx - math.cos(a) * r * 0.92, cy - math.sin(a) * r * 0.92
        s += f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" stroke="{P["Ad"]}" stroke-width="1.4"/>'
    ri = r * inner
    s += f'<path d="M{cx-ri} {cy} A{ri} {ri} 0 0 1 {cx+ri} {cy} Z" fill="{P["T"]}" stroke="{P["K"]}" stroke-width="1.3"/>'
    return s


def house(P=ACTIVE, w=160, h=140):
    K = P["K"]
    b = _ground(P)
    # труба и дымок
    b += f'<path d="M108 30 q-6 -7 0 -12 q6 -5 0 -11" fill="none" stroke="{P["L"]}" stroke-width="3"/>'
    b += f'<rect x="102" y="30" width="12" height="24" fill="{P["T"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<rect x="99.5" y="26" width="17" height="6" rx="1.5" fill="{P["Td"]}" stroke="{K}" stroke-width="2"/>'
    # стены из бруса
    b += f'<rect x="30" y="64" width="100" height="58" fill="{P["C2"]}" stroke="{K}" stroke-width="2"/>'
    for y in range(74, 120, 8):
        b += f'<line x1="31" y1="{y}" x2="129" y2="{y}" stroke="{P["Ad"]}" stroke-width="1" opacity=".28"/>'
    b += f'<rect x="108" y="65" width="21" height="56" fill="{K}" opacity="{P["sh"]*0.6}"/>'
    # угловые доски
    for x in (30, 123):
        b += f'<rect x="{x}" y="64" width="7" height="58" fill="{P["G"]}" stroke="{K}" stroke-width="2"/>'
    # цоколь
    b += f'<rect x="25" y="119" width="110" height="9" rx="2" fill="{P["T"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<rect x="26" y="124" width="108" height="3" fill="{K}" opacity="{P["sh"]}"/>'
    # кровля и фронтон
    b += f'<path d="M16 68 L80 16 L144 68 Z" fill="{P["G"]}" stroke="{K}" stroke-width="2.2"/>'
    b += f'<path d="M22 64 L80 21 L86 25.5 L30 66 Z" fill="{P["C"]}" opacity="{P["hl"]*0.35}"/>'
    b += f'<path d="M33 63 L80 27 L127 63 Z" fill="{P["C"]}" stroke="{K}" stroke-width="1.6"/>'
    b += f'<path d="M80 27 L127 63 L80 63 Z" fill="{K}" opacity="{P["sh"]*0.4}"/>'
    b += _sun(80, 63, 19, P)
    # резной подзор под фронтоном
    b += f'<rect x="28" y="62" width="104" height="6" fill="{P["G"]}" stroke="{K}" stroke-width="1.6"/>'
    for i in range(13):
        cx = 32 + i * 8
        b += f'<path d="M{cx-3.6} 68 A3.6 3.6 0 0 0 {cx+3.6} 68 Z" fill="{P["C"]}" stroke="{K}" stroke-width="1.1"/>'
    # окна с резными наличниками и ставнями
    for cx in (58, 102):
        for sx in (cx - 17, cx + 11):
            b += f'<rect x="{sx}" y="84" width="6" height="28" rx="1" fill="{P["G"]}" stroke="{K}" stroke-width="1.6"/>'
            b += _dia(sx + 3, 98, 2.4, P["A"], P, 0.8)
        b += f'<rect x="{cx-11}" y="84" width="22" height="28" fill="{P["C"]}" stroke="{K}" stroke-width="1.8"/>'
        b += f'<rect x="{cx-7.5}" y="87.5" width="15" height="21" fill="{P["Gd"]}" stroke="{K}" stroke-width="1.2"/>'
        b += f'<path d="M{cx-7.5} 96 L{cx-1} 87.5 L{cx+4} 87.5 L{cx-7.5} 102 Z" fill="{P["C"]}" opacity="{P["hl"]*0.55}"/>'
        b += f'<line x1="{cx}" y1="88" x2="{cx}" y2="108" stroke="{P["C"]}" stroke-width="2"/>'
        b += f'<line x1="{cx-7}" y1="96" x2="{cx+7}" y2="96" stroke="{P["C"]}" stroke-width="2"/>'
        # кокошник наличника
        b += (f'<path d="M{cx-14} 85 L{cx-14} 80 Q{cx-14} 72 {cx-6} 71 Q{cx-2} 66 {cx} 64 Q{cx+2} 66 {cx+6} 71 '
              f'Q{cx+14} 72 {cx+14} 80 L{cx+14} 85 Z" fill="{P["C"]}" stroke="{K}" stroke-width="1.8"/>')
        b += _dia(cx, 76, 3.6, P["A"], P, 1)
        b += f'<circle cx="{cx-8.5}" cy="80" r="1.6" fill="{P["T"]}"/><circle cx="{cx+8.5}" cy="80" r="1.6" fill="{P["T"]}"/>'
        b += f'<rect x="{cx-14}" y="111" width="28" height="4.5" rx="1.2" fill="{P["C"]}" stroke="{K}" stroke-width="1.6"/>'
        for dx in (-7, 0, 7):
            b += f'<path d="M{cx+dx-2.4} 115.5 A2.4 2.4 0 0 0 {cx+dx+2.4} 115.5 Z" fill="{P["C"]}" stroke="{K}" stroke-width="1"/>'
    # куст для глубины
    b += (f'<path d="M8 126 Q4 112 14 108 Q16 98 26 101 Q34 98 36 108 Q42 114 38 126 Z" fill="{P["G"]}" stroke="{K}" stroke-width="2"/>'
          f'<path d="M14 110 Q18 103 25 104" fill="none" stroke="{P["C"]}" stroke-width="2" opacity="{P["hl"]*0.7}"/>')
    b += f'<path d="M136 126 Q134 116 142 114 Q146 106 154 112 Q158 120 154 126 Z" fill="{P["G"]}" stroke="{K}" stroke-width="2"/>'
    return _svg(b, w, h)


def book(P=ACTIVE, w=160, h=140):
    K = P["K"]
    b = _ground(P, cy=126)
    # обложка
    b += f'<path d="M10 54 L80 64 L150 54 L150 116 L80 128 L10 116 Z" fill="{P["G"]}" stroke="{K}" stroke-width="2.2"/>'
    b += f'<path d="M80 64 L150 54 L150 116 L80 128 Z" fill="{K}" opacity="{P["sh"]*0.8}"/>'
    # срез страниц
    b += f'<path d="M16 50 Q50 42 80 56 L80 124 Q50 110 16 118 Z" fill="{P["C2"]}" stroke="{K}" stroke-width="1.8"/>'
    b += f'<path d="M144 50 Q110 42 80 56 L80 124 Q110 110 144 118 Z" fill="{P["C2"]}" stroke="{K}" stroke-width="1.8"/>'
    # страницы
    b += f'<path d="M18 44 Q50 34 80 50 L80 118 Q50 102 18 110 Z" fill="{P["C"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<path d="M142 44 Q110 34 80 50 L80 118 Q110 102 142 110 Z" fill="{P["C"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<path d="M80 50 L80 118 Q72 112 66 110 L66 43 Q74 45 80 50 Z" fill="{K}" opacity="{P["sh"]*0.55}"/>'
    b += f'<path d="M80 50 L80 118 Q86 112 92 110 L92 43 Q86 45 80 50 Z" fill="{K}" opacity="{P["sh"]*0.3}"/>'
    # буквы
    b += (f'<text x="47" y="86" text-anchor="middle" font-family="PT Sans" font-weight="700" font-size="30" '
          f'fill="{P["T"]}" transform="rotate(-6 47 80)">Әә</text>')
    b += (f'<text x="113" y="86" text-anchor="middle" font-family="PT Sans" font-weight="700" font-size="30" '
          f'fill="{P["G"]}" transform="rotate(6 113 80)">Өө</text>')
    for (x, y, wd, rot) in ((30, 94, 32, -6), (32, 101, 24, -6), (98, 91, 32, 6), (100, 98, 24, 6)):
        b += f'<rect x="{x}" y="{y}" width="{wd}" height="3" rx="1.5" fill="{P["L"]}" transform="rotate({rot} {x} {y})"/>'
    # закладка
    b += f'<path d="M86 52 L86 134 L90.5 129 L95 134 L95 54 Z" fill="{P["A"]}" stroke="{K}" stroke-width="1.8"/>'
    # искры-ромбы
    b += _dia(40, 24, 6, P["T"], P) + _dia(80, 14, 8, P["A"], P) + _dia(120, 24, 6, P["G"], P)
    return _svg(b, w, h)


def gate(P=ACTIVE, w=160, h=140):
    K = P["K"]
    b = _ground(P, rx=70)
    # забор
    for x in (6, 17, 28, 124, 135, 146):
        b += f'<path d="M{x} 124 L{x} 84 L{x+4} 78 L{x+8} 84 L{x+8} 124 Z" fill="{P["C2"]}" stroke="{K}" stroke-width="1.8"/>'
    for y in (92, 112):
        b += f'<rect x="4" y="{y}" width="34" height="4" fill="{P["L"]}" stroke="{K}" stroke-width="1.2"/>'
        b += f'<rect x="122" y="{y}" width="34" height="4" fill="{P["L"]}" stroke="{K}" stroke-width="1.2"/>'
    # проём: вид во двор
    b += f'<rect x="48" y="56" width="64" height="68" fill="{P["C"]}"/>'
    b += f'<path d="M48 104 Q80 92 112 104 L112 124 L48 124 Z" fill="{P["G"]}" opacity=".35"/>'
    b += f'<path d="M72 124 L77 100 L83 100 L96 124 Z" fill="{P["C3"]}" stroke="{P["L"]}" stroke-width="1"/>'
    b += f'<circle cx="96" cy="72" r="7" fill="{P["A"]}" opacity=".85"/>'
    b += f'<rect x="48" y="56" width="64" height="68" fill="none" stroke="{K}" stroke-width="1.6"/>'
    # столбы
    for x in (34, 112):
        b += f'<rect x="{x}" y="46" width="14" height="80" fill="{P["T"]}" stroke="{K}" stroke-width="2"/>'
        b += f'<rect x="{x+9}" y="47" width="4" height="78" fill="{K}" opacity="{P["sh"]}"/>'
        b += _dia(x + 7, 72, 3.2, P["A"], P, 1) + _dia(x + 7, 100, 3.2, P["A"], P, 1)
    # навес и резная балка
    b += f'<path d="M22 48 L80 18 L138 48 L132 52 L80 26 L28 52 Z" fill="{P["G"]}" stroke="{K}" stroke-width="2.2"/>'
    b += f'<path d="M36 49 L80 27 L124 49 Z" fill="{P["C"]}" stroke="{K}" stroke-width="1.6"/>'
    b += _sun(80, 49, 14, P, 0.45)
    b += f'<rect x="32" y="48" width="96" height="9" fill="{P["C"]}" stroke="{K}" stroke-width="1.8"/>'
    for i in range(7):
        cx = 40 + i * 13.3
        b += _dia(round(cx, 1), 52.5, 2.6, P["T"] if i % 2 else P["G"], P, 0.8)
    # калитка, приоткрытая на зрителя
    b += f'<path d="M48 57 L70 51 L70 133 L48 124 Z" fill="{P["G"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<path d="M52 62 L66 58.5 L66 86 L52 88 Z" fill="none" stroke="{P["Gd"]}" stroke-width="1.6"/>'
    b += f'<path d="M52 94 L66 92.5 L66 124 L52 119 Z" fill="none" stroke="{P["Gd"]}" stroke-width="1.6"/>'
    b += _dia(59, 74, 4, P["A"], P, 1) + _dia(59, 106, 4, P["A"], P, 1)
    b += f'<path d="M49 58 L53 57 L53 124 L49 123 Z" fill="{P["C"]}" opacity="{P["hl"]*0.4}"/>'
    b += f'<circle cx="66" cy="96" r="1.8" fill="{P["A"]}" stroke="{K}" stroke-width="1"/>'
    return _svg(b, w, h)


def calendar(P=ACTIVE, w=160, h=140):
    K = P["K"]
    b = _ground(P, cx=78)
    b += f'<rect x="24" y="34" width="78" height="88" rx="9" fill="{P["C2"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<rect x="20" y="30" width="78" height="88" rx="9" fill="{P["C"]}" stroke="{K}" stroke-width="2.2"/>'
    b += f'<path d="M20 52 L20 39 Q20 30 29 30 L89 30 Q98 30 98 39 L98 52 Z" fill="{P["T"]}" stroke="{K}" stroke-width="2.2"/>'
    b += f'<rect x="22" y="32" width="74" height="5" rx="2.5" fill="{P["C"]}" opacity="{P["hl"]*0.35}"/>'
    for x in (38, 80):
        b += f'<rect x="{x-3.5}" y="22" width="7" height="16" rx="3.5" fill="{P["G"]}" stroke="{K}" stroke-width="1.8"/>'
    b += (f'<text x="59" y="102" text-anchor="middle" font-family="PT Sans" font-weight="700" font-size="50" '
          f'fill="{P["G"]}">7</text>')
    b += f'<path d="M20 118 L98 118" stroke="{K}" stroke-width="0"/>'
    for x in range(28, 92, 9):
        b += f'<circle cx="{x}" cy="58" r="1.4" fill="{P["L"]}"/>'
    # будильник
    cx, cy = 114, 88
    for s in (-1, 1):
        b += f'<line x1="{cx+s*18}" y1="{cy+24}" x2="{cx+s*24}" y2="{cy+33}" stroke="{K}" stroke-width="3"/>'
        bx, by = cx + s * 20, cy - 27
        b += f'<path d="M{bx-10} {by+5} A10 10 0 0 1 {bx+10} {by+5} Z" fill="{P["A"]}" stroke="{K}" stroke-width="1.8" transform="rotate({s*35} {bx} {by})"/>'
    b += f'<circle cx="{cx}" cy="{cy}" r="31" fill="{P["A"]}" stroke="{K}" stroke-width="2.2"/>'
    b += f'<path d="M{cx-24} {cy-14} A27 27 0 0 1 {cx+6} {cy-27}" fill="none" stroke="{P["C"]}" stroke-width="3.5" opacity="{P["hl"]*0.9}"/>'
    b += f'<circle cx="{cx}" cy="{cy}" r="23" fill="{P["C"]}" stroke="{K}" stroke-width="1.8"/>'
    b += f'<path d="M{cx} {cy} m0 -23 a23 23 0 0 1 0 46" fill="{K}" opacity="{P["sh"]*0.3}"/>'
    for i in range(12):
        a = math.pi * 2 * i / 12
        r1 = 17 if i % 3 else 15
        b += (f'<line x1="{cx+math.sin(a)*r1:.1f}" y1="{cy-math.cos(a)*r1:.1f}" x2="{cx+math.sin(a)*20:.1f}" '
              f'y2="{cy-math.cos(a)*20:.1f}" stroke="{K}" stroke-width="{2 if i % 3 == 0 else 1.2}"/>')
    b += f'<line x1="{cx}" y1="{cy}" x2="{cx}" y2="{cy-13}" stroke="{K}" stroke-width="3.2"/>'
    b += f'<line x1="{cx}" y1="{cy}" x2="{cx+11}" y2="{cy+7}" stroke="{P["T"]}" stroke-width="2.6"/>'
    b += f'<circle cx="{cx}" cy="{cy}" r="2.6" fill="{K}"/>'
    b += f'<rect x="{cx-4}" y="{cy-36}" width="8" height="6" rx="2" fill="{P["Ad"]}" stroke="{K}" stroke-width="1.4"/>'
    return _svg(b, w, h)


def echpochmak_shape(cx, cy, s, P, rot=0):
    # треугольный пирожок: корочка, защипы, отверстие сверху
    K = P["K"]
    h = s * 0.86
    top, bl, br = (cx, cy - h * 0.62), (cx - s / 2, cy + h * 0.38), (cx + s / 2, cy + h * 0.38)
    d = f"M{top[0]:.1f} {top[1]:.1f} L{br[0]:.1f} {br[1]:.1f} L{bl[0]:.1f} {bl[1]:.1f} Z"
    g = f'<g transform="rotate({rot} {cx} {cy})">'
    g += f'<path d="{d}" fill="{P["A"]}" stroke="{K}" stroke-width="{max(2.2, s*0.09):.1f}" stroke-linejoin="round"/>'
    g += f'<path d="{d}" fill="none" stroke="{P["A"]}" stroke-width="{s*0.04:.1f}"/>'
    hx, hy = cx, cy + h * 0.02
    for p in (top, bl, br):
        mx, my = hx + (p[0] - hx) * 0.72, hy + (p[1] - hy) * 0.72
        g += f'<line x1="{hx:.1f}" y1="{hy:.1f}" x2="{mx:.1f}" y2="{my:.1f}" stroke="{P["Ad"]}" stroke-width="{s*0.045:.1f}" opacity=".6"/>'
    g += f'<ellipse cx="{hx:.1f}" cy="{hy:.1f}" rx="{s*0.09:.1f}" ry="{s*0.065:.1f}" fill="{P["Td"]}" stroke="{K}" stroke-width="1"/>'
    g += (f'<path d="M{top[0]-s*0.06:.1f} {top[1]+h*0.18:.1f} L{bl[0]+s*0.2:.1f} {bl[1]-h*0.12:.1f}" '
          f'stroke="{P["C"]}" stroke-width="{s*0.06:.1f}" opacity="{P["hl"]*0.8}"/>')
    return g + '</g>'


def samovar(P=ACTIVE, w=160, h=140):
    K = P["K"]
    b = _ground(P, rx=68)
    cx = 58
    # пар
    b += f'<path d="M{cx+2} 18 q-7 -6 0 -11 q6 -5 0 -10" fill="none" stroke="{P["L"]}" stroke-width="3"/>'
    # ручки
    for s in (-1, 1):
        d = f"M{cx+s*24} 62 Q{cx+s*38} 62 {cx+s*36} 76 Q{cx+s*34} 84 {cx+s*26} 84"
        b += f'<path d="{d}" fill="none" stroke="{K}" stroke-width="7"/>'
        b += f'<path d="{d}" fill="none" stroke="{P["T"]}" stroke-width="3.6"/>'
    # основание
    b += f'<path d="M38 124 L78 124 L72 112 L44 112 Z" fill="{P["A"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<rect x="{cx-9}" y="102" width="18" height="11" fill="{P["A"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<rect x="{cx-9}" y="102" width="18" height="11" fill="{K}" opacity="{P["sh"]}"/>'
    # тело
    body = f"M34 100 Q28 66 44 54 L72 54 Q88 66 82 100 Q58 110 34 100 Z"
    b += f'<path d="{body}" fill="{P["A"]}" stroke="{K}" stroke-width="2.2"/>'
    b += f'<path d="M66 55 L72 54 Q88 66 82 100 Q76 103 68 104 Q80 80 66 55 Z" fill="{K}" opacity="{P["sh"]*0.9}"/>'
    b += f'<path d="M41 64 Q37 80 41 96" fill="none" stroke="{P["C"]}" stroke-width="5" opacity="{P["hl"]*0.85}"/>'
    b += f'<path d="M32 66 Q58 72 84 66" fill="none" stroke="{P["Ad"]}" stroke-width="1.4" opacity=".7"/>'
    b += f'<path d="M31 90 Q58 97 85 90" fill="none" stroke="{P["Ad"]}" stroke-width="1.4" opacity=".7"/>'
    for x in (48, 58, 68):
        b += f'<circle cx="{x}" cy="78" r="3.4" fill="{P["C2"]}" stroke="{P["Ad"]}" stroke-width="1.2"/>'
    # кран
    b += f'<path d="M{cx-4} 92 L{cx+4} 92 L{cx+4} 98 L{cx+10} 102 L{cx+10} 106 L{cx-2} 100 Z" fill="{P["A"]}" stroke="{K}" stroke-width="1.6"/>'
    b += f'<rect x="{cx-6}" y="88" width="12" height="4" rx="2" fill="{P["T"]}" stroke="{K}" stroke-width="1.3"/>'
    # венец
    b += f'<path d="M42 55 L47 46 L69 46 L74 55 Z" fill="{P["A"]}" stroke="{K}" stroke-width="2"/>'
    # чайник сверху
    b += f'<path d="M{cx-12} 34 Q{cx-24} 30 {cx-24} 22" fill="none" stroke="{K}" stroke-width="5"/>'
    b += f'<path d="M{cx-12} 34 Q{cx-24} 30 {cx-24} 22" fill="none" stroke="{P["G"]}" stroke-width="2.4"/>'
    b += f'<path d="M{cx+12} 30 Q{cx+20} 30 {cx+18} 38 Q{cx+16} 42 {cx+11} 41" fill="none" stroke="{K}" stroke-width="4"/>'
    b += f'<ellipse cx="{cx}" cy="36" rx="14" ry="10.5" fill="{P["G"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<path d="M{cx-9} 31 Q{cx-4} 27 {cx+2} 27" fill="none" stroke="{P["C"]}" stroke-width="2.2" opacity="{P["hl"]*0.7}"/>'
    b += _dia(cx, 38, 3.6, P["A"], P, 0.9)
    b += f'<path d="M{cx-6} 26 Q{cx} 18 {cx+6} 26 Z" fill="{P["G"]}" stroke="{K}" stroke-width="1.6"/>'
    b += f'<circle cx="{cx}" cy="20" r="2" fill="{P["A"]}" stroke="{K}" stroke-width="1"/>'
    # тарелка с эчпочмаками
    b += f'<ellipse cx="122" cy="116" rx="33" ry="9.5" fill="{P["C"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<ellipse cx="122" cy="115" rx="25" ry="5.5" fill="{P["C2"]}" stroke="{P["G"]}" stroke-width="1.6"/>'
    b += echpochmak_shape(122, 92, 30, P)
    b += echpochmak_shape(107, 106, 26, P, -8)
    b += echpochmak_shape(137, 106, 26, P, 8)
    return _svg(b, w, h)


ILLUS = {"house": house, "book": book, "gate": gate, "calendar": calendar, "samovar": samovar}


def illus(kind, w, h=None, locked=False):
    h = h or round(w * 140 / 160)
    return ILLUS[kind](LOCKED if locked else ACTIVE, w, h)


# ---------- модули 6-9 (04.11.2026) ----------
def city(P=ACTIVE, w=160, h=140):
    """Дом и город: улица из двух домов, фонарь, мостовая."""
    K = P["K"]
    b = _ground(P, rx=72)
    # мостовая
    b += f'<path d="M4 124 L156 124 L150 132 L10 132 Z" fill="{P["C3"]}" stroke="{K}" stroke-width="1.6"/>'
    for x in range(18, 150, 16):
        b += f'<line x1="{x}" y1="125" x2="{x-3}" y2="131" stroke="{P["L"]}" stroke-width="1.4"/>'
    # дальний дом (терракотовый кирпич, два этажа)
    b += f'<rect x="64" y="40" width="64" height="84" fill="{P["T"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<rect x="112" y="41" width="15" height="82" fill="{K}" opacity="{P["sh"]*0.8}"/>'
    for y in (52, 66, 80, 94, 108):
        b += f'<line x1="65" y1="{y}" x2="127" y2="{y}" stroke="{P["Td"]}" stroke-width="1" opacity=".5"/>'
    b += f'<path d="M60 42 L132 42 L126 30 L66 30 Z" fill="{P["G"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<rect x="60" y="40" width="72" height="5" fill="{P["C"]}" stroke="{K}" stroke-width="1.4"/>'
    for cx in (80, 112):
        for top in (52, 84):
            b += (f'<path d="M{cx-8} {top+20} L{cx-8} {top+7} A8 8 0 0 1 {cx+8} {top+7} L{cx+8} {top+20} Z" '
                  f'fill="{P["C"]}" stroke="{K}" stroke-width="1.6"/>')
            b += f'<path d="M{cx-5} {top+19} L{cx-5} {top+8} A5 5 0 0 1 {cx+5} {top+8} L{cx+5} {top+19} Z" fill="{P["Gd"]}"/>'
            b += f'<line x1="{cx}" y1="{top+3}" x2="{cx}" y2="{top+19}" stroke="{P["C"]}" stroke-width="1.6"/>'
            b += f'<rect x="{cx-10}" y="{top+19}" width="20" height="3.5" rx="1" fill="{P["C"]}" stroke="{K}" stroke-width="1.2"/>'
    b += _dia(96, 36, 3.4, P["A"], P, 1)
    # ближний дом (зелёный, деревянный, с резным фронтоном)
    b += f'<rect x="18" y="74" width="50" height="50" fill="{P["C2"]}" stroke="{K}" stroke-width="2"/>'
    for y in (82, 90, 98, 106, 114):
        b += f'<line x1="19" y1="{y}" x2="67" y2="{y}" stroke="{P["Ad"]}" stroke-width="1" opacity=".28"/>'
    b += f'<rect x="56" y="75" width="11" height="48" fill="{K}" opacity="{P["sh"]*0.6}"/>'
    b += f'<path d="M12 77 L43 50 L74 77 Z" fill="{P["G"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<path d="M22 75 L43 57 L64 75 Z" fill="{P["C"]}" stroke="{K}" stroke-width="1.4"/>'
    b += _sun(43, 75, 10, P, 0.45)
    b += f'<rect x="27" y="86" width="16" height="20" fill="{P["Gd"]}" stroke="{K}" stroke-width="1.6"/>'
    b += f'<line x1="35" y1="86" x2="35" y2="106" stroke="{P["C"]}" stroke-width="1.6"/>'
    b += f'<path d="M24 87 L24 83 Q35 77 46 83 L46 87 Z" fill="{P["C"]}" stroke="{K}" stroke-width="1.4"/>'
    b += f'<rect x="49" y="94" width="13" height="30" rx="1.5" fill="{P["T"]}" stroke="{K}" stroke-width="1.6"/>'
    b += f'<circle cx="59" cy="110" r="1.3" fill="{P["A"]}"/>'
    # фонарь
    b += f'<line x1="142" y1="124" x2="142" y2="64" stroke="{K}" stroke-width="3.4"/>'
    b += f'<line x1="142" y1="124" x2="142" y2="64" stroke="{P["G"]}" stroke-width="1.6"/>'
    b += f'<path d="M135 64 L149 64 L146 52 L138 52 Z" fill="{P["A"]}" stroke="{K}" stroke-width="1.6"/>'
    b += f'<path d="M136 52 L148 52 L142 46 Z" fill="{P["G"]}" stroke="{K}" stroke-width="1.4"/>'
    b += f'<rect x="137" y="120" width="10" height="5" rx="1" fill="{P["G"]}" stroke="{K}" stroke-width="1.2"/>'
    # дерево у дома
    b += f'<path d="M2 124 Q-2 106 8 102 Q8 90 18 94 Q22 104 16 124 Z" fill="{P["G"]}" stroke="{K}" stroke-width="1.8"/>'
    return _svg(b, w, h)


def verbs(P=ACTIVE, w=160, h=140):
    """Что я делаю: лейка поливает цветок в горшке, рядом метла; линии движения."""
    K = P["K"]
    b = _ground(P, rx=66)
    # горшок с цветком
    b += f'<path d="M96 96 L132 96 L127 124 L101 124 Z" fill="{P["T"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<rect x="93" y="90" width="42" height="9" rx="2" fill="{P["T"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<path d="M118 99 L127 99 L124 122 L116 122 Z" fill="{K}" opacity="{P["sh"]}"/>'
    b += _dia(114, 110, 4, P["A"], P, 1)
    b += f'<path d="M114 90 Q112 70 116 52" fill="none" stroke="{K}" stroke-width="4"/><path d="M114 90 Q112 70 116 52" fill="none" stroke="{P["G"]}" stroke-width="2"/>'
    b += f'<path d="M113 76 Q98 70 96 60 Q108 60 113 72 Z" fill="{P["G"]}" stroke="{K}" stroke-width="1.6"/>'
    b += f'<path d="M115 70 Q130 62 134 52 Q120 52 115 66 Z" fill="{P["G"]}" stroke="{K}" stroke-width="1.6"/>'
    # цветок-тюльпан
    b += f'<path d="M106 44 Q106 30 112 28 L116 36 L120 28 Q126 30 126 44 Q122 54 116 54 Q110 54 106 44 Z" fill="{P["T"]}" stroke="{K}" stroke-width="1.8"/>'
    b += f'<path d="M110 42 Q110 34 113 32" fill="none" stroke="{P["C"]}" stroke-width="1.6" opacity="{P["hl"]*0.8}"/>'
    # капли
    for (x, y) in ((86, 46), (92, 56), (82, 60), (96, 68)):
        b += f'<path d="M{x} {y-5} Q{x+4} {y} {x} {y+3} Q{x-4} {y} {x} {y-5} Z" fill="{P["C"]}" stroke="{K}" stroke-width="1.3"/>'
    # лейка (наклонена)
    g = '<g transform="rotate(-18 46 66)">'
    g += f'<path d="M66 60 L94 40 L97 44 L70 66 Z" fill="{P["G"]}" stroke="{K}" stroke-width="1.8"/>'
    g += f'<ellipse cx="95" cy="41" rx="4" ry="6" fill="{P["G"]}" stroke="{K}" stroke-width="1.6" transform="rotate(-35 95 41)"/>'
    g += f'<path d="M24 54 Q20 42 34 40 L58 40 Q72 42 68 54 L66 86 Q46 92 26 86 Z" fill="{P["G"]}" stroke="{K}" stroke-width="2.2"/>'
    g += f'<path d="M30 50 Q28 66 32 82" fill="none" stroke="{P["C"]}" stroke-width="3.4" opacity="{P["hl"]*0.7}"/>'
    g += f'<path d="M58 44 Q66 70 62 86 Q66 86 66 86 L68 54 Q70 46 58 44 Z" fill="{K}" opacity="{P["sh"]}"/>'
    g += f'<path d="M30 40 Q46 14 62 40" fill="none" stroke="{K}" stroke-width="6"/><path d="M30 40 Q46 14 62 40" fill="none" stroke="{P["A"]}" stroke-width="3"/>'
    g += f'<rect x="24" y="58" width="44" height="7" fill="{P["A"]}" stroke="{K}" stroke-width="1.4"/>'
    g += _dia(46, 74, 4, P["A"], P, 1)
    g += '</g>'
    b += g
    # линии движения
    for (x1, y1, x2, y2) in ((10, 40, 18, 46), (8, 56, 18, 58), (12, 72, 20, 68)):
        b += f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{P["Ad"]}" stroke-width="2.2"/>'
    return _svg(b, w, h)


def shop(P=ACTIVE, w=160, h=140):
    """Магазин и кафе: рыночный прилавок с навесом, яблоки, груши, весы, чашка."""
    K = P["K"]
    b = _ground(P, rx=70)
    # стойки
    for x in (20, 136):
        b += f'<rect x="{x}" y="40" width="6" height="84" fill="{P["G"]}" stroke="{K}" stroke-width="1.8"/>'
    # навес в полоску с фестонами
    b += f'<path d="M8 44 L20 18 L142 18 L152 44 Z" fill="{P["C"]}" stroke="{K}" stroke-width="2"/>'
    for i in range(6):
        x0 = 20 + i * 20.3
        xb = 8 + i * 24
        if i % 2 == 0:
            b += f'<path d="M{x0:.1f} 18 L{x0+20.3:.1f} 18 L{xb+24} 44 L{xb} 44 Z" fill="{P["G"]}"/>'
    b += f'<path d="M8 44 L20 18 L142 18 L152 44 Z" fill="none" stroke="{K}" stroke-width="2"/>'
    for i in range(6):
        cx = 8 + 12 + i * 24
        fill = P["G"] if i % 2 == 0 else P["C"]
        b += f'<path d="M{cx-12} 44 A12 9 0 0 0 {cx+12} 44 Z" fill="{fill}" stroke="{K}" stroke-width="1.8"/>'
    b += f'<rect x="20" y="15" width="122" height="5" rx="2" fill="{P["T"]}" stroke="{K}" stroke-width="1.6"/>'
    # вывеска
    b += f'<rect x="58" y="58" width="44" height="16" rx="3" fill="{P["C"]}" stroke="{K}" stroke-width="1.6"/>'
    b += _dia(68, 66, 3, P["T"], P, 0.8) + _dia(80, 66, 3, P["A"], P, 0.8) + _dia(92, 66, 3, P["G"], P, 0.8)
    b += f'<line x1="66" y1="54" x2="70" y2="58" stroke="{K}" stroke-width="1.2"/><line x1="94" y1="54" x2="90" y2="58" stroke="{K}" stroke-width="1.2"/>'
    # прилавок
    b += f'<rect x="14" y="96" width="132" height="28" rx="2" fill="{P["T"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<rect x="12" y="92" width="136" height="7" rx="2" fill="{P["C2"]}" stroke="{K}" stroke-width="1.8"/>'
    for x in (40, 80, 120):
        b += f'<line x1="{x}" y1="100" x2="{x}" y2="123" stroke="{P["Td"]}" stroke-width="1.6"/>'
    b += f'<rect x="15" y="117" width="130" height="6" fill="{K}" opacity="{P["sh"]}"/>'
    # ящик с яблоками
    b += f'<rect x="20" y="80" width="40" height="13" fill="{P["C2"]}" stroke="{K}" stroke-width="1.6"/>'
    for (x, y) in ((27, 78), (37, 76), (47, 78), (32, 70), (42, 70), (53, 74)):
        b += f'<circle cx="{x}" cy="{y}" r="6" fill="{P["T"]}" stroke="{K}" stroke-width="1.4"/>'
        b += f'<circle cx="{x-2}" cy="{y-2}" r="1.6" fill="{P["C"]}" opacity="{P["hl"]*0.9}"/>'
    # груши
    for (x, y) in ((70, 84), (82, 82)):
        b += (f'<path d="M{x} {y-12} Q{x+4} {y-12} {x+4} {y-5} Q{x+9} {y} {x+6} {y+6} Q{x} {y+10} {x-6} {y+6} '
              f'Q{x-9} {y} {x-4} {y-5} Q{x-4} {y-12} {x} {y-12} Z" fill="{P["A"]}" stroke="{K}" stroke-width="1.4"/>')
        b += f'<line x1="{x}" y1="{y-12}" x2="{x+2}" y2="{y-16}" stroke="{K}" stroke-width="1.4"/>'
    # весы
    b += f'<rect x="100" y="86" width="26" height="7" rx="2" fill="{P["A"]}" stroke="{K}" stroke-width="1.5"/>'
    b += f'<line x1="113" y1="86" x2="113" y2="76" stroke="{K}" stroke-width="2"/>'
    b += f'<path d="M100 76 Q113 72 126 76" fill="none" stroke="{K}" stroke-width="1.6"/>'
    b += f'<path d="M98 76 L108 76 Q106 82 103 82 Q100 82 98 76 Z" fill="{P["A"]}" stroke="{K}" stroke-width="1.3"/>'
    b += f'<path d="M118 76 L128 76 Q126 82 123 82 Q120 82 118 76 Z" fill="{P["A"]}" stroke="{K}" stroke-width="1.3"/>'
    # чашка чая
    b += f'<path d="M132 80 L146 80 Q146 92 139 92 Q132 92 132 80 Z" fill="{P["C"]}" stroke="{K}" stroke-width="1.5"/>'
    b += f'<path d="M146 83 Q151 84 146 89" fill="none" stroke="{K}" stroke-width="1.4"/>'
    b += f'<path d="M137 76 q-2 -3 0 -6 M141 76 q-2 -3 0 -6" fill="none" stroke="{P["L"]}" stroke-width="1.6"/>'
    return _svg(b, w, h)


def nature(P=ACTIVE, w=160, h=140):
    """Природа: берёза, река между холмами, солнце."""
    K = P["K"]
    b = _ground(P, rx=72)
    # солнце
    b += f'<circle cx="122" cy="32" r="15" fill="{P["A"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<path d="M113 26 A10 10 0 0 1 124 21" fill="none" stroke="{P["C"]}" stroke-width="2.4" opacity="{P["hl"]*0.8}"/>'
    for i in range(8):
        a = math.pi * 2 * i / 8
        b += (f'<line x1="{122+math.cos(a)*20:.1f}" y1="{32+math.sin(a)*20:.1f}" x2="{122+math.cos(a)*26:.1f}" '
              f'y2="{32+math.sin(a)*26:.1f}" stroke="{P["Ad"]}" stroke-width="2"/>')
    # дальний холм
    b += f'<path d="M40 92 Q90 56 156 84 L156 124 L40 124 Z" fill="{P["G"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<path d="M60 80 Q90 64 120 68" fill="none" stroke="{P["C"]}" stroke-width="2.4" opacity="{P["hl"]*0.45}"/>'
    # ближний холм
    b += f'<path d="M4 104 Q50 84 100 104 L100 124 L4 124 Z" fill="{P["G"]}" stroke="{K}" stroke-width="2"/>'
    b += f'<path d="M4 104 Q50 84 100 104 L100 110 Q50 92 4 110 Z" fill="{P["C"]}" opacity="{P["hl"]*0.25}"/>'
    # река
    b += f'<path d="M100 124 Q96 112 112 106 Q132 98 156 100 L156 112 Q134 110 124 116 Q116 120 120 124 Z" fill="{P["C"]}" stroke="{K}" stroke-width="1.8"/>'
    b += f'<path d="M126 108 q4 -2 8 0 M138 104 q4 -2 8 0" fill="none" stroke="{P["L"]}" stroke-width="1.6"/>'
    # берёза
    b += f'<path d="M36 124 L38 58 L44 58 L46 124 Z" fill="{P["C"]}" stroke="{K}" stroke-width="2"/>'
    for (y, x, wd) in ((112, 38, 6), (98, 40, 5), (86, 38, 6), (72, 40, 4)):
        b += f'<rect x="{x}" y="{y}" width="{wd}" height="2.6" rx="1.2" fill="{K}"/>'
    b += f'<path d="M42 82 L54 70" stroke="{K}" stroke-width="2.4"/><path d="M41 92 L30 82" stroke="{K}" stroke-width="2.2"/>'
    crown = "M41 18 Q56 16 60 30 Q72 36 66 50 Q70 64 56 66 Q48 76 38 68 Q22 72 20 58 Q10 48 20 38 Q20 22 41 18 Z"
    b += f'<path d="{crown}" fill="{P["G"]}" stroke="{K}" stroke-width="2.2"/>'
    b += f'<path d="M24 40 Q26 26 40 23" fill="none" stroke="{P["C"]}" stroke-width="3" opacity="{P["hl"]*0.55}"/>'
    b += f'<path d="M50 60 Q60 58 62 48" fill="none" stroke="{K}" stroke-width="1.4" opacity="{P["sh"]*1.8}"/>'
    for (x, y) in ((30, 36), (46, 32), (56, 48), (34, 56)):
        b += f'<path d="M{x} {y} q4 -3 8 0" fill="none" stroke="{P["Gd"]}" stroke-width="1.6"/>'
    # травинки
    for x in (14, 60, 84):
        b += f'<path d="M{x} 118 l-3 -7 M{x} 118 l1 -8 M{x} 118 l4 -6" stroke="{K}" stroke-width="1.4" fill="none"/>'
    return _svg(b, w, h)


ILLUS.update({"city": city, "verbs": verbs, "shop": shop, "nature": nature})
