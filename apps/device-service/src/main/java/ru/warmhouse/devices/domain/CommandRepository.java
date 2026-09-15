package ru.warmhouse.devices.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommandRepository extends JpaRepository<Command, UUID> {
}
