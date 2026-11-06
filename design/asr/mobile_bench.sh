#!/usr/bin/env bash
# Размер и скорость модели распознавания для телефона: конвертация в ggml (whisper.cpp), квантизация и замер на CPU компьютера.
#
# Запуск из корня проекта: bash design/asr/mobile_bench.sh   (10-20 минут: клоны репозиториев, сборка, конвертация)
# Что делает (всё в /tmp/asr-mobile): клонирует whisper.cpp и openai/whisper (нужен файл mel-фильтров), берёт модель из кэша
# Hugging Face (скачивает при отсутствии), конвертирует в ggml (f16), квантует в q5_1 и q8_0, печатает размеры,
# замеряет время распознавания одной записи на 1 и 4 потоках CPU (без Metal: так ближе к Android) и считает долю узнанных слов
# на ваших записях для q5_1 (потеря точности от квантизации).
# Внимание: скрипт запускает чужой код (конвертер и сборку whisper.cpp), поэтому запускайте его сами и только в этом окружении.
# Замер на Mac (Apple M4 Pro) быстрее типичного телефона; цифры только ориентир, настоящий замер нужен на устройстве.
set -euo pipefail
WORK=${WORK:-/tmp/asr-mobile}
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
PY=${PY:-/tmp/asr-venv/bin/python}
MODEL_ID=${MODEL_ID:-mcronomus/whisper-small-tt}
SAMPLE=${SAMPLE:-$ROOT/design/asr/recordings/islam_l3_s1.wav}

mkdir -p "$WORK/out"
[ -d "$WORK/whisper.cpp" ] || git clone --depth 1 https://github.com/ggml-org/whisper.cpp.git "$WORK/whisper.cpp"
[ -d "$WORK/whisper-openai" ] || git clone --depth 1 https://github.com/openai/whisper.git "$WORK/whisper-openai"

SNAP=$("$PY" -c "from huggingface_hub import snapshot_download; print(snapshot_download('$MODEL_ID'))")
echo "Модель: $SNAP"
if [ ! -f "$WORK/out/ggml-model.bin" ]; then
  (cd "$WORK/out" && "$PY" "$WORK/whisper.cpp/models/convert-h5-to-ggml.py" "$SNAP" "$WORK/whisper-openai" "$WORK/out")
fi

cmake -S "$WORK/whisper.cpp" -B "$WORK/build" -DGGML_METAL=OFF -DCMAKE_BUILD_TYPE=Release >/dev/null
cmake --build "$WORK/build" -j --config Release >/dev/null
CLI=$(find "$WORK/build/bin" -name 'whisper-cli' | head -1)
QUANT=$(find "$WORK/build/bin" \( -name 'whisper-quantize' -o -name 'quantize' \) | head -1)
echo "whisper-cli: $CLI"; echo "quantize: $QUANT"

for q in q5_1 q8_0; do
  [ -f "$WORK/out/ggml-model-$q.bin" ] || "$QUANT" "$WORK/out/ggml-model.bin" "$WORK/out/ggml-model-$q.bin" "$q"
done
echo; echo "== Размеры файлов моделей =="
ls -lh "$WORK/out"/ggml-model*.bin | awk '{print $5, $9}'

echo; echo "== Скорость на CPU компьютера (запись: $SAMPLE) =="
afinfo "$SAMPLE" | grep -i "estimated duration" || true
for m in ggml-model ggml-model-q8_0 ggml-model-q5_1; do
  for t in 1 4; do
    echo "--- $m, потоков: $t"
    "$CLI" -m "$WORK/out/$m.bin" -f "$SAMPLE" -l tt -t "$t" -nt 2>&1 | grep -E "load time|encode time|decode time|total time" || true
  done
done

echo; echo "== Точность после квантизации q5_1 (ваши записи) =="
"$PY" "$ROOT/design/asr/evaluate.py" --backend cpp --cli "$CLI" --model "$WORK/out/ggml-model-q5_1.bin" | tail -4
