# temperature-api

Заглушка датчика температуры для монолита `smart_home`. Python 3.12; единственная зависимость — `kafka-python` для задания 6.

## Эндпоинты

- `GET /health` — проверка работоспособности.
- `GET /temperature?location=<room>&sensorId=<id>` — случайная температура; если передан только один параметр, второй подбирается по таблице `1 = Living Room, 2 = Bedroom, 3 = Kitchen`.
- `GET /temperature/{sensorId}` — то же по идентификатору датчика (этот путь вызывает монолит).

Ответ:

```json
{
  "value": 21.7, "unit": "°C", "timestamp": "2025-01-01T10:00:00+00:00",
  "location": "Living Room", "status": "active", "sensor_id": "1",
  "sensor_type": "temperature", "description": "Temperature in Living Room: 21.7°C"
}
```

## Публикация в Kafka (задание 6)

Если задана `KAFKA_BOOTSTRAP_SERVERS`, фоновый поток раз в `PUBLISH_INTERVAL_SECONDS` (по умолчанию 5)
публикует показания датчиков `PUBLISH_SENSOR_IDS` (по умолчанию `1,2,3`) в топик `KAFKA_TOPIC`
(`telemetry.measurements`) с `deviceId = monolith-sensor-{id}` — так устройство «само присылает» телеметрию.
HTTP-часть от брокера не зависит.

## Запуск

Порт задаётся переменной `PORT` (по умолчанию `8081`).

```bash
python app.py
```

Через Docker Compose из каталога `apps`: `docker compose up --build temperature-api`.
