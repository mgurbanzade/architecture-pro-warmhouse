package ru.warmhouse.devices.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.warmhouse.devices.service.DeviceRegistry;

import java.time.Instant;
import java.time.OffsetDateTime;

@Component
public class MeasurementConsumer {

    private static final Logger log = LoggerFactory.getLogger(MeasurementConsumer.class);

    private final DeviceRegistry registry;
    private final ObjectMapper mapper;

    public MeasurementConsumer(DeviceRegistry registry, ObjectMapper mapper) {
        this.registry = registry;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "${app.topics.measurements}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMeasurement(String payload) {
        try {
            JsonNode node = mapper.readTree(payload);
            String deviceId = node.path("deviceId").asText(null);
            if (deviceId == null || deviceId.isBlank()) {
                log.warn("Measurement without deviceId skipped");
                return;
            }
            Instant at = node.hasNonNull("measuredAt")
                    ? OffsetDateTime.parse(node.get("measuredAt").asText()).toInstant()
                    : Instant.now();
            if (registry.markSeen(deviceId, at)) {
                log.debug("Device {} is online ({})", deviceId, at);
            }
        } catch (Exception e) {
            log.warn("Measurement could not be parsed: {}", e.getMessage());
        }
    }
}
