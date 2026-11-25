package com.learning.booking.controller;

import com.learning.booking.dto.ErrorResponse;
import com.learning.booking.dto.FieldErrorResponse;
import com.learning.booking.exception.*;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.InputCoercionException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ForbiddenOperationException.class)
    ResponseEntity<ErrorResponse> handleForbiddenOperation (ForbiddenOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("FORBIDDEN", "Недостаточно прав для выполнения операции", List.of()));
    }

    @ExceptionHandler(RoomNotFoundException.class)
    ResponseEntity<ErrorResponse> handleRoomNotFound (RoomNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("ROOM_NOT_FOUND", "Комната не найдена", List.of()));
    }

    @ExceptionHandler(SlotNotFoundException.class)
    ResponseEntity<ErrorResponse> handleSlotNotFound (SlotNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("SLOT_NOT_FOUND", "Слот не найден", List.of()));
    }

    @ExceptionHandler(BookingNotFoundException.class)
    ResponseEntity<ErrorResponse> handleBookingNotFound (BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("BOOKING_NOT_FOUND", "Бронирование не найдено", List.of()));
    }

    @ExceptionHandler(SlotAlreadyBookedException.class)
    ResponseEntity<ErrorResponse> handleSlotAlreadyBooked (SlotAlreadyBookedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("SLOT_ALREADY_BOOKED", "Слот уже забронирован", List.of()));
    }

    @ExceptionHandler(SlotAlreadyStartedException.class)
    ResponseEntity<ErrorResponse> handleSlotAlreadyStarted (SlotAlreadyStartedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("SLOT_ALREADY_STARTED", "Слот уже начался. Создать бронь нельзя", List.of()));
    }

    @ExceptionHandler(CancellationClosedException.class)
    ResponseEntity<ErrorResponse> handleCancellationClosed (CancellationClosedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CANCELLATION_CLOSED", "Время отмены активной брони истекло", List.of()));
    }

    @ExceptionHandler(SlotAlreadyExistsException.class)
    ResponseEntity<ErrorResponse> handleSlotAlreadyExists (SlotAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("SLOT_ALREADY_EXISTS", "Слот этой комнаты с таким началом уже существует", List.of()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpectedException (Exception ex) {
        logger.error("Ошибка при обработке запроса", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Внутренняя ошибка сервиса", List.of()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<FieldErrorResponse> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        return handleExceptionInternal(ex, validationError(fieldErrors), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ErrorResponse body = validationError(ex.getParameterName(), "Обязательный параметр отсутствует");
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        if (ex instanceof MethodArgumentTypeMismatchException argumentMismatch) {
            ErrorResponse body = validationError(argumentMismatch.getName(), "Значение параметра имеет неверный формат");
            return handleExceptionInternal(ex, body, headers, status, request);
        }
        return super.handleTypeMismatch(ex, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ErrorResponse body = switch (ex.getCause()) {
            case null -> new ErrorResponse("VALIDATION_ERROR", "Тело запроса обязательно", List.of());
            case UnrecognizedPropertyException unknown -> validationError(unknown.getPropertyName(), "Неизвестное поле");
            case InputCoercionException coercion -> invalidJsonValue(coercion);
            case StreamReadException syntax -> new ErrorResponse(
                    "MALFORMED_JSON", "Не удалось разобрать тело запроса", List.of()
            );
            case JacksonException jackson -> invalidJsonValue(jackson);
            default -> validationError(List.of());
        };

        return handleExceptionInternal(ex, body, headers, status, request);
    }

    private ErrorResponse invalidJsonValue(JacksonException ex) {
        String field = ex.getPath().stream()
                .map(JacksonException.Reference::getPropertyName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));
        if (field.isEmpty()) {
            return validationError(List.of());
        }
        return validationError(field, "Значение имеет неверный тип или формат");
    }

    @ExceptionHandler(InvalidFieldException.class)
    ResponseEntity<ErrorResponse> handleInvalidField(InvalidFieldException ex) {
        return ResponseEntity.badRequest().body(validationError(ex.getField(), ex.getMessage()));
    }

    private ErrorResponse validationError(String field, String message) {
        return validationError(List.of(new FieldErrorResponse(field, message)));
    }

    private ErrorResponse validationError(List<FieldErrorResponse> fieldErrors) {
        return new ErrorResponse("VALIDATION_ERROR", "Некорректные данные запроса", fieldErrors);
    }
}
