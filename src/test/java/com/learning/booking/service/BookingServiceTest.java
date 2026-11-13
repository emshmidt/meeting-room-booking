package com.learning.booking.service;

import com.learning.booking.config.CurrentUserProvider;
import com.learning.booking.domain.BookingStatus;
import com.learning.booking.domain.Role;
import com.learning.booking.domain.TimeSlot;
import com.learning.booking.domain.User;
import com.learning.booking.dto.BookingResponse;
import com.learning.booking.dto.CreateBookingRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

}
