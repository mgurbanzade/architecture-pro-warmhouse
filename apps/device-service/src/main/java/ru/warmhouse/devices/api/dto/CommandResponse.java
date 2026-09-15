package ru.warmhouse.devices.api.dto;

import ru.warmhouse.devices.domain.Command;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public record CommandResponse(
        UUID id,
        UUID deviceId,
        String type,
        String status,
        String issuedBy,
        Instant issuedAt) {

    public static CommandResponse from(Command command) {
        return new CommandResponse(
                command.getId(),
                command.getDeviceId(),
                command.getType(),
                command.getStatus().name().toLowerCase(Locale.ROOT),
                command.getIssuedBy(),
                command.getIssuedAt());
    }
}
