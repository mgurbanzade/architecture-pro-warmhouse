package ru.warmhouse.devices.api.dto;

import ru.warmhouse.devices.domain.Device;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public record DeviceResponse(
        UUID id,
        UUID houseId,
        UUID roomId,
        String typeCode,
        String name,
        String serialNumber,
        String status,
        Instant lastSeenAt) {

    public static DeviceResponse from(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getHouseId(),
                device.getRoomId(),
                device.getTypeCode(),
                device.getName(),
                device.getSerialNumber(),
                device.getStatus().name().toLowerCase(Locale.ROOT),
                device.getLastSeenAt());
    }
}
