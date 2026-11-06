package com.campus.parking.api;

import com.campus.parking.api.ApiModels.ApiError;
import com.campus.parking.service.ParkingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ParkingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleParking(ParkingException exception) {
        return new ApiError(exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(DataIntegrityViolationException exception) {
        return new ApiError("The record conflicts with existing data (for example, a duplicate email or floor/slot number).");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidInput(Exception exception) {
        return new ApiError("The request contains invalid or missing values. Check the field names and allowed enum values.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Exception exception) {
        return new ApiError("The request could not be completed. Check the supplied data and server configuration.");
    }
}