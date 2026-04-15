package io.github.iamjunhyeok.multitenant.web;

import io.github.iamjunhyeok.multitenant.exception.TenantNotFoundException;
import io.github.iamjunhyeok.multitenant.exception.TenantNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TenantExceptionHandler {

  @ExceptionHandler(TenantNotFoundException.class)
  public ResponseEntity<TenantErrorResponse> handleTenantNotFound(TenantNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new TenantErrorResponse("TENANT_NOT_FOUND", ex.getMessage()));
  }

  @ExceptionHandler(TenantNotValidException.class)
  public ResponseEntity<TenantErrorResponse> handleTenantNotValid(TenantNotValidException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new TenantErrorResponse("TENANT_NOT_VALID", ex.getMessage()));
  }
}
