package ru.warmhouse.devices.domain;

public class UnknownDeviceTypeException extends RuntimeException {

    public UnknownDeviceTypeException(String typeCode) {
        super("Unknown device type: " + typeCode);
    }
}
