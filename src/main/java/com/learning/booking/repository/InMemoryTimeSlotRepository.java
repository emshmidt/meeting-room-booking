package com.learning.booking.repository;


import com.learning.booking.domain.TimeSlot;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryTimeSlotRepository implements TimeSlotRepository{
    private final Map<UUID, TimeSlot> storage =
            new HashMap<>();

    @Override
    public synchronized void save(TimeSlot timeSlot) {
        if (timeSlot == null) {
            throw new IllegalArgumentException("Time slot must not be null");
        }
        if (timeSlot.id() == null) {
            throw new IllegalArgumentException("Time slot's id must not be null");
        }
        if (storage.containsKey(timeSlot.id())) {
            throw new IllegalArgumentException("Time slot's id must be unique");
        }

        storage.put(timeSlot.id(), timeSlot);
    }

    @Override
    public synchronized Optional<TimeSlot> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }

        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public synchronized List<TimeSlot> findAll() {
        return List.copyOf(storage.values());
    }
}
