import os
import re
import uuid
import subprocess

from fastapi import FastAPI
from pydantic import BaseModel


app = FastAPI()

PIPER_PYTHON = ".venv-tts/Scripts/python.exe"
MODEL = "tts/models/pt_BR-cadu-medium.onnx"
OUTPUT_DIR = "tts/generated"

os.makedirs(OUTPUT_DIR, exist_ok=True)


class TTSRequest(BaseModel):
    text: str
    emotion: str = "EXPRESSIVE"


def clean_text(text: str) -> str:
    text = re.sub(
        r"[^\w\sÀ-ÿ.,!?;:'\"()-]",
        "",
        text
    )

    return text.strip()


@app.post("/tts")
def generate_tts(request: TTSRequest):

    text = clean_text(request.text)

    if not text:
        raise RuntimeError("Texto vazio após limpeza.")

    filename = f"{uuid.uuid4()}.wav"

    output_path = os.path.join(
        OUTPUT_DIR,
        filename
    )

    command = [
        PIPER_PYTHON,
        "-m",
        "piper",
        "--model",
        MODEL,
        "--output_file",
        output_path
    ]

    print(f"[TTS] Texto: {text}")
    print(f"[TTS] Emotion: {request.emotion}")
    print(f"[TTS] Gerando: {output_path}")

    process = subprocess.run(
        command,
        input=text,
        text=True,
        encoding="utf-8",
        errors="replace",
        capture_output=True
    )

    if process.returncode != 0:
        raise RuntimeError(
            "Piper error: " + process.stderr
        )

    if not os.path.exists(output_path):
        raise RuntimeError(
            "Piper terminou sem gerar o arquivo WAV."
        )

    print(f"[TTS] Gerado: {output_path}")

    return {
        "file": output_path,
        "emotion": request.emotion.upper()
    }


@app.get("/health")
def health():
    return {
        "status": "ok",
        "engine": "piper",
        "model": MODEL
    }