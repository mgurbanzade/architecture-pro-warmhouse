from .schemas import Measurement

LOCATION_BY_SENSOR_ID = {"1": "Living Room", "2": "Bedroom", "3": "Kitchen"}
SENSOR_ID_BY_LOCATION = {location: sensor_id for sensor_id, location in LOCATION_BY_SENSOR_ID.items()}


def serial_for_sensor(sensor_id: str, prefix: str) -> str:
    return f"{prefix}{sensor_id}"


def to_monolith_response(measurement: Measurement, sensor_id: str) -> dict:
    location = measurement.location or LOCATION_BY_SENSOR_ID.get(sensor_id, "Unknown")
    return {
        "value": measurement.value,
        "unit": measurement.unit,
        "timestamp": measurement.measuredAt.isoformat(timespec="seconds"),
        "location": location,
        "status": "active",
        "sensor_id": sensor_id,
        "sensor_type": "temperature",
        "description": f"Temperature in {location}: {measurement.value}{measurement.unit}",
    }
