import os
import uuid

import torch
import torchaudio
from fastapi import FastAPI
from pydantic import BaseModel

from chatterbox.mtl_tts import ChatterboxMultilingualTTS


app = FastAPI()

device = "cuda" if torch.cuda.is_available() else "cpu"

print("Device:", device)
print("Carregando Chatterbox...")

model = ChatterboxMultilingualTTS.from_pretrained(
    device=device
)

print("Chatterbox carregado!")
OUTPUT_DIR = "tts/generated"

os.makedirs(OUTPUT_DIR, exist_ok=True)


class TTSRequest(BaseModel):
    text: str
    emotion: str = "EXPRESSIVE"


VOICE_PRESETS = {
    "CALM": {
        "exaggeration": 0.50,
        "cfg_weight": 0.50
    },
    "NATURAL": {
        "exaggeration": 0.60,
        "cfg_weight": 0.40
    },
    "EXPRESSIVE": {
        "exaggeration": 0.75,
        "cfg_weight": 0.35
    },
    "EXCITED": {
        "exaggeration": 0.85,
        "cfg_weight": 0.25
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

    audio = model.generate(
        request.text,
        language_id="pt",
        exaggeration=preset["exaggeration"],
        cfg_weight=preset["cfg_weight"]
    )

    torchaudio.save(
        output_path,
        audio,
        model.sr
    )

    return {
        "file": output_path,
        "emotion": request.emotion.upper(),
        "exaggeration": preset["exaggeration"],
        "cfg_weight": preset["cfg_weight"]
    }