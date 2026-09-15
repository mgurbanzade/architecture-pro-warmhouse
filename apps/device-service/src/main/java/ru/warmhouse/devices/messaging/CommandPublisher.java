package ru.warmhouse.devices.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.warmhouse.devices.domain.Command;

import java.util.Map;

@Component
public class CommandPublisher {

    private static final Logger log = LoggerFactory.getLogger(CommandPublisher.class);

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final String topic;

    public CommandPublisher(KafkaTemplate<String, String> kafka, ObjectMapper mapper,
                            @Value("${app.topics.commands}") String topic) {
        this.kafka = kafka;
        this.mapper = mapper;
        this.topic = topic;
    }

    public void publish(Command command, String deviceSerial) {
        try {
            String json = mapper.writeValueAsString(Map.of(
                    "commandId", command.getId(),
                    "deviceId", command.getDeviceId(),
                    "deviceSerial", deviceSerial,
                    "type", command.getType(),
                    "payload", command.getPayload(),
                    "issuedAt", command.getIssuedAt()));
            kafka.send(topic, deviceSerial, json).whenComplete((result, error) -> {
                if (error != null) {
                    log.warn("Command {} was not published to {}: {}", command.getId(), topic, error.getMessage());
                } else {
                    log.info("Command {} published to {}", command.getId(), topic);
                }
            });
        } catch (Exception e) {
            log.warn("Command {} was not published to {}: {}", command.getId(), topic, e.getMessage());
        }
    }
}
