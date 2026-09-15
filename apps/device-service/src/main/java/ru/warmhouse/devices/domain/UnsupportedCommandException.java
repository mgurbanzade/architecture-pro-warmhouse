package ru.warmhouse.devices.domain;

public class UnsupportedCommandException extends RuntimeException {

    public UnsupportedCommandException(String typeCode, String commandType) {
        super("Type " + typeCode + " does not support command " + commandType);
    }
}
