package com.learning.booking.repository;


import com.learning.booking.domain.Booking;
import com.learning.booking.domain.BookingStatus;
import com.learning.booking.exception.BookingNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    /**
     * Сохраняет новое бронирование.
     *
     * После успешного сохранения бронирование доступно через
     * findById() и findAll().
     *  При отклонении записи состояние хранилища не меняется.
     *
     * @param booking бронирование для сохранения
     * @throws IllegalArgumentException если booking равна null
     *         или бронирование с таким UUID уже существует
     *         или booking.id равен null
     */
    void save(Booking booking);

    /**
     * Находит бронирование по id.
     * Поиск не изменяет состояние хранилища.
     *
     * @param id идентификатор бронирования
     * @return найденное бронирование или {@code Optional.empty()},
     *         если бронирование отсутствует
     * @throws IllegalArgumentException  если id равен null
     */
    Optional<Booking> findById (UUID id);

    /**
     * Возвращает все бронирования, порядок результата не гарантирован.
     *
     * Последующие изменения хранилища не изменяют
     * ранее полученный список.
     *
     * @return неизменяемый снимок бронирований;
     *         пустой список, если хранилище пустое
     */

    List<Booking> findAll();

    /**
     * Обновляет статус существующего бронирования.
     *
     * <p>Бронирование определяется по {@code bookingId}.
     * Поля {@code id}, {@code userId}, {@code slotId} и {@code createdAt}
     * берутся из сохранённой записи. Количество записей сохраняется.
     *
     * <p>После успешного обновления новый статус доступен через
     * {@code findById()} и {@code findAll()}.
     * При отклонении обновления состояние хранилища сохраняется.
     *
     * @param bookingId идентификатор бронирования, который используется для поиска
     * @param status новый статус бронирования
     * @throws IllegalArgumentException если  {@code bookingId}
     *         или {@code status} равны {@code null},
     * @throws BookingNotFoundException если бронирование с указанным идентификатором отсутствует
     */

    void updateStatus(UUID bookingId, BookingStatus status);

}
