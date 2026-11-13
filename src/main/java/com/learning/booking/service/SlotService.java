package com.learning.booking.service;

import com.learning.booking.config.CurrentUserProvider;
import com.learning.booking.domain.*;
import com.learning.booking.dto.CreateSlotRequest;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.dto.SlotResponse;
import com.learning.booking.exception.ForbiddenOperationException;
import com.learning.booking.exception.InvalidSlotDataException;
import com.learning.booking.exception.RoomNotFoundException;
import com.learning.booking.exception.SlotAlreadyExistsException;
import com.learning.booking.repository.BookingRepository;
import com.learning.booking.repository.RoomRepository;
import com.learning.booking.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.Long.min;

@Service
public class SlotService {
    private final RoomRepository roomRepository;
    private final TimeSlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final CurrentUserProvider userProvider;
    private final Clock clock;

    SlotService(RoomRepository roomRepository, TimeSlotRepository slotRepository,
                BookingRepository bookingRepository, CurrentUserProvider userProvider,
                Clock clock) {
        if (roomRepository == null) {
            throw new IllegalArgumentException("Room repository must not be null");
        }
        if (slotRepository == null) {
            throw new IllegalArgumentException("Time slot repository must not be null");
        }
        if (bookingRepository == null) {
            throw new IllegalArgumentException("Booking repository must not be null");
        }
        if (userProvider == null) {
            throw new IllegalArgumentException("User provider must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("Clock must not be null");
        }

        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.userProvider = userProvider;
        this.clock = clock;
    }

    public PageResponse<SlotResponse> getPage(
            UUID roomId,
            OffsetDateTime from,
            OffsetDateTime to,
            boolean availableOnly,
            int page,
            int size
    ) {
        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isEmpty()) {
            throw new RoomNotFoundException("Room was not found");
        }

        if (from == null) {
            throw new InvalidSlotDataException("Starting time should not be null");
        }
        if (to == null) {
            throw new InvalidSlotDataException("Ending time should not be null");
        }
        if (!from.isBefore(to)) {
            throw new InvalidSlotDataException("Ending time should be after starting time");
        }

        Instant now = clock.instant();
        List<TimeSlot> slots = slotRepository.findAll();
        List<Booking> bookings = bookingRepository.findAll();

        Instant fromInstant = from.toInstant();
        Instant toInstant = to.toInstant();

        List<TimeSlot> roomSlots = slots.stream()
                .filter(slot-> slot.roomId().equals(roomId))
                .filter(slot -> !slot.startsAt().isBefore(fromInstant) && slot.startsAt().isBefore(toInstant))
                .toList();


        List<TimeSlot> filteredSlots;
        if (availableOnly) {
            filteredSlots = roomSlots.stream()
                    .filter(slot -> isSlotAvailable(slot, now, bookings))
                    .toList();
        } else {
            filteredSlots = roomSlots;
        }

        long totalElements = filteredSlots.size();


        List<TimeSlot> sortedSlots = filteredSlots.stream()
                .sorted(
                        Comparator.comparing(TimeSlot::startsAt)
                                .thenComparing(TimeSlot::id)
                )
                .toList();

        long offset = (long) page * size;

        List<SlotResponse> items;

        if (offset >= totalElements) {
            items = List.of();
        } else {
            int fromIndex = Math.toIntExact(offset);

            int toIndex = Math.toIntExact(min(offset + size, totalElements));

            items = sortedSlots.subList(fromIndex, toIndex).stream().map(
                    slot -> new SlotResponse(
                            slot.id(), slot.roomId(), slot.startsAt(), slot.endsAt(),
                            isSlotAvailable(slot, now, bookings))
            ).toList();
        }

        return new PageResponse<SlotResponse>(items, page, size, totalElements);
    }

    public synchronized SlotResponse createSlot(UUID roomId,CreateSlotRequest slotRequest) {
        User user = userProvider.getCurrentUser();
        Instant now = clock.instant();

        if (user.role() != Role.ADMIN) {
            throw new ForbiddenOperationException("Only ADMIN can create a new slot");
        }

        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isEmpty()) {
            throw new RoomNotFoundException("Room was not found");
        }

        if (slotRequest.startsAt() == null) {
            throw new InvalidSlotDataException("Starts at must not be null");
        }
        if (slotRequest.endsAt() == null) {
            throw new InvalidSlotDataException("Ends at must not be null");
        }

        Instant startsAt = slotRequest.startsAt().toInstant();
        Instant endsAt = slotRequest.endsAt().toInstant();

        if (!startsAt.equals(startsAt.truncatedTo(ChronoUnit.HOURS))) {
            throw new InvalidSlotDataException("Slot should start at the exact hour");
        }

        if (!Duration.between(startsAt, endsAt)
                .equals(Duration.ofMinutes(60))) {
            throw new InvalidSlotDataException("Slot should last a whole hour");
        }


        List<TimeSlot> slots = slotRepository.findAll();
        List<TimeSlot> roomSlots = slots.stream()
                .filter(slot -> slot.roomId().equals(roomId))
                .filter(slot -> slot.startsAt().equals(startsAt))
                .toList();
        if (!roomSlots.isEmpty()) {
            throw new SlotAlreadyExistsException("Slot for this room with this startsAt already exists");
        }

        TimeSlot newSlot = new TimeSlot(
                UUID.randomUUID(), roomId, startsAt, endsAt
                );

        slotRepository.save(newSlot);

        return new SlotResponse(
                newSlot.id(), newSlot.roomId(), newSlot.startsAt(), newSlot.endsAt(),
                isSlotAvailable(newSlot, now, bookingRepository.findAll())
        );
    }


    private boolean isSlotAvailable(TimeSlot slot, Instant now, List<Booking> bookings) {
        boolean noActiveBookings = bookings.stream()
                .filter(booking -> booking.slotId().equals(slot.id()))
                .noneMatch(booking -> booking.status() == BookingStatus.ACTIVE);

        return noActiveBookings && slot.startsAt().isAfter(now);
    }



}
