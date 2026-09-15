package ru.warmhouse.devices.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.warmhouse.devices.api.dto.ErrorResponse;
import ru.warmhouse.devices.domain.DeviceNotFoundException;
import ru.warmhouse.devices.domain.DuplicateSerialException;
import ru.warmhouse.devices.domain.UnknownDeviceTypeException;
import ru.warmhouse.devices.domain.UnsupportedCommandException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DeviceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFound(DeviceNotFoundException e) {
        return new ErrorResponse("device_not_found", e.getMessage());
    }

    @ExceptionHandler(DuplicateSerialException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse duplicate(DuplicateSerialException e) {
        return new ErrorResponse("duplicate_serial", e.getMessage());
    }

    @ExceptionHandler(UnknownDeviceTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse unknownType(UnknownDeviceTypeException e) {
        return new ErrorResponse("unknown_device_type", e.getMessage());
    }

    @ExceptionHandler(UnsupportedCommandException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse unsupported(UnsupportedCommandException e) {
        return new ErrorResponse("unsupported_command", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");
        return new ErrorResponse("validation_error", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse malformed(HttpMessageNotReadableException e) {
        return new ErrorResponse("malformed_json", "Request body could not be parsed");
    }
}
