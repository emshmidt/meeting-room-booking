package com.learning.booking.repository;

import com.learning.booking.domain.TimeSlot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotRepository {
    /**
     * Сохраняет новый слот.
     *
     * После успешного сохранения слот доступен через
     * findById() и findAll().
     *  При отклонении записи состояние хранилища не меняется.
     *
     * @param timeSlot слот для сохранения
     * @throws IllegalArgumentException если timeSlot равен null
     *         или слот с таким UUID уже существует
     *         или timeSlot.id равен null
     */
    void save(TimeSlot timeSlot);

    /**
     * Находит слот по id.
     * Поиск не изменяет состояние хранилища.
     *
     * @param id идентификатор слота
     * @return найденный слот или {@code Optional.empty()},
     *         если слот отсутствует
     * @throws IllegalArgumentException  если id равен null
     */
    Optional<TimeSlot> findById (UUID id);

    /**
     * Возвращает все слоты, порядок результата не гарантирован.
     *
     * Последующие изменения хранилища не изменяют
     * ранее полученный список.
     *
     * @return неизменяемый снимок слотов;
     *         пустой список, если хранилище пустое
     */

    List<TimeSlot> findAll();
}
