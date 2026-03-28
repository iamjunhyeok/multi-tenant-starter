package io.github.iamjunhyeok.multitenant.exception;

public class TenantNotFoundException extends RuntimeException {

  public TenantNotFoundException(String message) {
    super(message);
  }
}
