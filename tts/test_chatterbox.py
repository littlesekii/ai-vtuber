import torch
import torchaudio

from chatterbox.mtl_tts import ChatterboxMultilingualTTS

device = "cuda" if torch.cuda.is_available() else "cpu"

print("Device:", device)

model = ChatterboxMultilingualTTS.from_pretrained(
    device=device
)

text = "Olá! Eu sou a Sakura. Que bom ter você aqui comigo na live!"

audio = model.generate(
    text,
    language_id="pt",
    audio_prompt_path="tts/references/sakura_voice.wav"
)

torchaudio.save(
    "tts/sakura_chatterbox_feminina.wav",
    audio,
    model.sr
)

print("Áudio gerado!")