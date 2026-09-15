package ru.warmhouse.devices.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices", uniqueConstraints = @UniqueConstraint(columnNames = "serial_number"))
public class Device {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "house_id", nullable = false)
    private UUID houseId;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "type_code", nullable = false)
    private String typeCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status = DeviceStatus.OFFLINE;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Device() {
    }

    public static Device register(UUID houseId, UUID roomId, String typeCode, String serialNumber, String name) {
        Device device = new Device();
        device.houseId = houseId;
        device.roomId = roomId;
        device.typeCode = typeCode;
        device.serialNumber = serialNumber;
        device.name = name;
        return device;
    }

    public void markSeen(Instant at) {
        this.status = DeviceStatus.ONLINE;
        this.lastSeenAt = at;
    }

    public UUID getId() { return id; }
    public UUID getHouseId() { return houseId; }
    public UUID getRoomId() { return roomId; }
    public String getTypeCode() { return typeCode; }
    public String getName() { return name; }
    public String getSerialNumber() { return serialNumber; }
    public DeviceStatus getStatus() { return status; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public Instant getCreatedAt() { return createdAt; }
}
