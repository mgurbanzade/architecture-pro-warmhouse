# telemetry-service

Сервис телеметрии «Тёплого дома» (MVP, задание 6). Python 3.12, FastAPI, PostgreSQL, Kafka.

## Что делает

- Читает показания **только** из топика `telemetry.measurements` (формат — [events-asyncapi.yaml](../../schemas/api/events-asyncapi.yaml)),
  валидирует их (Pydantic), отбрасывает дубли по `eventId`, хранит в PostgreSQL.
- Отдаёт последние значения, историю и агрегаты по устройству —
  [telemetry-openapi.yaml](../../schemas/api/telemetry-openapi.yaml).
- **Совместимость с монолитом:** `GET /temperature/{sensorId}` и `GET /temperature?location=…` отвечают в формате
  `temperature-api`, поэтому монолит переключается на этот сервис одной переменной `TEMPERATURE_API_URL`.

## API

- `GET /health`
- `GET /telemetry/{deviceId}/latest`
- `GET /telemetry/{deviceId}?metric=&from=&to=&limit=`
- `GET /telemetry/{deviceId}/aggregate?metric=&period=hour|day&from=&to=`
- `GET /temperature/{sensorId}`, `GET /temperature?location=&sensorId=` — совместимость с монолитом

## Конфигурация

| Переменная | По умолчанию |
|---|---|
| `PORT` | `8082` |
| `DATABASE_URL` | `postgresql://postgres:postgres@localhost:5432/telemetry` |
| `KAFKA_BOOTSTRAP_SERVERS` | пусто — приём показаний выключен |
| `KAFKA_TOPIC` | `telemetry.measurements` |
| `MONOLITH_SERIAL_PREFIX` | `monolith-sensor-` |

Схема БД создаётся при старте. Недоступность Kafka не мешает запуску: подписка повторяется каждые 5 с.

## Запуск

```bash
pip install -r requirements.txt && python -m app.main
```

Тесты: `python -m pytest tests`. Через Docker Compose из каталога `apps`: `docker compose up --build telemetry-service` (тесты выполняются при сборке).
