#!/usr/bin/env python3
"""Замер распознавания татарской речи на записях: доля слов, которые модель узнала.

Запуск (из корня проекта, пример для модели с transformers):
    python3 design/asr/evaluate.py --model mcronomus/whisper-small-tt --audio design/asr/recordings
    python3 design/asr/evaluate.py --model dhcppc0/soyle_onnx --backend onnx --audio design/asr/recordings

Файлы записей: design/asr/recordings/<голос>_<id>.wav (например mama_l3_w1.wav, id из phrases.csv); 16 кГц, моно, WAV.
Результат: таблица по каждой записи в design/asr/results_<модель>.csv и итог по голосам: средняя доля узнанных слов и доля
записей, где узнано не меньше 30, 50 и 70 процентов слов (так подбирается порог «засчитано» для беты).
Слово считается узнанным, если в распознанном тексте есть похожее (после той же свёртки татарских букв, что в приложении:
ә→а, ө→о, ү→у, җ→ж, ң→н, һ→х; сходство не ниже 0,8, порядок слов не важен). Записи голосов это личные данные: каталог
recordings в git не попадает (.gitignore), в репозиторий их не добавлять.
Нужны: pip install torch transformers librosa soundfile (для --backend onnx ещё optimum[onnxruntime]).
Скрипт скачивает модель с Hugging Face и запускает её: делай это в отдельном виртуальном окружении.
"""
import argparse
import csv
import difflib
import glob
import os
import re
import statistics
import sys

FOLD = str.maketrans("әөүҗңһ", "аоужнх")
SIMILARITY = 0.8


def words(text):
    text = text.lower().translate(FOLD)
    return re.findall(r"[\w]+", text, flags=re.UNICODE)


def hit_rate(expected, recognized):
    pool = words(recognized)
    want = words(expected)
    if not want:
        return 0.0
    hits = 0
    for word in want:
        best = max(pool, key=lambda p: difflib.SequenceMatcher(None, word, p).ratio(), default=None)
        if best is not None and difflib.SequenceMatcher(None, word, best).ratio() >= SIMILARITY:
            hits += 1
            pool.remove(best)
    return hits / len(want)


def load_recognizer(model, backend, language, cli=None, extra=""):
    if backend == "cpp":
        # whisper.cpp: model это путь к файлу ggml, cli это путь к whisper-cli (замер квантованной модели, как будет на телефоне).
        import subprocess

        def run(path, audio):
            import shlex
            done = subprocess.run([cli, "-m", model, "-f", path, "-l", language, "-nt", "-np", *shlex.split(extra)], capture_output=True, text=True, check=True)
            return done.stdout.strip()

        return run
    if backend == "onnx":
        from optimum.onnxruntime import ORTModelForSpeechSeq2Seq
        from transformers import AutoProcessor, pipeline
        asr = ORTModelForSpeechSeq2Seq.from_pretrained(model)
        processor = AutoProcessor.from_pretrained(model)
        pipe = pipeline("automatic-speech-recognition", model=asr, tokenizer=processor.tokenizer, feature_extractor=processor.feature_extractor)
    else:
        from transformers import pipeline
        pipe = pipeline("automatic-speech-recognition", model=model)
    return lambda path, audio: pipe({"raw": audio, "sampling_rate": 16000}, generate_kwargs={"language": language, "task": "transcribe"})["text"]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", required=True)
    parser.add_argument("--audio", default=os.path.join(os.path.dirname(__file__), "recordings"))
    parser.add_argument("--phrases", default=os.path.join(os.path.dirname(__file__), "phrases.csv"))
    parser.add_argument("--backend", choices=["transformers", "onnx", "cpp"], default="transformers")
    parser.add_argument("--cli", help="для --backend cpp: путь к whisper-cli из whisper.cpp")
    parser.add_argument("--extra", default="", help="для --backend cpp: дополнительные ключи whisper-cli, например \"-ac 256\" (короткий контекст аудио, быстрее)")
    parser.add_argument("--language", default="tt", help="код языка Whisper, для татарского tt")
    args = parser.parse_args()

    import librosa

    phrases = {row["id"]: row for row in csv.DictReader(open(args.phrases, encoding="utf-8"))}
    recognize = load_recognizer(args.model, args.backend, args.language, args.cli, args.extra)
    results = []
    for path in sorted(glob.glob(os.path.join(args.audio, "*.wav"))):
        name = os.path.splitext(os.path.basename(path))[0]
        voice, _, phrase_id = name.partition("_")
        if phrase_id not in phrases:
            print("пропуск (нет такой фразы):", name, file=sys.stderr)
            continue
        audio, _ = librosa.load(path, sr=16000)
        text = recognize(path, audio)
        rate = hit_rate(phrases[phrase_id]["tatar"], text)
        results.append((voice, phrase_id, phrases[phrase_id]["kind"], phrases[phrase_id]["tatar"], text.strip(), round(rate, 2)))
        print(f"{voice:10} {phrase_id:8} {rate:4.0%}  ожидали «{phrases[phrase_id]['tatar']}», услышали «{text.strip()}»")

    out = os.path.join(os.path.dirname(__file__), "results_" + re.sub(r"[^\w.-]+", "_", args.model) + ".csv")
    with open(out, "w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["voice", "id", "kind", "expected", "recognized", "hit_rate"])
        writer.writerows(results)
    print("\nИтог по голосам и видам:")
    for voice in sorted({r[0] for r in results}):
        for kind in ("word", "phrase"):
            rates = [r[5] for r in results if r[0] == voice and r[2] == kind]
            if rates:
                shares = " ".join(f">={t}%: {sum(1 for x in rates if x >= t / 100) / len(rates):.0%}" for t in (30, 50, 70))
                print(f"{voice:10} {kind:6} записей {len(rates):3}  среднее {statistics.mean(rates):.0%}  {shares}")
    print("Подробно:", out)


if __name__ == "__main__":
    main()
