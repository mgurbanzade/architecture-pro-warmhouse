package ru.warmhouse.devices.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceTypesTest {

    @Test
    void catalogContainsMonolithSensorType() {
        assertTrue(DeviceTypes.byCode("temperature").isPresent());
    }

    @Test
    void heatingSupportsOnlyOnAndOff() {
        assertEquals(java.util.List.of("turn_on", "turn_off"), DeviceTypes.byCode("heating").orElseThrow().capabilities());
    }

    @Test
    void unknownCodeIsEmpty() {
        assertTrue(DeviceTypes.byCode("toaster").isEmpty());
    }
}
