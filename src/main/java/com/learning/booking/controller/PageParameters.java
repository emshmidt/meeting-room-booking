package com.learning.booking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be an integer", e);
            }
        }

        if (sizeString == null) {
            size = 20;
        } else {
            try {
                size = Integer.parseInt(sizeString);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be an integer", e);
            }
        }

        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be not negative");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be from 1 to 100");
        }

        return new PageParameters(page, size);
    }
}
