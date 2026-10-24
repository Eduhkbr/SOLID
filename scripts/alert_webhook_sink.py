#!/usr/bin/env python3
"""Receiver descartável para inspecionar notificações do Alertmanager em aula."""

import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from threading import Lock


OUTPUT = Path("/data/notifications.jsonl")
LOCK = Lock()


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):  # noqa: N802 - método exigido pela biblioteca padrão
        if self.path != "/notifications":
            self.send_error(404)
            return
        try:
            notifications = [json.loads(line) for line in OUTPUT.read_text(encoding="utf-8").splitlines()]
        except FileNotFoundError:
            notifications = []
        body = json.dumps(notifications, ensure_ascii=False).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):  # noqa: N802 - método exigido pela biblioteca padrão
        if self.path != "/notifications":
            self.send_error(404)
            return
        length = int(self.headers.get("Content-Length", "0"))
        try:
            notification = json.loads(self.rfile.read(length))
        except (json.JSONDecodeError, UnicodeDecodeError):
            self.send_error(400, "JSON inválido")
            return
        with LOCK:
            OUTPUT.parent.mkdir(parents=True, exist_ok=True)
            with OUTPUT.open("a", encoding="utf-8") as output:
                output.write(json.dumps(notification, ensure_ascii=False) + "\n")
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b"ok")

    def log_message(self, _format, *_args):
        return


if __name__ == "__main__":
    ThreadingHTTPServer(("0.0.0.0", 8080), Handler).serve_forever()
