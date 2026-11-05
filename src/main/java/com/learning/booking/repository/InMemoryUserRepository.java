package com.learning.booking.repository;

import com.learning.booking.domain.User;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryUserRepository implements UserRepository{
    private final Map<UUID, User> storage =
            new HashMap<>();

    @Override
    public synchronized void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (user.id() == null) {
            throw new IllegalArgumentException("User's id must not be null");
        }
        if (storage.containsKey(user.id())) {
            throw new IllegalArgumentException("User's id must be unique");
        }

        storage.put(user.id(), user);
    }

    @Override
    public synchronized Optional<User> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }

        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public synchronized List<User> findAll() {
        return List.copyOf(storage.values());
    }
}
