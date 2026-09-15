package ru.warmhouse.devices.domain;

public final class CommandPolicy {

    private CommandPolicy() {
    }

    public static void ensureSupported(String typeCode, String commandType) {
        DeviceType type = DeviceTypes.byCode(typeCode)
                .orElseThrow(() -> new UnknownDeviceTypeException(typeCode));
        if (!type.capabilities().contains(commandType)) {
            throw new UnsupportedCommandException(typeCode, commandType);
        }
    }
}
