package com.example.solidconnection.custom.exception;

import com.example.solidconnection.custom.response.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_INPUT;
import static com.example.solidconnection.custom.exception.ErrorCode.JSON_PARSING_FAILED;
import static com.example.solidconnection.custom.exception.ErrorCode.JWT_EXCEPTION;
import static com.example.solidconnection.custom.exception.ErrorCode.NOT_DEFINED_ERROR;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        log.error("커스텀 예외 발생 : {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex);
        return ResponseEntity
                .status(ex.getCode())
                .body(errorResponse);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException ex) {
        String errorMessage = ex.getValue() + " 은(는) 유효하지 않은 값입니다.";
        log.error("JSON 파싱 예외 발생 : {}", errorMessage);
        ErrorResponse errorResponse = new ErrorResponse(JSON_PARSING_FAILED, errorMessage);
        return ResponseEntity
                .status(JSON_PARSING_FAILED.getCode())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError -> errors.add(fieldError.getDefaultMessage()));

        String errorMessage = errors.toString();
        log.error("입력값 검증 예외 발생 : {}", errorMessage);
        ErrorResponse errorResponse = new ErrorResponse(INVALID_INPUT, errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Object> handleJwtException(JwtException ex) {
        String errorMessage = ex.getMessage();
        log.error("JWT 예외 발생 : {}", errorMessage);
        ErrorResponse errorResponse = new ErrorResponse(JWT_EXCEPTION, errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleOtherException(Exception ex) {
        String errorMessage = ex.getMessage();
        log.error("알 수 없는 예외 발생 : {}", errorMessage);
        ErrorResponse errorResponse = new ErrorResponse(NOT_DEFINED_ERROR, errorMessage);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
