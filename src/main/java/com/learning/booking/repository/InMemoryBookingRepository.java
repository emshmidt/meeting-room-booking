package com.learning.booking.repository;


import com.learning.booking.domain.Booking;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryBookingRepository implements BookingRepository{
    private final Map<UUID, Booking> storage =
            new HashMap<>();

    @Override
    public synchronized void save(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking must not be null");
        }
        if (booking.id() == null) {
            throw new IllegalArgumentException("Booking's id must not be null");
        }
        if (storage.containsKey(booking.id())) {
            throw new IllegalArgumentException("Booking's id must be unique");
        }

        storage.put(booking.id(), booking);
    }

    @Override
    public synchronized Optional<Booking> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }

        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public synchronized List<Booking> findAll() {
        return List.copyOf(storage.values());
    }
}
