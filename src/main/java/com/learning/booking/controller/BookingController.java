package com.learning.booking.controller;

import com.learning.booking.domain.BookingStatus;
import com.learning.booking.dto.BookingResponse;
import com.learning.booking.dto.CreateBookingRequest;
import com.learning.booking.dto.ErrorResponse;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.exception.InvalidFieldException;
import com.learning.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    private final BookingService bookingService;

    BookingController (BookingService bookingService) {
        if (bookingService == null) {
            throw new IllegalArgumentException("Booking service must not be null");
        }

        this.bookingService = bookingService;
    }

    @Operation(
            summary = "Забронировать слот комнаты",
            description = "Бронирует свободный будущий слот комнаты для текущего пользователя. "
                    + "Заголовок Location содержит URI созданной брони."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Бронь создана",
            headers = @Header(
                    name = "Location",
                    description = "Путь к созданной брони, по которому можно выполнить GET",
                    required = true,
                    schema = @Schema(type = "string")
            ),
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BookingResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректное тело запроса или неверный формат slotId",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Слот не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Слот уже забронирован или его время начала наступило",
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
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest bookingRequest
    ) {
        BookingResponse response = bookingService.createBooking(bookingRequest);
        return ResponseEntity
                .created(URI.create("/api/v1/bookings/"+ response.id().toString()))
                .body(response);
    }

    @Operation(
            summary = "Получить бронь комнаты по ID",
            description = "Возвращает данные брони комнаты, включая её текущий статус. "
                    + "Доступно владельцу брони и администратору."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Данные брони получены",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BookingResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректный формат UUID брони",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Бронь доступна только владельцу или администратору",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Бронь не найдена",
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
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @Parameter(schema = @Schema(type = "string", format = "uuid"))
            @PathVariable("id") String idString
    ) {
        UUID id = PathParameters.parseUuid(idString, "id");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.getBooking(id));
    }

    @Operation(
            summary = "Получить историю своих бронирований",
            description = "Возвращает брони комнат текущего пользователя постранично, начиная с самых новых. "
                    + "Параметр status позволяет выбрать активные или отменённые брони."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Страница бронирований текущего пользователя получена",
            useReturnTypeSchema = true
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректный фильтр по статусу или параметры пагинации",
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
    public ResponseEntity<PageResponse<BookingResponse>> getBookings(
            @Parameter(
                    description = "Фильтр по статусу. Если параметр пропущен, возвращаются брони обоих статусов.",
                    schema = @Schema(type = "string", allowableValues = {"ACTIVE", "CANCELLED"})
            )
            @RequestParam(name = "status", required = false) String statusString,
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
        BookingStatus status = null;
        if (statusString != null) {
            try {
                status = BookingStatus.valueOf(statusString);
            } catch (IllegalArgumentException e) {
                throw new InvalidFieldException("status", "Допустимые значения: ACTIVE или CANCELLED", e);
            }
        }
        PageParameters pagination = PageParameters.parse(pageString, sizeString);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.getPage(status, pagination.page(), pagination.size()));
    }

    @Operation(
            summary = "Отменить бронь комнаты",
            description = "Владелец или администратор может отменить активную бронь комнаты до начала слота. "
                    + "Запись сохраняется в истории. Повторная отмена возвращает 204."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Бронь отменена",
            content = @Content
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректный формат UUID брони",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Отменить бронь может только её владелец или администратор",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Бронь не найдена",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Срок отмены активной брони истёк, поскольку слот уже начался",
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @Parameter(schema = @Schema(type = "string", format = "uuid"))
            @PathVariable("id") String idString
    ) {
        UUID id = PathParameters.parseUuid(idString, "id");
        bookingService.cancelBooking(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
