package com.learning.booking.repository;

import com.learning.booking.domain.Room;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository {
    /**
     * Сохраняет новую запись.
     *
     * После успешного сохранения комната доступна через
     * findById() и findAll().
     *  При отклонении записи состояние хранилища не меняется.
     *
     * @param room комната для сохранения
     * @throws IllegalArgumentException если room равна null
     *         или комната с таким UUID уже существует
     *         или room.id равен null
     */
    void save(Room room);

    /**
     * Находит комнату по id.
     * Поиск не изменяет состояние хранилища.
     *
     * @param id идентификатор комнаты
     * @return найденная комната или {@code Optional.empty()},
     *         если комната отсутствует
     * @throws IllegalArgumentException  если id равен null
     */
    Optional<Room> findById (UUID id);

    /**
     * Возвращает все комнаты, порядок результата не гарантирован.
     *
     * Последующие изменения хранилища не изменяют
     * ранее полученный список.
     *
     * @return неизменяемый снимок комнат;
     *         пустой список, если хранилище пустое
     */

    List<Room> findAll();
}
