package io.github.iamjunhyeok.multitenant.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TenantContextHolderTest {

  @AfterEach
  void tearDown() {
    TenantContextHolder.clear();
  }

  @Test
  @DisplayName("컨텍스트를 설정하면 동일한 값을 반환한다")
  void setAndGetContext() {
    TenantContext context = new TenantContext(new TenantId("tenant-1"));
    TenantContextHolder.setContext(context);
    assertEquals(context, TenantContextHolder.getContext());
  }

  @Test
  @DisplayName("설정하지 않은 상태에서는 null을 반환한다")
  void getContextReturnsNullByDefault() {
    assertNull(TenantContextHolder.getContext());
  }

  @Test
  @DisplayName("clear 호출 시 컨텍스트가 제거된다")
  void clearRemovesContext() {
    TenantContextHolder.setContext(new TenantContext(new TenantId("tenant-1")));
    TenantContextHolder.clear();
    assertNull(TenantContextHolder.getContext());
  }

  @Test
  @DisplayName("다른 스레드에서는 컨텍스트가 공유되지 않는다")
  void threadIsolation() throws InterruptedException {
    TenantContextHolder.setContext(new TenantContext(new TenantId("tenant-1")));

    AtomicReference<TenantContext> childContext = new AtomicReference<>();
    Thread thread = new Thread(() -> childContext.set(TenantContextHolder.getContext()));
    thread.start();
    thread.join();

    assertNull(childContext.get());
  }

  @Test
  @DisplayName("컨텍스트를 덮어쓰면 최신 값이 반환된다")
  void overwriteContext() {
    TenantContextHolder.setContext(new TenantContext(new TenantId("tenant-1")));
    TenantContextHolder.setContext(new TenantContext(new TenantId("tenant-2")));
    assertEquals("tenant-2", TenantContextHolder.getContext().tenantId().value());
  }
}
