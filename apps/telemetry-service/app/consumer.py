import asyncio
import logging

from aiokafka import AIOKafkaConsumer
from pydantic import ValidationError

from .config import Settings
from .schemas import MeasurementEvent
from .storage import Storage

log = logging.getLogger("telemetry.consumer")


def parse_event(raw: bytes) -> MeasurementEvent | None:
    try:
        return MeasurementEvent.model_validate_json(raw)
    except ValidationError as error:
        first = error.errors()[0] if error.errors() else {}
        log.warning("Measurement rejected: %s %s", first.get("loc"), first.get("msg"))
        return None


async def consume_forever(storage: Storage, settings: Settings) -> None:
    while True:
        consumer = AIOKafkaConsumer(
            settings.kafka_topic,
            bootstrap_servers=settings.kafka_bootstrap_servers,
            group_id=settings.kafka_group_id,
            auto_offset_reset="earliest",
            enable_auto_commit=True,
        )
        try:
            await consumer.start()
            log.info("Kafka: subscribed to %s", settings.kafka_topic)
            async for message in consumer:
                event = parse_event(message.value)
                if event is None:
                    continue
                if await storage.insert(event):
                    log.info("Stored measurement %s %s=%s%s", event.deviceId, event.metric, event.value, event.unit)
        except asyncio.CancelledError:
            raise
        except Exception as error:
            log.warning("Kafka unavailable (%s), retrying in %s s", error, settings.kafka_retry_seconds)
            await asyncio.sleep(settings.kafka_retry_seconds)
        finally:
            await consumer.stop()
