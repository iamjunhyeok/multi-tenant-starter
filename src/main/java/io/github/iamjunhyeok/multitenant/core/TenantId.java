package io.github.iamjunhyeok.multitenant.core;

public record TenantId(String value) {

  public TenantId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("테넌트 ID는 필수값입니다");
    }
    this.value = value;
  }
}
