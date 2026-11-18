package com.learning.booking.service;

import com.learning.booking.config.CurrentUserProvider;
import com.learning.booking.domain.*;
import com.learning.booking.dto.BookingResponse;
import com.learning.booking.dto.CreateBookingRequest;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.exception.*;
import com.learning.booking.repository.BookingRepository;
import com.learning.booking.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static java.lang.Long.min;

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

    public PageResponse<BookingResponse> getPage(
            BookingStatus bookingStatus,
            int page,
            int size
    ) {
        User user = provider.getCurrentUser();

        List<Booking> bookings = bookingRepository.findAll();

        List<Booking> usersBookings = bookings.stream()
                .filter(b -> b.userId().equals(user.id()))
                .toList();

        List<Booking> filteredBookings;

        if (bookingStatus == null) {
            filteredBookings = usersBookings;
        } else {
            filteredBookings = usersBookings.stream()
                    .filter(b -> b.status().equals(bookingStatus))
                    .toList();
        }

        long totalElements = filteredBookings.size();

        List<Booking> sortedBookings = filteredBookings.stream().sorted(
                        Comparator.comparing(Booking::createdAt)
                                .thenComparing(Booking::id)
                                .reversed()
                )
                .toList();

        long offset = (long) page * size;

        List<BookingResponse> items;

        if (offset >= totalElements) {
            items = List.of();
        } else {
            int fromIndex = Math.toIntExact(offset);

            int toIndex = Math.toIntExact(min(offset + size, totalElements));

            items = sortedBookings.subList(fromIndex, toIndex).stream().map(
                    b -> toBookingResponse(b, slotRepository.findById(b.slotId())
                            .orElseThrow(() -> new BookingSlotMissingException("Internal server error")))
            ).toList();
        }

        return new PageResponse<BookingResponse>(items, page, size, totalElements);
    }

    public synchronized void cancelBooking(UUID bookingId) {
        if (bookingId == null) {
            throw new InvalidBookingDataException("Id must not be null");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        User user = provider.getCurrentUser();

        if (!user.id().equals(booking.userId()) && !user.role().equals(Role.ADMIN)) {
            throw new ForbiddenOperationException("Only owner or ADMIN can update booking");
        }

        if (booking.status().equals(BookingStatus.CANCELLED)) {
            return;
        }

        TimeSlot slot = slotRepository.findById(booking.slotId())
                .orElseThrow(() -> new BookingSlotMissingException("Internal server error"));
        Instant now = clock.instant();

        if (!slot.startsAt().isAfter(now)) {
            throw new CancellationClosedException("Slot already started");
        }

        bookingRepository.updateStatus(bookingId, BookingStatus.CANCELLED);
    }




}
