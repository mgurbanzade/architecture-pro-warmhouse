package ru.warmhouse.devices.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.warmhouse.devices.domain.Command;
import ru.warmhouse.devices.domain.CommandPolicy;
import ru.warmhouse.devices.domain.CommandRepository;
import ru.warmhouse.devices.domain.Device;
import ru.warmhouse.devices.messaging.CommandPublisher;

import java.util.Map;
import java.util.UUID;

@Service
public class CommandDispatcher {

    private final DeviceRegistry registry;
    private final CommandRepository commands;
    private final CommandPublisher publisher;

    public CommandDispatcher(DeviceRegistry registry, CommandRepository commands, CommandPublisher publisher) {
        this.registry = registry;
        this.commands = commands;
        this.publisher = publisher;
    }

    @Transactional
    public Command dispatch(UUID deviceId, String type, Map<String, Object> payload, String issuedBy) {
        Device device = registry.get(deviceId);
        CommandPolicy.ensureSupported(device.getTypeCode(), type);
        Command command = commands.save(Command.issue(deviceId, type, payload, issuedBy));
        publisher.publish(command, device.getSerialNumber());
        return command;
    }
}
