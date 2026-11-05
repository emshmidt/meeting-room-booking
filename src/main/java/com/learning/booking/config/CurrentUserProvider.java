package com.learning.booking.config;

import com.learning.booking.domain.User;

public interface CurrentUserProvider {
    /**При корректной конфигурации текущий пользователь должен существовать.
    * Ошибку настройки не будем маскировать пустым результатом.
     */
    User getCurrentUser();
}
