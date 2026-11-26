package com.learning.booking.controller;

import com.learning.booking.dto.CreateSlotRequest;
import com.learning.booking.dto.ErrorResponse;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.dto.SlotResponse;
import com.learning.booking.exception.InvalidFieldException;
import com.learning.booking.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/slots")
public class SlotController {
    private final SlotService slotService;

    SlotController(SlotService slotService) {
        if (slotService == null) {
            throw new IllegalArgumentException("Slot service must not be null");
        }

        this.slotService = slotService;
    }

    @Operation(
            summary = "Создать слот для комнаты",
            description = "Добавляет часовой слот для выбранной комнаты. "
                    + "Слот должен начинаться ровно в начале часа по UTC. Доступно администратору."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Слот создан",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SlotResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректный UUID комнаты или неверные данные слота",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Создавать слоты может только администратор",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Комната не найдена",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "У комнаты уже есть слот с таким временем начала",
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
    public ResponseEntity<SlotResponse> createSlot(
            @Parameter(schema = @Schema(type = "string", format = "uuid"))
            @PathVariable("roomId") String roomIdString,
            @Valid @RequestBody CreateSlotRequest slotRequest
    ) {
        UUID roomId = PathParameters.parseUuid(roomIdString, "roomId");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(slotService.createSlot(roomId, slotRequest));
    }

    @Operation(
            summary = "Получить слоты комнаты",
            description = "Возвращает слоты комнаты, время начала которых попадает в период [from, to). "
                    + "При availableOnly=true возвращает только свободные будущие слоты."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список слотов комнаты получен",
            useReturnTypeSchema = true
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректный UUID комнаты, период или параметры запроса",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Комната не найдена",
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
    public ResponseEntity<PageResponse<SlotResponse>> getSlots (
            @Parameter(schema = @Schema(type = "string", format = "uuid"))
            @PathVariable("roomId") String roomIdString,

            @Parameter(description = "Нижняя граница времени начала слота, включительно. Дата и время со смещением.")
            @RequestParam(name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,

            @Parameter(description = "Верхняя граница времени начала слота, исключительно. Дата и время со смещением, строго позже from.")
            @RequestParam(name = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to,

            @Parameter(schema = @Schema(type = "boolean", defaultValue = "false"))
            @RequestParam(name = "availableOnly", required = false) String availableOnlyString,
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
        UUID roomId = PathParameters.parseUuid(roomIdString, "roomId");

        boolean availableOnly = switch (availableOnlyString) {
            case null -> false;
            case "false" -> false;
            case "true" -> true;
            default -> throw new InvalidFieldException("availableOnly", "Допустимые значения: true или false");
        };

        PageParameters pagination = PageParameters.parse(pageString, sizeString);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(slotService.getPage(
                        roomId, from, to, availableOnly,
                        pagination.page(), pagination.size()
                ));
    }

}
