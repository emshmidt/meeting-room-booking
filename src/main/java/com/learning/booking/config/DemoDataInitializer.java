package com.learning.booking.config;

import com.learning.booking.domain.*;
import com.learning.booking.repository.BookingRepository;
import com.learning.booking.repository.RoomRepository;
import com.learning.booking.repository.TimeSlotRepository;
import com.learning.booking.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;


@Component
public class DemoDataInitializer implements ApplicationRunner {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingRepository bookingRepository;
    private final CurrentUserProvider currentUserProvider;



    DemoDataInitializer(RoomRepository roomRepository, UserRepository userRepository,
                        TimeSlotRepository timeSlotRepository, BookingRepository bookingRepository,
                        CurrentUserProvider currentUserProvider) {
        if (roomRepository == null) {
            throw new IllegalArgumentException("Room repository must not be null");
        }
        if (userRepository == null) {
            throw new IllegalArgumentException("User repository must not be null");
        }
        if (timeSlotRepository == null) {
            throw new IllegalArgumentException("Time slot repository must not be null");
        }
        if (bookingRepository == null) {
            throw new IllegalArgumentException("Booking repository must not be null");
        }
        if (currentUserProvider == null) {
            throw new IllegalArgumentException("User provider must not be null");
        }

        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.bookingRepository = bookingRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        fillRoomsData();
        fillUsersData();
        fillTimeSlotsData();
        fillBookingsData();

        System.out.println("Rooms: " + roomRepository.findAll().size());
        System.out.println("Users: " + userRepository.findAll().size());
        System.out.println("Slots: " + timeSlotRepository.findAll().size());
        System.out.println("Bookings: " + bookingRepository.findAll().size());

        System.out.println("Current user: " + currentUserProvider.getCurrentUser());
    }

    private void fillRoomsData() {
        Room orion = new Room(UUID.fromString( "00000000-0000-0000-0000-000000000001"),
                "Orion",
                4,
                true,
                null);

        Room vega = new Room(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "Vega",
                8,
                false,
                "");
        Room sirius = new Room(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                "Sirius",
                8,
                true,
                "Near the window");
        Room luna = new Room(UUID.fromString("00000000-0000-0000-0000-000000000004"),
                "Luna",
                12,
                false,
                null);

        roomRepository.save(orion);
        roomRepository.save(vega);
        roomRepository.save(sirius);
        roomRepository.save(luna);
    }

    private void fillUsersData() {
        User anna = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Anna",
                "anna@example.com",
                Role.USER);
        User boris = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "Boris",
                "boris@example.com",
                Role.USER);
        User vera = new User(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                "Vera",
                "vera@example.com",
                Role.ADMIN);

        userRepository.save(anna);
        userRepository.save(boris);
        userRepository.save(vera);
    }

    private void fillTimeSlotsData() {
        TimeSlot timeSlot1 = new TimeSlot(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                Instant.parse("2030-01-14T09:00:00Z"),
                Instant.parse("2030-01-14T10:00:00Z"));
        TimeSlot timeSlot2 = new TimeSlot(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                Instant.parse("2030-01-14T10:00:00Z"),
                Instant.parse("2030-01-14T11:00:00Z"));
        TimeSlot timeSlot3 = new TimeSlot(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                Instant.parse("2030-01-14T11:00:00Z"),
                Instant.parse("2030-01-14T12:00:00Z"));
        TimeSlot timeSlot4 = new TimeSlot(UUID.fromString("00000000-0000-0000-0000-000000000004"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                Instant.parse("2030-01-14T09:00:00Z"),
                Instant.parse("2030-01-14T10:00:00Z"));
        TimeSlot timeSlot5 = new TimeSlot(UUID.fromString("00000000-0000-0000-0000-000000000005"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                Instant.parse("2030-01-14T10:00:00Z"),
                Instant.parse("2030-01-14T11:00:00Z"));
        TimeSlot timeSlot6 = new TimeSlot(UUID.fromString("00000000-0000-0000-0000-000000000006"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                Instant.parse("2030-01-14T11:00:00Z"),
                Instant.parse("2030-01-14T12:00:00Z"));
        TimeSlot timeSlot7 = new TimeSlot(UUID.fromString("00000000-0000-0000-0000-000000000007"),
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                Instant.parse("2030-01-14T09:00:00Z"),
                Instant.parse("2030-01-14T10:00:00Z"));
        TimeSlot timeSlot8 = new TimeSlot(UUID.fromString("00000000-0000-0000-0000-000000000008"),
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                Instant.parse("2030-01-14T10:00:00Z"),
                Instant.parse("2030-01-14T11:00:00Z"));

        timeSlotRepository.save(timeSlot1);
        timeSlotRepository.save(timeSlot2);
        timeSlotRepository.save(timeSlot3);
        timeSlotRepository.save(timeSlot4);
        timeSlotRepository.save(timeSlot5);
        timeSlotRepository.save(timeSlot6);
        timeSlotRepository.save(timeSlot7);
        timeSlotRepository.save(timeSlot8);
    }

    private void fillBookingsData() {
        Booking booking1 = new Booking(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                BookingStatus.ACTIVE,
                Instant.parse("2026-10-01T09:01:00Z"));
        Booking booking2 = new Booking(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                BookingStatus.CANCELLED,
                Instant.parse("2026-10-01T09:01:00Z"));
        Booking booking3 = new Booking(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                BookingStatus.CANCELLED,
                Instant.parse("2026-10-01T09:01:00Z"));
        Booking booking4 = new Booking(UUID.fromString("00000000-0000-0000-0000-000000000004"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                BookingStatus.ACTIVE,
                Instant.parse("2026-10-01T09:01:00Z"));
        Booking booking5 = new Booking(UUID.fromString("00000000-0000-0000-0000-000000000005"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                UUID.fromString("00000000-0000-0000-0000-000000000004"),
                BookingStatus.ACTIVE,
                Instant.parse("2026-10-01T09:01:00Z"));
        Booking booking6 = new Booking(UUID.fromString("00000000-0000-0000-0000-000000000006"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                UUID.fromString("00000000-0000-0000-0000-000000000007"),
                BookingStatus.CANCELLED,
                Instant.parse("2026-10-01T09:01:00Z"));

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);
        bookingRepository.save(booking5);
        bookingRepository.save(booking6);
    }
}
