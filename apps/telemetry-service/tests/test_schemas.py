import json

from app.consumer import parse_event


def _event(**overrides) -> bytes:
    event = {
        "eventId": "3c2c1c1a-1111-4111-8111-111111111111",
        "deviceId": "monolith-sensor-1",
        "metric": "temperature",
        "value": 21.4,
        "unit": "°C",
        "measuredAt": "2026-09-14T19:58:22+00:00",
        "location": "Living Room",
    }
    event.update(overrides)
    return json.dumps(event).encode()


def test_parses_valid_event_and_keeps_timezone():
    event = parse_event(_event())

    assert event is not None
    assert event.deviceId == "monolith-sensor-1"
    assert event.measuredAt.tzinfo is not None


def test_naive_timestamp_is_treated_as_utc():
    event = parse_event(_event(measuredAt="2026-09-14T19:58:22"))

    assert event is not None
    assert event.measuredAt.utcoffset().total_seconds() == 0


def test_rejects_event_without_device_id():
    assert parse_event(_event(deviceId="")) is None


def test_rejects_non_finite_value():
    assert parse_event(_event(value="NaN")) is None


def test_rejects_malformed_json():
    assert parse_event(b"{not json") is None
