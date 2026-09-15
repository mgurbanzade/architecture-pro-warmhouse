package ru.warmhouse.devices.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandPolicyTest {

    @Test
    void allowsCommandListedInTypeCapabilities() {
        assertDoesNotThrow(() -> CommandPolicy.ensureSupported("heating", "turn_on"));
    }

    @Test
    void rejectsCommandNotListedInTypeCapabilities() {
        assertThrows(UnsupportedCommandException.class,
                () -> CommandPolicy.ensureSupported("temperature", "turn_on"));
    }

    @Test
    void rejectsUnknownDeviceType() {
        assertThrows(UnknownDeviceTypeException.class,
                () -> CommandPolicy.ensureSupported("toaster", "turn_on"));
    }
}
