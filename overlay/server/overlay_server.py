import asyncio
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from threading import Thread

import websockets


PROJECT_DIR = Path(__file__).resolve().parent.parent
PUBLIC_DIR = PROJECT_DIR / "public"

HTTP_PORT = 8095
WS_PORT = 8096

clients = set()


def run_http():
    class OverlayHandler(SimpleHTTPRequestHandler):

        def __init__(self, *args, **kwargs):
            super().__init__(
                *args,
                directory=str(PUBLIC_DIR),
                **kwargs
            )

        def do_GET(self):
            path = self.path.split("?", 1)[0]

            if path in ("", "/"):
                self.path = "/message/index.html"

            elif path.rstrip("/") == "/ranking":
                self.path = "/ranking/index.html"

            super().do_GET()


    server = ThreadingHTTPServer(
        ("127.0.0.1", HTTP_PORT),
        OverlayHandler
    )

    print(
        f"[OVERLAY HTTP] Principal: "
        f"http://127.0.0.1:{HTTP_PORT}/"
    )

    print(
        f"[OVERLAY HTTP] Ranking:   "
        f"http://127.0.0.1:{HTTP_PORT}/ranking"
    )

    server.serve_forever()


async def handle_client(websocket):
    clients.add(websocket)

    print(
        f"[OVERLAY WS] Client connected. "
        f"Total: {len(clients)}"
    )

    try:
        async for message in websocket:

            print(
                f"[OVERLAY WS] Received: "
                f"{message}"
            )

            dead_clients = []

            for client in list(clients):

                if client == websocket:
                    continue

                try:
                    await client.send(
                        message
                    )

                except Exception:
                    dead_clients.append(
                        client
                    )

            for client in dead_clients:
                clients.discard(
                    client
                )

    finally:
        clients.discard(
            websocket
        )

        print(
            f"[OVERLAY WS] Client disconnected. "
            f"Total: {len(clients)}"
        )


async def run_websocket():
    async with websockets.serve(
        handle_client,
        "127.0.0.1",
        WS_PORT
    ):
        print(
            f"[OVERLAY WS] "
            f"ws://127.0.0.1:{WS_PORT}/ws"
        )

        await asyncio.Future()


def main():

    http_thread = Thread(
        target=run_http,
        daemon=True
    )

    http_thread.start()

    asyncio.run(
        run_websocket()
    )


if __name__ == "__main__":
    main()
