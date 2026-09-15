import json
import os
import random
import threading
import time
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse
from uuid import uuid4

DEFAULT_PORT = 8081
TEMP_MIN, TEMP_MAX = 18.0, 26.0

LOCATION_BY_SENSOR_ID = {"1": "Living Room", "2": "Bedroom", "3": "Kitchen"}
SENSOR_ID_BY_LOCATION = {location: sensor_id for sensor_id, location in LOCATION_BY_SENSOR_ID.items()}

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC", "telemetry.measurements")
PUBLISH_INTERVAL_SECONDS = float(os.getenv("PUBLISH_INTERVAL_SECONDS", "5"))
PUBLISH_SENSOR_IDS = [s for s in os.getenv("PUBLISH_SENSOR_IDS", "1,2,3").split(",") if s]
DEVICE_SERIAL_PREFIX = os.getenv("DEVICE_SERIAL_PREFIX", "monolith-sensor-")


def resolve(location: str, sensor_id: str) -> tuple[str, str]:
    if not location:
        location = LOCATION_BY_SENSOR_ID.get(sensor_id, "Unknown")
    if not sensor_id:
        sensor_id = SENSOR_ID_BY_LOCATION.get(location, "0")
    return location, sensor_id


def build_reading(location: str, sensor_id: str) -> dict:
    value = round(random.uniform(TEMP_MIN, TEMP_MAX), 1)
    return {
        "value": value,
        "unit": "°C",
        "timestamp": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "location": location,
        "status": "active",
        "sensor_id": sensor_id,
        "sensor_type": "temperature",
        "description": f"Temperature in {location}: {value}°C",
    }


def measurement_event(reading: dict) -> dict:
    return {
        "eventId": str(uuid4()),
        "deviceId": f"{DEVICE_SERIAL_PREFIX}{reading['sensor_id']}",
        "metric": "temperature",
        "value": reading["value"],
        "unit": reading["unit"],
        "measuredAt": reading["timestamp"],
        "location": reading["location"],
    }


class Handler(BaseHTTPRequestHandler):
    def do_GET(self) -> None:
        url = urlparse(self.path)

        if url.path == "/health":
            self._send_json(200, {"status": "ok"})
            return

        if url.path == "/temperature":
            query = parse_qs(url.query)
            location = query.get("location", [""])[0]
            sensor_id = query.get("sensorId", [""])[0]
        elif url.path.startswith("/temperature/"):
            sensor_id = url.path[len("/temperature/"):].strip("/")
            location = ""
            if not sensor_id:
                self._send_json(404, {"error": "sensor id is required"})
                return
        else:
            self._send_json(404, {"error": "not found"})
            return

        location, sensor_id = resolve(location, sensor_id)
        self._send_json(200, build_reading(location, sensor_id))

    def _send_json(self, status: int, body: dict) -> None:
        payload = json.dumps(body, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(payload)))
        self.end_headers()
        self.wfile.write(payload)

    def log_message(self, format: str, *args) -> None:
        print(f"{self.address_string()} - {format % args}", flush=True)


class MeasurementPublisher(threading.Thread):
    def __init__(self) -> None:
        super().__init__(name="measurement-publisher", daemon=True)
        self._producer = None

    def run(self) -> None:
        while True:
            if self._producer is None:
                self._producer = self._connect()
            if self._producer is not None:
                self._publish_round()
            time.sleep(PUBLISH_INTERVAL_SECONDS)

    def _connect(self):
        try:
            from kafka import KafkaProducer

            producer = KafkaProducer(bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS, api_version_auto_timeout_ms=5000)
            print(f"kafka: connected to {KAFKA_BOOTSTRAP_SERVERS}, topic {KAFKA_TOPIC}", flush=True)
            return producer
        except Exception as error:
            print(f"kafka: unavailable ({error}), retry in {PUBLISH_INTERVAL_SECONDS}s", flush=True)
            return None

    def _publish_round(self) -> None:
        try:
            for sensor_id in PUBLISH_SENSOR_IDS:
                location, sensor_id = resolve("", sensor_id)
                event = measurement_event(build_reading(location, sensor_id))
                self._producer.send(KAFKA_TOPIC, key=event["deviceId"].encode(), value=json.dumps(event).encode())
            self._producer.flush(timeout=5)
        except Exception as error:
            print(f"kafka: publish failed ({error}), reconnecting", flush=True)
            self._producer = None


def main() -> None:
    port = int(os.getenv("PORT", DEFAULT_PORT))
    if KAFKA_BOOTSTRAP_SERVERS:
        MeasurementPublisher().start()
    else:
        print("kafka: KAFKA_BOOTSTRAP_SERVERS not set, publishing disabled", flush=True)
    server = ThreadingHTTPServer(("0.0.0.0", port), Handler)
    print(f"temperature-api listening on :{port}", flush=True)
    server.serve_forever()

if __name__ == "__main__":
    main()
