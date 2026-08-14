# AI VTuber

## Pré-requisitos

- Docker (com suporte a GPU para acelerar a LLM)
- Python 3.11+
- Java 17+ e Maven

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

## 3. Rodando a aplicação Java

Com o Ollama e o Kokoro no ar:

```bash
mvn spring-boot:run
```
