package com.learning.booking.service;

import com.learning.booking.config.CurrentUserProvider;
import com.learning.booking.domain.*;
import com.learning.booking.dto.BookingResponse;
import com.learning.booking.dto.CreateBookingRequest;
import com.learning.booking.exception.*;
import com.learning.booking.repository.BookingRepository;
import com.learning.booking.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final TimeSlotRepository slotRepository;
    private final CurrentUserProvider provider;
    private final Clock clock;

    BookingService(BookingRepository bookingRepository,
                   TimeSlotRepository slotRepository,
                   CurrentUserProvider provider,
                   Clock clock
                   ) {
        if (bookingRepository == null) {
            throw new IllegalArgumentException("Booking repository must not be null");
        }
        if (slotRepository == null) {
            throw new IllegalArgumentException("Slot repository must not be null");
        }
        if (provider == null) {
            throw new IllegalArgumentException("User provider must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("Clock must not be null");
        }

        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.provider = provider;
        this.clock = clock;
    }


    public synchronized BookingResponse createBooking(CreateBookingRequest bookingRequest) {
        if (bookingRequest.slotId() == null) {
            throw new InvalidBookingDataException("Slot id must not be null");
        }

        User user = provider.getCurrentUser();

        TimeSlot slot = slotRepository.findById(bookingRequest.slotId())
                .orElseThrow(() -> new SlotNotFoundException("Slot was not found"));

        Instant now = clock.instant();
        if (!slot.startsAt().isAfter(now)) {
            throw new SlotAlreadyStartedException("Slot already started");
        }

        if (bookingRepository.findAll().stream().filter(b -> b.slotId().equals(bookingRequest.slotId()))
                .anyMatch(b -> b.status() == BookingStatus.ACTIVE)) {
            throw new SlotAlreadyBookedException("Slot already booked");
        }

        Booking booking = new Booking(UUID.randomUUID(), user.id(), bookingRequest.slotId(), BookingStatus.ACTIVE, now);

        bookingRepository.save(booking);

        return toBookingResponse(booking, slot);
    }

    public BookingResponse getBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking was not found"));

        User user = provider.getCurrentUser();

        if (user.role() != Role.ADMIN && !user.id().equals(booking.userId())) {
            throw new ForbiddenOperationException("Only ADMIN and owner can get booking");
        }

        TimeSlot slot = slotRepository.findById(booking.slotId())
                .orElseThrow(() -> new BookingSlotMissingException("Internal server error"));

        return toBookingResponse(booking, slot);
    }

    private BookingResponse toBookingResponse(Booking booking, TimeSlot slot) {
        return new BookingResponse(
                booking.id(), booking.userId(), booking.slotId(),
                booking.status(), booking.createdAt(),
                slot.roomId(), slot.startsAt(), slot.endsAt()
        );
    }




}
