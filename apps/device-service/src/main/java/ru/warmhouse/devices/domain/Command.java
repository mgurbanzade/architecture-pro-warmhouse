package ru.warmhouse.devices.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "commands")
public class Command {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(nullable = false)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommandStatus status = CommandStatus.PENDING;

    @Column(name = "issued_by", nullable = false)
    private String issuedBy;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    protected Command() {
    }

    public static Command issue(UUID deviceId, String type, Map<String, Object> payload, String issuedBy) {
        Command command = new Command();
        command.deviceId = deviceId;
        command.type = type;
        command.payload = payload == null ? Map.of() : payload;
        command.issuedBy = issuedBy;
        return command;
    }

    public UUID getId() { return id; }
    public UUID getDeviceId() { return deviceId; }
    public String getType() { return type; }
    public Map<String, Object> getPayload() { return payload; }
    public CommandStatus getStatus() { return status; }
    public String getIssuedBy() { return issuedBy; }
    public Instant getIssuedAt() { return issuedAt; }
}
