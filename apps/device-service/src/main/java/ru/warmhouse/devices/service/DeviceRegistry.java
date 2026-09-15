package ru.warmhouse.devices.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.warmhouse.devices.domain.Device;
import ru.warmhouse.devices.domain.DeviceNotFoundException;
import ru.warmhouse.devices.domain.DeviceRepository;
import ru.warmhouse.devices.domain.DeviceTypes;
import ru.warmhouse.devices.domain.DuplicateSerialException;
import ru.warmhouse.devices.domain.UnknownDeviceTypeException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceRegistry {

    private final DeviceRepository devices;

    public DeviceRegistry(DeviceRepository devices) {
        this.devices = devices;
    }

    @Transactional
    public Device register(UUID houseId, UUID roomId, String typeCode, String serialNumber, String name) {
        DeviceTypes.byCode(typeCode).orElseThrow(() -> new UnknownDeviceTypeException(typeCode));
        if (devices.existsBySerialNumber(serialNumber)) {
            throw new DuplicateSerialException(serialNumber);
        }
        return devices.save(Device.register(houseId, roomId, typeCode, serialNumber, name));
    }

    @Transactional(readOnly = true)
    public List<Device> listByHouse(UUID houseId) {
        return devices.findByHouseIdOrderByCreatedAtAsc(houseId);
    }

    @Transactional(readOnly = true)
    public Device get(UUID id) {
        return devices.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));
    }

    @Transactional
    public void delete(UUID id) {
        devices.delete(get(id));
    }

    @Transactional
    public boolean markSeen(String serialNumber, Instant at) {
        return devices.findBySerialNumber(serialNumber)
                .map(device -> {
                    device.markSeen(at);
                    return true;
                })
                .orElse(false);
    }
}
