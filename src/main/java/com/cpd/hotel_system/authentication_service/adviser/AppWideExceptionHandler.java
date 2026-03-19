package com.cpd.hotel_system.authentication_service.adviser;

import com.cpd.hotel_system.authentication_service.exception.BadRequestException;
import com.cpd.hotel_system.authentication_service.exception.EntryNotFoundException;
import com.cpd.hotel_system.authentication_service.util.StandardResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppWideExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<StandardResponseDTO> handleBadRequestException(BadRequestException ex){
        return new ResponseEntity<StandardResponseDTO>(
                new StandardResponseDTO(
                        400,
                        ex.getMessage(),
                        ex
                ), HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(EntryNotFoundException.class)
    public ResponseEntity<StandardResponseDTO> handleEntryNotFoundException(EntryNotFoundException ex){
        return new ResponseEntity<StandardResponseDTO>(
                new StandardResponseDTO(
                        404,
                        ex.getMessage(),
                        ex),
                HttpStatus.NOT_FOUND
        );
    }
}
