#!/usr/bin/env python3
"""Рендерит эталонные PNG экранов из design/Tatar App Screens.dc.html в design/screens/NN.png.

Нужны только Python 3 и Google Chrome (путь можно задать переменной CHROME). Playwright не нужен.
Запуск из корня проекта:
    python3 design/render_screens.py            # все экраны
    python3 design/render_screens.py 03 05 06   # только указанные номера

Как это работает: макет на шаблонах (sc-if, {{ }}) отдаётся по локальному HTTP, Chrome отрисовывает страницу,
скрипт забирает готовую разметку каждого экрана (390x844) и снимает её отдельно в масштабе x2 на прозрачном фоне.
"""
import html
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import threading
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer

ROOT = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(ROOT, "Tatar App Screens.dc.html")
OUT = os.path.join(ROOT, "screens")
CHROME = os.environ.get("CHROME", "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")

# Забираем разметку кадра каждого экрана после того, как шаблоны отработали.
COLLECT = """<pre id="rects" style="position:absolute;left:0;top:0;opacity:0"></pre>
<script>
window.addEventListener('load', function () { setTimeout(function () {
  var out = [];
  document.querySelectorAll('[data-screen-label]').forEach(function (el) {
    var frame = null;
    el.querySelectorAll('div').forEach(function (d) {
      var r = d.getBoundingClientRect();
      if (!frame && Math.round(r.width) === 390 && Math.round(r.height) === 844) frame = d;
    });
    if (frame) out.push({label: el.getAttribute('data-screen-label'), html: frame.outerHTML});
  });
  document.getElementById('rects').textContent = 'RECTS' + JSON.stringify(out) + 'ENDRECTS';
}, 1500); });
</script>
</body>"""

PAGE_HEAD = (
    '<!DOCTYPE html><html><head><meta charset="utf-8"><base href="http://127.0.0.1:{port}/">'
    '<link href="https://fonts.googleapis.com/css2?family=PT+Sans:ital,wght@0,400;0,700;1,400&display=swap" rel="stylesheet">'
    '<style>html,body{{margin:0;background:transparent;font-family:"PT Sans",sans-serif;color:#3D3A2D}}'
    "@keyframes tamyrPulse{{0%,100%{{opacity:.35}}50%{{opacity:1}}}}</style></head><body>"
)


def chrome(args):
    return subprocess.run([CHROME, "--headless", "--disable-gpu", "--hide-scrollbars", *args],
                          capture_output=True, text=True, check=False)


def main():
    wanted = set(sys.argv[1:])
    work = tempfile.mkdtemp(prefix="tamyr-render-")
    try:
        shutil.copy(os.path.join(ROOT, "support.js"), work)
        shutil.copytree(os.path.join(ROOT, "icons"), os.path.join(work, "icons"))
        page = open(SRC, encoding="utf-8").read().replace("</body>", COLLECT, 1)
        page = page.replace("</head>", "<style>html,body{background:transparent !important}</style></head>", 1)
        open(os.path.join(work, "page.html"), "w", encoding="utf-8").write(page)

        class QuietHandler(SimpleHTTPRequestHandler):
            def log_message(self, *args):
                pass

        handler = partial(QuietHandler, directory=work)
        server = ThreadingHTTPServer(("127.0.0.1", 0), handler)
        port = server.server_address[1]
        threading.Thread(target=server.serve_forever, daemon=True).start()

        dom = chrome(["--virtual-time-budget=10000", "--window-size=3400,2300", "--dump-dom",
                      f"http://127.0.0.1:{port}/page.html"]).stdout
        match = re.search(r'<pre id="rects"[^>]*>RECTS(.*?)ENDRECTS</pre>', dom, re.S)
        if not match:
            sys.exit("Не удалось получить разметку экранов: страница не отрисовалась.")
        screens = json.loads(html.unescape(match.group(1)))

        for screen in screens:
            number = screen["label"][:2]
            if wanted and number not in wanted:
                continue
            frame = re.sub(r"box-shadow:\s*(0 14px 36px rgba\(61, ?58, ?45, ?0?\.14\)|rgba\(61, 58, 45, 0\.14\) 0px 14px 36px( 0px)?);?",
                           "", screen["html"])
            name = f"s{number}.html"
            open(os.path.join(work, name), "w", encoding="utf-8").write(
                PAGE_HEAD.format(port=port) + frame + "</body></html>")
            target = os.path.join(OUT, f"{number}.png")
            chrome(["--default-background-color=00000000", "--force-device-scale-factor=2", "--window-size=390,844",
                    "--virtual-time-budget=6000", f"--screenshot={target}", f"http://127.0.0.1:{port}/{name}"])
            print("OK" if os.path.exists(target) else "НЕТ", target)
        server.shutdown()
    finally:
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    main()
