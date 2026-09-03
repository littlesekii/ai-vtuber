# Mío Overlay

Estrutura organizada por responsabilidade.

```text
mio-overlay-organized/
│
├── server/
│   └── overlay_server.py
│
├── public/
│   ├── message/
│   │   ├── index.html
│   │   └── app.js
│   │
│   ├── ranking/
│   │   ├── index.html
│   │   └── ranking.js
│   │
│   └── shared/
│       ├── style.css
│       └── assets/
│           └── default-avatar.svg
│
└── README.md
```

## Rodar

Na raiz do projeto:

```cmd
python server/overlay_server.py
```

## Overlays

Principal / mensagem:

```text
http://127.0.0.1:8085/
```

Ranking:

```text
http://127.0.0.1:8085/ranking
```

WebSocket local:

```text
ws://127.0.0.1:8086/ws
```

## Cloudflare Tunnel

Use o mesmo hostname.

```yaml
ingress:
  - hostname: overlay.seudominio.com
    path: /ws
    service: http://127.0.0.1:8086

  - hostname: overlay.seudominio.com
    service: http://127.0.0.1:8085

  - service: http_status:404
```

Então:

```text
https://overlay.seudominio.com/
https://overlay.seudominio.com/ranking
wss://overlay.seudominio.com/ws
```


## Último presente

O `/ranking` agora alterna automaticamente:

```text
Ranking de likes: 8 segundos
Último presente: 4 segundos
```

Se ainda não tiver recebido nenhum presente, o ranking fica visível
continuamente.

Evento recomendado:

```json
{
  "type": "LAST_GIFT",
  "data": {
    "username": "Davi",
    "profileImageUrl": "https://exemplo.com/avatar.jpg",
    "giftName": "Rosa",
    "giftImageUrl": "https://exemplo.com/rosa.png",
    "amount": 3
  }
}
```

Também é aceito `type: "GIFT"` para compatibilidade.

Campos:

- `username`: nome do presenteador
- `profileImageUrl`: foto do presenteador
- `giftName`: nome do presente
- `giftImageUrl`: URL da imagem do presente
- `amount`: quantidade enviada
