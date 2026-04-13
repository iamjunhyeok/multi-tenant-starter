package io.github.iamjunhyeok.multitenant.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.exception.TenantNotValidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TenantIdValidatorTest {

  private TenantIdValidator validator;

  @BeforeEach
  void setUp() {
    validator = new TenantIdValidator(new TenantProperties());
  }

  @Test
  @DisplayName("영숫자 조합의 테넌트 ID는 유효하다")
  void validAlphanumeric() {
    assertDoesNotThrow(() -> validator.validate(new TenantId("tenant1")));
  }

  @Test
  @DisplayName("하이픈과 언더스코어가 포함된 테넌트 ID는 유효하다")
  void validWithHyphenAndUnderscore() {
    assertDoesNotThrow(() -> validator.validate(new TenantId("tenant-1")));
    assertDoesNotThrow(() -> validator.validate(new TenantId("tenant_1")));
  }

  @Test
  @DisplayName("대문자 테넌트 ID는 유효하다")
  void validUpperCase() {
    assertDoesNotThrow(() -> validator.validate(new TenantId("TENANT")));
  }

  @Test
  @DisplayName("최대 길이와 동일한 테넌트 ID는 유효하다")
  void exactMaxLength() {
    String id = "a".repeat(64);
    assertDoesNotThrow(() -> validator.validate(new TenantId(id)));
  }

  @Test
  @DisplayName("최대 길이를 초과하면 예외가 발생한다")
  void exceedsMaxLength() {
    String id = "a".repeat(65);
    assertThrows(TenantNotValidException.class, () -> validator.validate(new TenantId(id)));
  }

  @Test
  @DisplayName("공백이 포함되면 예외가 발생한다")
  void invalidWithSpace() {
    assertThrows(TenantNotValidException.class, () -> validator.validate(new TenantId("tenant 1")));
  }

  @Test
  @DisplayName("특수문자가 포함되면 예외가 발생한다")
  void invalidWithSpecialCharacters() {
    assertThrows(TenantNotValidException.class, () -> validator.validate(new TenantId("tenant@1")));
    assertThrows(TenantNotValidException.class, () -> validator.validate(new TenantId("tenant.1")));
    assertThrows(TenantNotValidException.class, () -> validator.validate(new TenantId("tenant/1")));
  }

  @Test
  @DisplayName("SQL Injection 시도는 거부된다")
  void invalidSqlInjectionAttempt() {
    assertThrows(TenantNotValidException.class,
        () -> validator.validate(new TenantId("'; DROP TABLE users; --")));
  }
}
