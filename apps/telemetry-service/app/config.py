import os
from dataclasses import dataclass


@dataclass(frozen=True)
class Settings:
    database_url: str = os.getenv("DATABASE_URL", "postgresql://postgres:postgres@localhost:5432/telemetry")
    kafka_bootstrap_servers: str = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "")
    kafka_topic: str = os.getenv("KAFKA_TOPIC", "telemetry.measurements")
    kafka_group_id: str = os.getenv("KAFKA_GROUP_ID", "telemetry-service")
    kafka_retry_seconds: float = float(os.getenv("KAFKA_RETRY_SECONDS", "5"))
    monolith_serial_prefix: str = os.getenv("MONOLITH_SERIAL_PREFIX", "monolith-sensor-")
    port: int = int(os.getenv("PORT", "8082"))

settings = Settings()
