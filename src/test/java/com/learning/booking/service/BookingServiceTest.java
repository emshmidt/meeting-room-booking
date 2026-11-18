package com.learning.booking.service;

import com.learning.booking.config.CurrentUserProvider;
import com.learning.booking.domain.*;
import com.learning.booking.dto.BookingResponse;
import com.learning.booking.dto.CreateBookingRequest;
import com.learning.booking.exception.CancellationClosedException;
import com.learning.booking.exception.ForbiddenOperationException;
import com.learning.booking.exception.SlotAlreadyBookedException;
import com.learning.booking.repository.BookingRepository;
import com.learning.booking.repository.InMemoryBookingRepository;
import com.learning.booking.repository.InMemoryTimeSlotRepository;
import com.learning.booking.repository.TimeSlotRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    @Test
    void shouldRejectSecondBookingForSameSlotAndKeepFirstBooking() {
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        TimeSlotRepository slotRepository = new InMemoryTimeSlotRepository();

        User testUser = new User(UUID.randomUUID(), "Veronica", "veronica@test.com", Role.ADMIN);
        CurrentUserProvider provider = () -> testUser;

        Instant from = Instant.parse("2030-01-14T10:00:00Z");
        Instant to = Instant.parse("2030-01-14T11:00:00Z");
        TimeSlot slot = new TimeSlot(UUID.randomUUID(), UUID.randomUUID(), from, to);
        slotRepository.save(slot);

        Clock clock = Clock.fixed(
                Instant.parse("2030-01-14T09:00:00Z"),
                ZoneOffset.UTC
        );
        BookingService service = new BookingService(bookingRepository, slotRepository, provider, clock);

        BookingResponse response = service.createBooking(new CreateBookingRequest(slot.id()));

        assertEquals(testUser.id(), response.userId());
        assertEquals(BookingStatus.ACTIVE, response.status());
        assertEquals(clock.instant(), response.createdAt());

        assertThrows(SlotAlreadyBookedException.class,
                () -> service.createBooking(new CreateBookingRequest(slot.id())));

        assertEquals(1, bookingRepository.findAll().size());
        assertEquals(response.id(), bookingRepository.findAll().getFirst().id());
    }

    @Test
    void shouldRejectCancellationWhenSlotStartsNow() {
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        TimeSlotRepository slotRepository = new InMemoryTimeSlotRepository();

        User testUser = new User(UUID.randomUUID(), "Veronica", "veronica@test.com", Role.USER);
        CurrentUserProvider provider = () -> testUser;

        Instant from = Instant.parse("2030-01-14T10:00:00Z");
        Instant to = Instant.parse("2030-01-14T11:00:00Z");
        TimeSlot slot = new TimeSlot(UUID.randomUUID(), UUID.randomUUID(), from, to);
        slotRepository.save(slot);

        Booking booking = new Booking(
                UUID.randomUUID(), testUser.id(), slot.id(),
                BookingStatus.ACTIVE,Instant.parse("2030-01-14T09:00:00Z")
        );
        bookingRepository.save(booking);

        Clock clock = Clock.fixed(
                Instant.parse("2030-01-14T10:00:00Z"),
                ZoneOffset.UTC
        );
        BookingService service = new BookingService(bookingRepository, slotRepository, provider, clock);

        assertThrows(CancellationClosedException.class,
                () -> service.cancelBooking(booking.id())
        );

        Booking newBooking = bookingRepository.findById(booking.id())
                .orElseThrow();

        assertEquals(BookingStatus.ACTIVE, newBooking.status());
        assertEquals(1, bookingRepository.findAll().size());
    }

    @Test
    void shouldAllowRepeatedCancellationAfterSlotStart() {
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        TimeSlotRepository slotRepository = new InMemoryTimeSlotRepository();

        User testUser = new User(UUID.randomUUID(), "Veronica", "veronica@test.com", Role.USER);
        CurrentUserProvider provider = () -> testUser;

        Instant from = Instant.parse("2030-01-14T10:00:00Z");
        Instant to = Instant.parse("2030-01-14T11:00:00Z");
        TimeSlot slot = new TimeSlot(UUID.randomUUID(), UUID.randomUUID(), from, to);
        slotRepository.save(slot);

        Booking booking = new Booking(
                UUID.randomUUID(), testUser.id(), slot.id(),
                BookingStatus.CANCELLED,Instant.parse("2030-01-14T09:00:00Z")
        );
        bookingRepository.save(booking);

        Clock clock = Clock.fixed(
                Instant.parse("2030-01-14T11:00:00Z"),
                ZoneOffset.UTC
        );
        BookingService service = new BookingService(bookingRepository, slotRepository, provider, clock);

        assertDoesNotThrow(() -> {
            service.cancelBooking(booking.id());
            service.cancelBooking(booking.id());
        });

        Booking newBooking = bookingRepository.findById(booking.id())
                .orElseThrow();
        assertEquals(booking, newBooking);
        assertEquals(1, bookingRepository.findAll().size());
    }

    @Test
    void shouldRejectCancellationOfAnotherUsersCancelledBooking() {
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        TimeSlotRepository slotRepository = new InMemoryTimeSlotRepository();

        User slotUser = new User(UUID.randomUUID(), "Anna", "anna@test.com", Role.USER);
        User testUser = new User(UUID.randomUUID(), "Boris", "boris@test.com", Role.USER);
        CurrentUserProvider provider = () -> testUser;

        Instant from = Instant.parse("2030-01-14T10:00:00Z");
        Instant to = Instant.parse("2030-01-14T11:00:00Z");
        TimeSlot slot = new TimeSlot(UUID.randomUUID(), UUID.randomUUID(), from, to);
        slotRepository.save(slot);

        Booking booking = new Booking(
                UUID.randomUUID(), slotUser.id(), slot.id(),
                BookingStatus.CANCELLED,Instant.parse("2030-01-14T09:00:00Z")
        );
        bookingRepository.save(booking);

        Clock clock = Clock.fixed(
                Instant.parse("2030-01-14T11:00:00Z"),
                ZoneOffset.UTC
        );
        BookingService service = new BookingService(bookingRepository, slotRepository, provider, clock);


        assertThrows(ForbiddenOperationException.class,
                () -> service.cancelBooking(booking.id())
        );

        Booking newBooking = bookingRepository.findById(booking.id())
                .orElseThrow();
        assertEquals(booking, newBooking);
        assertEquals(1, bookingRepository.findAll().size());
    }

}
