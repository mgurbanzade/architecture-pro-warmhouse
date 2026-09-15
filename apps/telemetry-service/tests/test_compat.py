from datetime import datetime, timezone

from app.compat import serial_for_sensor, to_monolith_response
from app.schemas import Measurement


def _measurement(location=None) -> Measurement:
    return Measurement(
        deviceId="monolith-sensor-2",
        metric="temperature",
        value=22.5,
        unit="°C",
        measuredAt=datetime(2026, 9, 14, 19, 58, 22, tzinfo=timezone.utc),
        location=location,
    )


def test_serial_uses_prefix_and_sensor_id():
    assert serial_for_sensor("7", "monolith-sensor-") == "monolith-sensor-7"


def test_response_matches_temperature_api_contract():
    response = to_monolith_response(_measurement(location="Bedroom"), "2")

    assert response == {
        "value": 22.5,
        "unit": "°C",
        "timestamp": "2026-09-14T19:58:22+00:00",
        "location": "Bedroom",
        "status": "active",
        "sensor_id": "2",
        "sensor_type": "temperature",
        "description": "Temperature in Bedroom: 22.5°C",
    }


def test_location_falls_back_to_sensor_table_when_missing():
    assert to_monolith_response(_measurement(), "3")["location"] == "Kitchen"
    assert to_monolith_response(_measurement(), "9")["location"] == "Unknown"
