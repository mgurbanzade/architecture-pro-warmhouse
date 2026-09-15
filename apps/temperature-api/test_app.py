from app import LOCATION_BY_SENSOR_ID, build_reading, measurement_event, resolve


def test_resolve_fills_location_from_sensor_id():
    assert resolve("", "2") == ("Bedroom", "2")


def test_resolve_fills_sensor_id_from_location():
    assert resolve("Kitchen", "") == ("Kitchen", "3")


def test_resolve_unknown_pairs_use_defaults():
    assert resolve("", "42") == ("Unknown", "42")
    assert resolve("Garage", "") == ("Garage", "0")


def test_reading_is_within_range_and_has_monolith_fields():
    reading = build_reading("Living Room", "1")

    assert 18.0 <= reading["value"] <= 26.0
    assert set(reading) == {"value", "unit", "timestamp", "location", "status", "sensor_id", "sensor_type", "description"}


def test_measurement_event_uses_serial_and_common_format():
    event = measurement_event(build_reading(LOCATION_BY_SENSOR_ID["1"], "1"))

    assert event["deviceId"] == "monolith-sensor-1"
    assert event["metric"] == "temperature"
    assert set(event) == {"eventId", "deviceId", "metric", "value", "unit", "measuredAt", "location"}
