package io.github.iamjunhyeok.multitenant.exception;

public class TenantNotValidException extends RuntimeException {

  public TenantNotValidException(String message) {
    super(message);
  }
}
