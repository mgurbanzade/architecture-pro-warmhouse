package ru.warmhouse.devices.domain;

import java.util.List;
import java.util.Optional;

public final class DeviceTypes {

    public static final List<DeviceType> ALL = List.of(
            new DeviceType("temperature", "Temperature sensor", "mqtt", List.of()),
            new DeviceType("heating", "Heating module", "mqtt", List.of("turn_on", "turn_off")),
            new DeviceType("light", "Lighting", "mqtt", List.of("turn_on", "turn_off")),
            new DeviceType("gate", "Automatic gate", "mqtt", List.of("open", "close")));

    private DeviceTypes() {
    }

    public static Optional<DeviceType> byCode(String code) {
        return ALL.stream().filter(type -> type.code().equals(code)).findFirst();
    }
}
