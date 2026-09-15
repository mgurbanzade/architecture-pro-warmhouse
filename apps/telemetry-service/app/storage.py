from datetime import datetime

import asyncpg

from .schemas import Aggregate, Measurement, MeasurementEvent

SCHEMA = """
CREATE TABLE IF NOT EXISTS measurements (
    id          bigserial PRIMARY KEY,
    event_id    uuid NOT NULL UNIQUE,
    device_id   text NOT NULL,
    metric      text NOT NULL,
    value       double precision NOT NULL,
    unit        text NOT NULL,
    measured_at timestamptz NOT NULL,
    location    text
);
CREATE INDEX IF NOT EXISTS idx_measurements_device_metric_time
    ON measurements (device_id, metric, measured_at DESC);
"""

COLUMNS = "device_id, metric, value, unit, measured_at, location"


def _to_measurement(row: asyncpg.Record) -> Measurement:
    return Measurement(
        deviceId=row["device_id"],
        metric=row["metric"],
        value=row["value"],
        unit=row["unit"],
        measuredAt=row["measured_at"],
        location=row["location"],
    )


class Storage:
    def __init__(self, dsn: str) -> None:
        self._dsn = dsn
        self._pool: asyncpg.Pool | None = None

    async def connect(self) -> None:
        self._pool = await asyncpg.create_pool(self._dsn, min_size=1, max_size=5)
        async with self._pool.acquire() as conn:
            await conn.execute(SCHEMA)

    async def close(self) -> None:
        if self._pool is not None:
            await self._pool.close()

    async def insert(self, event: MeasurementEvent) -> bool:
        row = await self._pool.fetchrow(
            f"INSERT INTO measurements (event_id, {COLUMNS}) VALUES ($1, $2, $3, $4, $5, $6, $7) "
            "ON CONFLICT (event_id) DO NOTHING RETURNING id",
            event.eventId, event.deviceId, event.metric, event.value, event.unit, event.measuredAt, event.location,
        )
        return row is not None

    async def latest(self, device_id: str) -> list[Measurement]:
        rows = await self._pool.fetch(
            f"SELECT DISTINCT ON (metric) {COLUMNS} FROM measurements "
            "WHERE device_id = $1 ORDER BY metric, measured_at DESC",
            device_id,
        )
        return [_to_measurement(r) for r in rows]

    async def history(self, device_id: str, metric: str, since: datetime, until: datetime, limit: int) -> list[Measurement]:
        rows = await self._pool.fetch(
            f"SELECT {COLUMNS} FROM measurements "
            "WHERE device_id = $1 AND metric = $2 AND measured_at BETWEEN $3 AND $4 "
            "ORDER BY measured_at DESC LIMIT $5",
            device_id, metric, since, until, limit,
        )
        return [_to_measurement(r) for r in rows]

    async def aggregate(self, device_id: str, metric: str, period: str, since: datetime, until: datetime) -> list[Aggregate]:
        rows = await self._pool.fetch(
            "SELECT date_trunc($3::text, measured_at) AS period_start, "
            "min(value) AS min, max(value) AS max, avg(value) AS avg, count(*) AS count "
            "FROM measurements WHERE device_id = $1 AND metric = $2 AND measured_at BETWEEN $4 AND $5 "
            "GROUP BY 1 ORDER BY 1",
            device_id, metric, period, since, until,
        )
        return [Aggregate(periodStart=r["period_start"], min=r["min"], max=r["max"], avg=r["avg"], count=r["count"]) for r in rows]
