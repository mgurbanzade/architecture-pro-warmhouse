# device-service

Сервис устройств «Тёплого дома» (MVP, задание 6). Java 21, Spring Boot 3, PostgreSQL, Kafka.

## Что делает

- Реестр устройств: регистрация по серийному номеру и типу, список по дому, удаление.
- Команды устройствам: проверка по возможностям типа (`heating`: `turn_on`/`turn_off`, `gate`: `open`/`close`, …),
  сохранение со статусом `pending`, публикация в топик `devices.commands` для шлюза устройств.
- Читает показания из топика `telemetry.measurements` и отмечает устройство как `online` (`lastSeenAt`).

Идентификатор устройства в событиях MVP — серийный номер. Датчики монолита регистрируются
с серийным номером `monolith-sensor-{id}` в доме `00000000-0000-0000-0000-000000000001`.

## API

Соответствует [schemas/api/devices-openapi.yaml](../../schemas/api/devices-openapi.yaml):

- `GET /device-types`
- `GET /devices?houseId=…`, `POST /devices`
- `GET /devices/{id}`, `DELETE /devices/{id}`
- `POST /devices/{id}/commands` → `202`

## Конфигурация

| Переменная | По умолчанию |
|---|---|
| `SERVER_PORT` | `8083` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/devices` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `postgres` / `postgres` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |

Схема БД создаётся при старте (`ddl-auto: update`). Недоступность Kafka не мешает запуску и REST API.

## Запуск

```bash
mvn spring-boot:run
```

Через Docker Compose из каталога `apps`: `docker compose up --build device-service`. Тесты выполняются при сборке образа.
