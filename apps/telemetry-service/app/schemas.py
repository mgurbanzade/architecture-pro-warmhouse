import math
from datetime import datetime, timezone
from uuid import UUID

from pydantic import BaseModel, Field, field_validator


def _as_utc(value: datetime) -> datetime:
    return value if value.tzinfo is not None else value.replace(tzinfo=timezone.utc)


class MeasurementEvent(BaseModel):
    eventId: UUID
    deviceId: str = Field(min_length=1)
    metric: str = Field(min_length=1)
    value: float
    unit: str = Field(min_length=1)
    measuredAt: datetime
    location: str | None = None

    @field_validator("value")
    @classmethod
    def value_must_be_finite(cls, value: float) -> float:
        if not math.isfinite(value):
            raise ValueError("value must be a finite number")
        return value

    @field_validator("measuredAt")
    @classmethod
    def measured_at_must_be_aware(cls, value: datetime) -> datetime:
        return _as_utc(value)


class Measurement(BaseModel):
    deviceId: str
    metric: str
    value: float
    unit: str
    measuredAt: datetime
    location: str | None = None


class MeasurementPage(BaseModel):
    items: list[Measurement]
    nextCursor: str | None = None


class Aggregate(BaseModel):
    periodStart: datetime
    min: float
    max: float
    avg: float
    count: int
