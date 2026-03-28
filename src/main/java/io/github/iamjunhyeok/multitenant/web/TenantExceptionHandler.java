package io.github.iamjunhyeok.multitenant.web;

import io.github.iamjunhyeok.multitenant.exception.TenantNotFoundException;
import io.github.iamjunhyeok.multitenant.exception.TenantNotValidException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TenantExceptionHandler {

  @ExceptionHandler(TenantNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleTenantNotFound(TenantNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", "TENANT_NOT_FOUND", "message", ex.getMessage()));
  }

  @ExceptionHandler(TenantNotValidException.class)
  public ResponseEntity<Map<String, String>> handleTenantNotValid(TenantNotValidException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", "TENANT_NOT_VALID", "message", ex.getMessage()));
  }
}
