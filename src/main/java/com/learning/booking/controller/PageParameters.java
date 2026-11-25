package com.learning.booking.controller;

import com.learning.booking.exception.InvalidFieldException;

public record PageParameters(int page, int size) {

    static PageParameters parse(String pageString, String sizeString) {
        int page;
        int size;

        if (pageString == null) {
            page = 0;
        } else {
            try {
                page = Integer.parseInt(pageString);
            } catch (NumberFormatException e) {
                throw new InvalidFieldException("page", "Ожидается целое число", e);
            }
        }

        if (sizeString == null) {
            size = 20;
        } else {
            try {
                size = Integer.parseInt(sizeString);
            } catch (NumberFormatException e) {
                throw new InvalidFieldException("size", "Ожидается целое число", e);
            }
        }

        if (page < 0) {
            throw new InvalidFieldException("page", "Номер страницы должен быть неотрицательным");
        }
        if (size < 1 || size > 100) {
            throw new InvalidFieldException("size", "Размер страницы должен быть от 1 до 100");
        }

        return new PageParameters(page, size);
    }
}
