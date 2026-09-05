# run.ps1 - AI VTuber: uma aba por servico (Windows Terminal)
$PROJECT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path

$JAVA_EXE = "C:\devenv\JDK\25.0_Zulu\bin\java.exe"

function Start-ServiceTab([string]$Title, [string]$CommandLine) {
    & wt.exe -w 0 new-tab -d "$PROJECT_DIR" --title "$Title" cmd /k $CommandLine
}

Start-ServiceTab "AI VTuber - Ollama"      "ollama serve"
Start-ServiceTab "AI VTuber - Kokoro TTS"  ".venv-kokoro\Scripts\python.exe -m uvicorn tts.kokoro_server:app --host 127.0.0.1 --port 8003"
Start-ServiceTab "AI VTuber - Overlay"     "python overlay/server/overlay_server.py"
Start-ServiceTab "AI VTuber - Cloudflared" "cloudflared tunnel --url localhost:8095"