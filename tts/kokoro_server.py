import os
import uuid

import numpy as np
import soundfile as sf
from fastapi import FastAPI
from pydantic import BaseModel

from kokoro import KPipeline


app = FastAPI()

OUTPUT_DIR = "tts/generated"

VOICE_MODE = "FEMALE"
# VOICE_MODE = "MALE"

os.makedirs(OUTPUT_DIR, exist_ok=True)


print("Carregando Kokoro...")

pipeline = KPipeline(
    lang_code="p"
)

print("Carregando vozes...")


if VOICE_MODE == "FEMALE":

    dora = pipeline.load_voice("pf_dora")
    bella = pipeline.load_voice("af_bella")

    voice = (
        dora * 0.80
        +
        bella * 0.20
    )

    voice_name = "dora_bella_80_20"

elif VOICE_MODE == "MALE":

    voice = pipeline.load_voice("pm_santa")

    voice_name = "pm_santa"

else:
    raise RuntimeError(
        "VOICE_MODE inválido. Use FEMALE ou MALE."
    )


print(f"Voz carregada: {voice_name}")
print("Kokoro pronto!")


class TTSRequest(BaseModel):
    text: str
    emotion: str = "EXPRESSIVE"


VOICE_PRESETS = {
    "CALM": {
        "speed": 0.92
    },
    "NATURAL": {
        "speed": 1.00
    },
    "EXPRESSIVE": {
        "speed": 1.05
    },
    "EXCITED": {
        "speed": 1.12
    }
}


@app.post("/tts")
def generate_tts(request: TTSRequest):

    preset = VOICE_PRESETS.get(
        request.emotion.upper(),
        VOICE_PRESETS["EXPRESSIVE"]
    )

    filename = f"{uuid.uuid4()}.wav"

    output_path = os.path.join(
        OUTPUT_DIR,
        filename
    )

    print()
    print(f"[TTS] Texto: {request.text}")
    print(f"[TTS] Emotion: {request.emotion}")
    print(f"[TTS] Speed: {preset['speed']}")
    print(f"[TTS] Voice: {voice_name}")

    generator = pipeline(
        request.text,
        voice=voice,
        speed=preset["speed"]
    )

    audios = []

    for _, _, audio in generator:
        audios.append(audio)

    if not audios:
        raise RuntimeError(
            "Kokoro não gerou áudio."
        )

    final_audio = np.concatenate(
        audios
    )

    sf.write(
        output_path,
        final_audio,
        24000,
        subtype="PCM_16"
    )

    print(f"[TTS] Gerado: {output_path}")

    return {
        "file": output_path,
        "emotion": request.emotion.upper(),
        "voice": voice_name,
        "speed": preset["speed"]
    }


@app.get("/health")
def health():

    return {
        "status": "ok",
        "engine": "kokoro",
        "voice": voice_name,
        "voice_mode": VOICE_MODE,
        "language": "pt-BR"
    }