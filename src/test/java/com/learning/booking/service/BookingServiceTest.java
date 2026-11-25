package com.learning.booking.service;

import com.learning.booking.config.CurrentUserProvider;
import com.learning.booking.domain.*;
import com.learning.booking.dto.BookingResponse;
import com.learning.booking.dto.CreateBookingRequest;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.exception.CancellationClosedException;
import com.learning.booking.exception.ForbiddenOperationException;
import com.learning.booking.exception.SlotAlreadyBookedException;
import com.learning.booking.exception.SlotAlreadyStartedException;
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

    @Test
    void shouldAllowRebookingAfterCancellation() {
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        TimeSlotRepository slotRepository = new InMemoryTimeSlotRepository();

        User testUser = new User(UUID.randomUUID(), "Veronica", "veronica@test.com", Role.USER);
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

        Clock clock2 = Clock.fixed(
                Instant.parse("2030-01-14T09:30:00Z"),
                ZoneOffset.UTC
        );

        BookingService service2 = new BookingService(bookingRepository, slotRepository, provider, clock2);

        service2.cancelBooking(response.id());

        BookingResponse canceledBooking = service2.getBooking(response.id());

        assertEquals(testUser.id(), canceledBooking.userId());
        assertEquals(BookingStatus.CANCELLED, canceledBooking.status());
        assertEquals(response.createdAt(), canceledBooking.createdAt());
        assertEquals(1, bookingRepository.findAll().size());

        Clock clock3 = Clock.fixed(
                Instant.parse("2030-01-14T09:45:00Z"),
                ZoneOffset.UTC
        );

        BookingService service3 = new BookingService(bookingRepository, slotRepository, provider, clock3);

        BookingResponse response3 = service3.createBooking(new CreateBookingRequest(slot.id()));

        assertEquals(testUser.id(), response3.userId());
        assertEquals(BookingStatus.ACTIVE, response3.status());
        assertEquals(clock3.instant(), response3.createdAt());
        assertEquals(2, bookingRepository.findAll().size());

        assertEquals(BookingStatus.CANCELLED, service3.getBooking(response.id()).status());
        assertEquals(BookingStatus.ACTIVE, service3.getBooking(response3.id()).status());
        assertEquals(slot.id(), service3.getBooking(response.id()).slotId());
        assertEquals(slot.id(), service3.getBooking(response3.id()).slotId());
        assertNotEquals(response.id(), response3.id());
    }

    @Test
    void shouldRejectBookingWhenSlotStartsNow() {
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        TimeSlotRepository slotRepository = new InMemoryTimeSlotRepository();

        User testUser = new User(UUID.randomUUID(), "Veronica", "veronica@test.com", Role.USER);
        CurrentUserProvider provider = () -> testUser;

        Instant from = Instant.parse("2030-01-14T10:00:00Z");
        Instant to = Instant.parse("2030-01-14T11:00:00Z");
        TimeSlot slot = new TimeSlot(UUID.randomUUID(), UUID.randomUUID(), from, to);
        slotRepository.save(slot);

        Clock clock = Clock.fixed(
                Instant.parse("2030-01-14T10:00:00Z"),
                ZoneOffset.UTC
        );
        BookingService service = new BookingService(bookingRepository, slotRepository, provider, clock);

        assertThrows(SlotAlreadyStartedException.class,
                () -> service.createBooking(new CreateBookingRequest(slot.id())));

        assertEquals(0, bookingRepository.findAll().size());
    }

    @Test
    void shouldPaginateBookingHistoryAfterFilteringByUserAndStatus() {
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        TimeSlotRepository slotRepository = new InMemoryTimeSlotRepository();

        User annaUser = new User(UUID.randomUUID(), "Anna", "anna@test.com", Role.USER);
        User borisUser = new User(UUID.randomUUID(), "Boris", "boris@test.com", Role.USER);
        CurrentUserProvider provider = () -> annaUser;

        Instant from = Instant.parse("2030-01-14T10:00:00Z");
        Instant to = Instant.parse("2030-01-14T11:00:00Z");
        TimeSlot slot = new TimeSlot(UUID.randomUUID(), UUID.randomUUID(), from, to);
        slotRepository.save(slot);

        Booking bookingAnna1 = new Booking(
                UUID.randomUUID(), annaUser.id(), slot.id(),
                BookingStatus.CANCELLED,Instant.parse("2030-01-14T09:00:00Z")
        );
        Booking bookingAnna2 = new Booking(
                UUID.randomUUID(), annaUser.id(), slot.id(),
                BookingStatus.CANCELLED,Instant.parse("2030-01-14T10:00:00Z")
        );
        Booking bookingAnna3 = new Booking(
                UUID.randomUUID(), annaUser.id(), slot.id(),
                BookingStatus.ACTIVE,Instant.parse("2030-01-14T11:00:00Z")
        );
        bookingRepository.save(bookingAnna1);
        bookingRepository.save(bookingAnna2);
        bookingRepository.save(bookingAnna3);

        Booking bookingBoris = new Booking(
                UUID.randomUUID(), borisUser.id(), slot.id(),
                BookingStatus.CANCELLED,Instant.parse("2030-01-14T09:00:00Z")
        );
        bookingRepository.save(bookingBoris);


        Clock clock = Clock.fixed(
                Instant.parse("2030-01-14T12:00:00Z"),
                ZoneOffset.UTC
        );
        BookingService service = new BookingService(bookingRepository, slotRepository, provider, clock);

        PageResponse<BookingResponse> page0 = service.getPage(
                BookingStatus.CANCELLED, 0, 1
        );
        PageResponse<BookingResponse> page1 = service.getPage(
                BookingStatus.CANCELLED, 1, 1
        );
        PageResponse<BookingResponse> page2 = service.getPage(
                BookingStatus.CANCELLED, 2, 1
        );

        assertEquals(bookingAnna2.id(), page0.items().getFirst().id());
        assertEquals(1, page0.items().size());
        assertEquals(2, page0.totalElements());

        assertEquals(bookingAnna1.id(), page1.items().getFirst().id());
        assertEquals(1, page1.items().size());
        assertEquals(2, page1.totalElements());

        assertEquals(0, page2.items().size());
        assertEquals(2, page2.totalElements());
    }

}
