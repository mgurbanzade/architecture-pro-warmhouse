# Smart Home Sensor Management API

## Prerequisites

- Docker and Docker Compose

## Getting Started

### Option 1: Using Docker Compose (Recommended)

The easiest way to start the application is to use Docker Compose:

```bash
./init.sh
```

This script will:

1. Build and start PostgreSQL, Kafka, temperature-api, the monolith and the task 6 microservices (device-service, telemetry-service)
2. Wait for the services to be ready
3. Display information about how to access the API

Alternatively, you can run Docker Compose directly:

```bash
docker-compose up -d
```

The API will be available at http://localhost:8080, temperature-api at http://localhost:8081 (see `temperature-api/README.md`)

### Option 2: Manual setup

If you prefer to run the application without Docker:

1. Start the PostgreSQL database:

```bash
docker-compose up -d postgres
```

2. Build and run the application:

```bash
go build -o smarthome
./smarthome
```

## API Testing

A Postman collection is provided for testing the API. Import the `smarthome-api.postman_collection.json` file into Postman to get started.

## API Endpoints

- `GET /health` - Health check
- `GET /api/v1/sensors` - Get all sensors
- `GET /api/v1/sensors/:id` - Get a specific sensor
- `POST /api/v1/sensors` - Create a new sensor
- `PUT /api/v1/sensors/:id` - Update a sensor
- `DELETE /api/v1/sensors/:id` - Delete a sensor
- `PATCH /api/v1/sensors/:id/value` - Update a sensor's value and status
- `GET /api/v1/sensors/temperature/:location` - Get current temperature for a location (proxied to temperature-api)

## Task 6: microservices

| Service | Port | Description |
|---|---|---|
| `device-service` (Java, Spring Boot) | 8083 | Device registry and commands — see `device-service/README.md` |
| `telemetry-service` (Python, FastAPI) | 8082 | Measurements from Kafka, history, monolith-compatible `/temperature/{id}` — see `telemetry-service/README.md` |
| `kafka` | 9092 (internal) | Single-node KRaft broker; topics `telemetry.measurements`, `devices.commands` |

The monolith keeps polling `temperature-api` by default (task 5 works without the new services).
Switch it to the telemetry service with one variable:

```bash
TEMPERATURE_API_URL=http://telemetry-service:8082 docker compose up -d app
```

New sensors are mirrored into `device-service` best-effort (`DEVICE_SERVICE_URL`); if it is down, the monolith logs a warning and keeps working.
Postman folder «Task 6 - Microservices» contains requests for both services.

