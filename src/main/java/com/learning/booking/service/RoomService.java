package com.learning.booking.service;

import com.learning.booking.config.CurrentUserProvider;
import com.learning.booking.domain.Role;
import com.learning.booking.domain.Room;
import com.learning.booking.domain.User;
import com.learning.booking.dto.CreateRoomRequest;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.dto.RoomResponse;
import com.learning.booking.exception.ForbiddenOperationException;
import com.learning.booking.exception.InvalidRoomDataException;
import com.learning.booking.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static java.lang.Long.min;


@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final CurrentUserProvider provider;

    RoomService(RoomRepository roomRepository, CurrentUserProvider provider) {
        if (roomRepository == null) {
            throw new IllegalArgumentException("Room repository must not be null");
        }
        if (provider == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        this.roomRepository = roomRepository;
        this.provider = provider;
    }

    public PageResponse<RoomResponse> getPage(
            Integer minCapacity,
            int page,
            int size
    ) {

        List<Room> rooms = roomRepository.findAll();
        List<Room> filteredRooms;

        if (minCapacity != null) {
            filteredRooms = rooms.stream().filter(room -> room.capacity() >= minCapacity).toList();
        } else {
            filteredRooms = rooms;
        }

        long totalElements = filteredRooms.size();

        List<Room> sortedRooms = filteredRooms.stream().sorted(
                Comparator.comparing(Room::capacity)
                        .thenComparing(Room::id)
        )
                .toList();

        long offset = (long) page * size;

        List<RoomResponse> items;

        if (offset >= totalElements) {
            items = List.of();
        } else {
            int fromIndex = Math.toIntExact(offset);

            int toIndex = Math.toIntExact(min(offset + size, totalElements));

            items = sortedRooms.subList(fromIndex, toIndex).stream().map(
                    room -> new RoomResponse(room.id(), room.name(),
                            room.capacity(), room.hasProjector(), room.description())
            ).toList();
        }

        return new PageResponse<RoomResponse>(items, page, size, totalElements);
    }

    public RoomResponse createRoom(CreateRoomRequest roomRequest) {
        User user = provider.getCurrentUser();

        if (user.role() != Role.ADMIN) {
            throw new ForbiddenOperationException("Only ADMIN can create a new room");
        }

        if (roomRequest.name() == null) {
            throw new InvalidRoomDataException("Name must not be null");
        }
        if (roomRequest.capacity() == null || roomRequest.capacity() <= 0) {
            throw new InvalidRoomDataException("Capacity must be positive");
        }
        if (roomRequest.hasProjector() == null){
            throw new InvalidRoomDataException("Has projector must not be null");
        }

        String name = roomRequest.name().strip();
        if (name.isEmpty()) {
            throw new InvalidRoomDataException("Name must not be blank");
        }

        Room newRoom = new Room(
                UUID.randomUUID(), name,
                roomRequest.capacity(), roomRequest.hasProjector(),
                roomRequest.description()
        );

        roomRepository.save(newRoom);

        return new RoomResponse(
                newRoom.id(), newRoom.name(), newRoom.capacity(),
                newRoom.hasProjector(), newRoom.description()
        );
    }

}
