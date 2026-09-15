package ru.warmhouse.devices.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.warmhouse.devices.api.dto.CommandRequest;
import ru.warmhouse.devices.api.dto.CommandResponse;
import ru.warmhouse.devices.api.dto.DeviceResponse;
import ru.warmhouse.devices.api.dto.RegisterDeviceRequest;
import ru.warmhouse.devices.service.CommandDispatcher;
import ru.warmhouse.devices.service.DeviceRegistry;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceRegistry registry;
    private final CommandDispatcher dispatcher;

    public DeviceController(DeviceRegistry registry, CommandDispatcher dispatcher) {
        this.registry = registry;
        this.dispatcher = dispatcher;
    }

    @GetMapping
    public List<DeviceResponse> list(@RequestParam UUID houseId) {
        return registry.listByHouse(houseId).stream().map(DeviceResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse register(@Valid @RequestBody RegisterDeviceRequest request) {
        return DeviceResponse.from(registry.register(
                request.houseId(), request.roomId(), request.typeCode(), request.serialNumber(), request.name()));
    }

    @GetMapping("/{deviceId}")
    public DeviceResponse get(@PathVariable UUID deviceId) {
        return DeviceResponse.from(registry.get(deviceId));
    }

    @DeleteMapping("/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID deviceId) {
        registry.delete(deviceId);
    }

    @PostMapping("/{deviceId}/commands")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CommandResponse command(@PathVariable UUID deviceId, @Valid @RequestBody CommandRequest request) {
        return CommandResponse.from(dispatcher.dispatch(deviceId, request.type(), request.payload(), "user"));
    }
}
