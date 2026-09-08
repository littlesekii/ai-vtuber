# AI VTuber

## Pré-requisitos

- Docker (com suporte a GPU para acelerar a LLM)
- Python 3.11+
- Java 17+ e Maven
- [VNyan](https://assetstore.unity.com/packages/tools/integration/vnyan-vtuber-toolkit-226457) (para o avatar)

## 1. Instalação da LLM (Llama) via Docker

O projeto usa o [Ollama](https://ollama.com) como servidor de LLM, exposto na porta `11434`.

1. Suba o contêiner do Ollama:

   ```bash
   docker compose up -d
   ```

2. Baixe o modelo Llama dentro do contêiner:

   ```bash
   docker exec vtuber-ollama ollama pull llama3.1
   ```

   > Se preferir outro modelo (ex.: `llama3.2`, `qwen2.5`), basta trocar o nome.

3. Confirme que está no ar:

   ```bash
   curl http://127.0.0.1:11434/api/tags
   ```

## 2. Ambiente do Kokoro (TTS) em Python

1. Crie o ambiente virtual:

   ```bash
   python -m venv .venv-kokoro
   ```

2. Ative-o:

   - **Windows (PowerShell):**
     ```bash
     .\.venv-kokoro\Scripts\Activate.ps1
     ```
   - **Linux/macOS:**
     ```bash
     source .venv-kokoro/bin/activate
     ```

3. Instale as dependências:

   ```bash
   pip install -r requirements-kokoro.txt
   ```

4. Suba o servidor de TTS:

   ```bash
   python -m uvicorn tts.kokoro_server:app --host 127.0.0.1 --port 8003
   ```

5. Teste:

   ```bash
   curl http://127.0.0.1:8003/health
   ```

## 3. Overlay (Chat + Ranking)

O overlay fornece um servidor HTTP (porta `8095`) e WebSocket (porta `8096`) para exibir mensagens do chat e ranking de presentes.

```bash
python overlay/server/overlay_server.py
```

- HTTP: `http://127.0.0.1:8095`
- WebSocket: `ws://127.0.0.1:8096/ws`

Para acessar o overlay remotamente via Cloudflare Tunnel:

```bash
cloudflared tunnel --url http://localhost:8095
```

## 4. Rodando a aplicação Java

1. Compile o projeto:

   ```bash
   mvn clean package -DskipTests
   ```

2. Execute o JAR:

   ```bash
   java -jar target/ai-vtuber-1.0-SNAPSHOT.jar
   ```

## 5. Configuração

Edite `config/ai_vtuber.properties` para configurar:

- `app.tiktok.streamer-username` — usuário do TikTok para conexão
- `app.ai.model` — modelo do Ollama (padrão: `llama3.1`)
- `app.tts.url` — URL do servidor TTS (padrão: `http://127.0.0.1:8003`)
- `app.avatar.vnyan-websocket-url` — WebSocket do VNyan (padrão: `ws://127.0.0.1:1845/ws`)

## 6. Personalidade

Coloque arquivos de personalidade da IA na pasta `personality/`. O arquivo padrão é `personality/mio.txt`.

## Inicialização Rápida (Windows)

O script `run.ps1` inicia todos os serviços automaticamente:

```powershell
.\run.ps1
```
