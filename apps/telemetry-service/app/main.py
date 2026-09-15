import asyncio
import logging
from contextlib import asynccontextmanager, suppress
from datetime import datetime, timezone
from typing import Literal

import uvicorn
from fastapi import FastAPI, HTTPException, Query

from .compat import SENSOR_ID_BY_LOCATION, serial_for_sensor, to_monolith_response
from .config import settings
from .consumer import consume_forever
from .schemas import Aggregate, Measurement, MeasurementPage
from .storage import Storage

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
log = logging.getLogger("telemetry")

storage = Storage(settings.database_url)


@asynccontextmanager
async def lifespan(_: FastAPI):
    await storage.connect()
    task = None
    if settings.kafka_bootstrap_servers:
        task = asyncio.create_task(consume_forever(storage, settings))
    else:
        log.warning("KAFKA_BOOTSTRAP_SERVERS is not set, measurement intake is disabled")
    yield
    if task is not None:
        task.cancel()
        with suppress(asyncio.CancelledError):
            await task
    await storage.close()

app = FastAPI(title="Telemetry Service", version="1.0.0", lifespan=lifespan)


def _utc(value: datetime) -> datetime:
    return value if value.tzinfo is not None else value.replace(tzinfo=timezone.utc)


def _not_found(message: str) -> HTTPException:
    return HTTPException(status_code=404, detail={"code": "no_data", "message": message})


@app.get("/health")
async def health() -> dict:
    return {"status": "ok"}


@app.get("/telemetry/{device_id}/latest", response_model=list[Measurement])
async def latest(device_id: str) -> list[Measurement]:
    items = await storage.latest(device_id)
    if not items:
        raise _not_found(f"No measurements for device {device_id}")
    return items


@app.get("/telemetry/{device_id}/aggregate", response_model=list[Aggregate])
async def aggregate(
    device_id: str,
    metric: str,
    period: Literal["hour", "day"],
    since: datetime = Query(alias="from"),
    until: datetime = Query(alias="to"),
) -> list[Aggregate]:
    return await storage.aggregate(device_id, metric, period, _utc(since), _utc(until))


@app.get("/telemetry/{device_id}", response_model=MeasurementPage)
async def history(
    device_id: str,
    metric: str,
    since: datetime = Query(alias="from"),
    until: datetime = Query(alias="to"),
    limit: int = Query(1000, ge=1, le=10000),
) -> MeasurementPage:
    return MeasurementPage(items=await storage.history(device_id, metric, _utc(since), _utc(until), limit))


async def _monolith_reading(sensor_id: str) -> dict:
    serial = serial_for_sensor(sensor_id, settings.monolith_serial_prefix)
    readings = [m for m in await storage.latest(serial) if m.metric == "temperature"]
    if not readings:
        raise _not_found(f"No measurements for sensor {sensor_id} yet")
    return to_monolith_response(readings[0], sensor_id)


@app.get("/temperature/{sensor_id}")
async def temperature_by_sensor(sensor_id: str) -> dict:
    return await _monolith_reading(sensor_id)


@app.get("/temperature")
async def temperature_by_query(location: str = "", sensorId: str = "") -> dict:
    sensor_id = sensorId or SENSOR_ID_BY_LOCATION.get(location, "")
    if not sensor_id:
        raise _not_found(f"Location {location!r} is not linked to a sensor")
    return await _monolith_reading(sensor_id)


def main() -> None:
    uvicorn.run("app.main:app", host="0.0.0.0", port=settings.port)

if __name__ == "__main__":
    main()
