package com.learning.booking.repository;

import com.learning.booking.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    /**
     * Сохраняет нового пользователя.
     *
     * После успешного сохранения пользователь доступен через
     * findById() и findAll().
     *  При отклонении записи состояние хранилища не меняется.
     *
     * @param user пользователь для сохранения
     * @throws IllegalArgumentException если user равна null
     *         или пользователь с таким UUID уже существует
     *         или user.id равен null
     */
    void save(User user);

    /**
     * Находит пользователя по id.
     * Поиск не изменяет состояние хранилища.
     *
     * @param id идентификатор пользователя
     * @return найденный пользователь или {@code Optional.empty()},
     *         если пользователь отсутствует
     * @throws IllegalArgumentException  если id равен null
     */
    Optional<User> findById (UUID id);

    /**
     * Возвращает всех пользователей, порядок результата не гарантирован.
     *
     * Последующие изменения хранилища не изменяют
     * ранее полученный список.
     *
     * @return неизменяемый снимок пользователей;
     *         пустой список, если хранилище пустое
     */

    List<User> findAll();
}
