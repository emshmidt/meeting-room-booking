package com.learning.booking.service;

import com.learning.booking.config.CurrentUserProvider;
import com.learning.booking.domain.Role;
import com.learning.booking.domain.Room;
import com.learning.booking.domain.User;
import com.learning.booking.repository.InMemoryRoomRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoomServiceTest {

    @Test
    void shouldFilterSortAndPaginateRoomsKeepingTotalCount() {
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

        InMemoryRoomRepository repository = new InMemoryRoomRepository() {
            @Override
            public List<Room> findAll() {
                return List.of(luna, sirius, orion, vega);
            }
        };
        User testUser = new User(UUID.randomUUID(), "Veronica", "veronica@test.com", Role.ADMIN);
        CurrentUserProvider provider = () -> testUser;

        RoomService service = new RoomService(repository, provider);

        var firstPage = service.getPage(8, 0, 2);
        assertEquals(3L, firstPage.totalElements());
        assertEquals(0, firstPage.page());
        assertEquals(2, firstPage.size());
        assertEquals(2, firstPage.items().size());

        assertEquals(vega.id(), firstPage.items().get(0).id());
        assertEquals(sirius.id(), firstPage.items().get(1).id());

        var secondPage = service.getPage(8, 1, 2);
        assertEquals(3L, secondPage.totalElements());
        assertEquals(1, secondPage.page());
        assertEquals(2, secondPage.size());
        assertEquals(1, secondPage.items().size());

        assertEquals(luna.id(), secondPage.items().get(0).id());

        var thirdPage = service.getPage(8, 2, 2);
        assertEquals(3L, thirdPage.totalElements());
        assertEquals(2, thirdPage.page());
        assertEquals(2, thirdPage.size());
        assertTrue(thirdPage.items().isEmpty());

        var bigPage = service.getPage(8, Integer.MAX_VALUE, 100);
        assertEquals(3L, bigPage.totalElements());
        assertEquals(2147483647, bigPage.page());
        assertEquals(100, bigPage.size());
        assertTrue(bigPage.items().isEmpty());


    }
}
