package io.github.iamjunhyeok.multitenant.jpa;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import io.github.iamjunhyeok.multitenant.core.TenantId;
import io.github.iamjunhyeok.multitenant.exception.TenantNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TenantEntityListenerTest {

  private TenantEntityListener listener;

  @BeforeEach
  void setUp() {
    listener = new TenantEntityListener();
  }

  @AfterEach
  void tearDown() {
    TenantContextHolder.clear();
  }

  @Test
  @DisplayName("컨텍스트가 있으면 엔티티에 테넌트 ID를 자동 할당한다")
  void assignsTenantIdFromContext() {
    TenantContextHolder.setContext(new TenantContext(new TenantId("tenant-1")));
    TestEntity entity = new TestEntity();

    listener.setTenantId(entity);

    assertEquals("tenant-1", entity.getTenantId());
  }

  @Test
  @DisplayName("컨텍스트가 없으면 예외가 발생한다")
  void throwsWhenContextMissing() {
    TestEntity entity = new TestEntity();
    assertThrows(TenantNotFoundException.class, () -> listener.setTenantId(entity));
  }

  @Test
  @DisplayName("이미 테넌트 ID가 설정된 엔티티는 덮어쓰지 않는다")
  void doesNotOverwriteExistingTenantId() {
    TenantContextHolder.setContext(new TenantContext(new TenantId("tenant-2")));
    TestEntity entity = new TestEntity();
    entity.assignTenantId("tenant-1");

    listener.setTenantId(entity);

    assertEquals("tenant-1", entity.getTenantId());
  }

  @Test
  @DisplayName("TenantAwareEntity가 아닌 엔티티는 무시한다")
  void ignoresNonTenantAwareEntity() {
    assertDoesNotThrow(() -> listener.setTenantId(new Object()));
  }

  static class TestEntity extends TenantAwareEntity {
  }
}
