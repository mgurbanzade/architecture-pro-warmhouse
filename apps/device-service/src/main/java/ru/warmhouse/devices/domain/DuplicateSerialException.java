package ru.warmhouse.devices.domain;

public class DuplicateSerialException extends RuntimeException {

    public DuplicateSerialException(String serialNumber) {
        super("Device with serial number " + serialNumber + " is already connected");
    }
}
