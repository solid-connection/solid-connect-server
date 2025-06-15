package com.example.solidconnection.common.exception;

import com.example.solidconnection.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

import static com.example.solidconnection.common.exception.ErrorCode.DATA_INTEGRITY_VIOLATION;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_INPUT;
import static com.example.solidconnection.common.exception.ErrorCode.JSON_PARSING_FAILED;
import static com.example.solidconnection.common.exception.ErrorCode.JWT_EXCEPTION;
import static com.example.solidconnection.common.exception.ErrorCode.NOT_DEFINED_ERROR;

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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String errorMessage = ex.getMessage();
        log.error("데이터 무결성 제약조건 위반 예외 발생 : {}", errorMessage);
        ErrorResponse errorResponse = new ErrorResponse(DATA_INTEGRITY_VIOLATION, errorMessage);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
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
        log.error("서버 내부 예외 발생 : {}", errorMessage);
        ErrorResponse errorResponse = new ErrorResponse(NOT_DEFINED_ERROR, errorMessage);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
