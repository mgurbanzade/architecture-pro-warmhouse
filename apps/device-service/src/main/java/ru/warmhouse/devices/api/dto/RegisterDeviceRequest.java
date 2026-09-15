package ru.warmhouse.devices.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegisterDeviceRequest(
        @NotNull UUID houseId,
        UUID roomId,
        @NotBlank String typeCode,
        @NotBlank String serialNumber,
        @NotBlank String name) {
}
