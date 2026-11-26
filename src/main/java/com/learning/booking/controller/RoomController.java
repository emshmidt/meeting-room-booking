package com.learning.booking.controller;

import com.learning.booking.dto.CreateRoomRequest;
import com.learning.booking.dto.ErrorResponse;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.dto.RoomResponse;
import com.learning.booking.exception.InvalidFieldException;
import com.learning.booking.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {
    private final RoomService roomService;

    RoomController(RoomService roomService) {
        if (roomService == null) {
            throw new IllegalArgumentException("Room service must not be null");
        }

        this.roomService = roomService;
    }

    @Operation(
            summary = "Получить список комнат",
            description = "Возвращает комнаты постранично. Параметр minCapacity "
                    + "оставляет комнаты с указанной вместимостью или больше."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список комнат получен",
            useReturnTypeSchema = true
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры фильтрации или пагинации",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервиса",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping(produces = "application/json")
    public PageResponse<RoomResponse> getRooms(
            @Parameter(
                    description = "Минимальная вместимость комнаты.",
                    schema = @Schema(type = "integer", minimum = "1")
            )
            @RequestParam(name = "minCapacity", required = false) String minCapacityString,
            @Parameter(
                    description = "Номер страницы. Нумерация начинается с нуля.",
                    schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")
            )
            @RequestParam(name = "page", required = false) String pageString,

            @Parameter(
                    description = "Количество элементов на странице.",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "20")
            )
            @RequestParam(name = "size", required = false) String sizeString
    ) {
        Integer minCapacity = null;
        if (minCapacityString != null) {
            try {
                minCapacity = Integer.parseInt(minCapacityString);
            } catch (NumberFormatException e) {
                throw new InvalidFieldException("minCapacity", "Ожидается целое число", e);
            }

            if (minCapacity <= 0) {
                throw new InvalidFieldException("minCapacity", "Вместимость должна быть больше нуля");
            }
        }

        PageParameters pagination = PageParameters.parse(pageString, sizeString);
        return roomService.getPage(minCapacity, pagination.page(), pagination.size());
    }

    @Operation(
            summary = "Создать комнату",
            description = "Добавляет комнату и возвращает её данные с присвоенным UUID. Доступно администратору."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Комната создана",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoomResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректное тело запроса или неверные данные комнаты",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Создавать комнаты может только администратор",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервиса",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @Valid @RequestBody CreateRoomRequest roomRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roomService.createRoom(roomRequest));
    }
}
