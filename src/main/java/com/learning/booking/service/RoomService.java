package com.learning.booking.service;

import com.learning.booking.domain.Room;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.dto.RoomResponse;
import com.learning.booking.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static java.lang.Long.min;


@Service
public class RoomService {
    private final RoomRepository roomRepository;

    RoomService(RoomRepository roomRepository) {
        if (roomRepository == null) {
            throw new IllegalArgumentException("Room repository must not be null");
        }

        this.roomRepository = roomRepository;
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

}
