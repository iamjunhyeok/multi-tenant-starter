package io.github.iamjunhyeok.multitenant.core;

public record TenantId(String value) {

  public TenantId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Tenant ID cannot be null or blank");
    }
    this.value = value;
  }
}
