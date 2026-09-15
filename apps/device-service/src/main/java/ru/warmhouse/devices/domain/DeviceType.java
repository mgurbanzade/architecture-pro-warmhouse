package ru.warmhouse.devices.domain;

import java.util.List;

public record DeviceType(String code, String name, String protocol, List<String> capabilities) {
}
