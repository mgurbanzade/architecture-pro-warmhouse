package ru.warmhouse.devices.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.warmhouse.devices.domain.DeviceType;
import ru.warmhouse.devices.domain.DeviceTypes;

import java.util.List;

@RestController
public class DeviceTypeController {

    @GetMapping("/device-types")
    public List<DeviceType> list() {
        return DeviceTypes.ALL;
    }
}
