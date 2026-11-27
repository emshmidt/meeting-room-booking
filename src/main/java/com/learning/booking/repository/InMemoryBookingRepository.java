package com.learning.booking.repository;


import com.learning.booking.domain.Booking;
import com.learning.booking.domain.BookingStatus;
import com.learning.booking.exception.BookingNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Profile("memory")
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

    @Override
    public synchronized void updateStatus(UUID bookingId, BookingStatus status) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking id must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null");
        }

        Booking booking = storage.get(bookingId);

        if (booking == null) {
            throw new BookingNotFoundException("Booking not found");
        }

        Booking newBooking = new Booking(
                bookingId, booking.userId(), booking.slotId(),status, booking.createdAt()
        );

        storage.replace(bookingId, booking, newBooking);
    }
}
