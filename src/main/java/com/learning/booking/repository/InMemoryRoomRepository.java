package com.learning.booking.repository;

import com.learning.booking.domain.Room;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Profile("memory")
public class InMemoryRoomRepository implements RoomRepository{
    private final Map<UUID, Room> storage =
            new HashMap<>();

    @Override
    public synchronized void save(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room must not be null");
        }
        if (room.id() == null) {
            throw new IllegalArgumentException("Room's id must not be null");
        }
        if (storage.containsKey(room.id())) {
            throw new IllegalArgumentException("Room's id must be unique");
        }

        storage.put(room.id(), room);
    }

    @Override
    public synchronized Optional<Room> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }

        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public synchronized List<Room> findAll() {
        return List.copyOf(storage.values());
    }
}
