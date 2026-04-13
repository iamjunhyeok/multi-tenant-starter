package io.github.iamjunhyeok.multitenant.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import io.github.iamjunhyeok.multitenant.core.TenantId;
import io.github.iamjunhyeok.multitenant.core.TenantIdValidator;
import io.github.iamjunhyeok.multitenant.exception.TenantNotValidException;
import io.github.iamjunhyeok.multitenant.resolver.TenantResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantContextFilterTest {

  @Mock
  private TenantResolver tenantResolver;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private TenantContextFilter filter;

  @BeforeEach
  void setUp() {
    TenantProperties properties = new TenantProperties();
    TenantIdValidator tenantIdValidator = new TenantIdValidator(properties);
    filter = new TenantContextFilter(tenantResolver, tenantIdValidator, properties);
  }

  @AfterEach
  void tearDown() {
    TenantContextHolder.clear();
  }

  @Nested
  @DisplayName("테넌트 컨텍스트 생명주기")
  class ContextLifecycleTest {

    @Test
    @DisplayName("테넌트가 resolve되면 필터 체인 실행 중 컨텍스트가 설정된다")
    void setsContextDuringFilterChain() throws Exception {
      when(tenantResolver.resolve(request)).thenReturn(Optional.of(new TenantId("tenant-1")));

      doAnswer(invocation -> {
        assertNotNull(TenantContextHolder.getContext());
        assertEquals("tenant-1", TenantContextHolder.getContext().tenantId().value());
        return null;
      }).when(filterChain).doFilter(request, response);

      filter.doFilterInternal(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("필터 체인 실행 후 컨텍스트가 정리된다")
    void clearsContextAfterFilterChain() throws Exception {
      when(tenantResolver.resolve(request)).thenReturn(Optional.of(new TenantId("tenant-1")));

      filter.doFilterInternal(request, response, filterChain);

      assertNull(TenantContextHolder.getContext());
    }

    @Test
    @DisplayName("테넌트가 없으면 컨텍스트 없이 필터 체인이 실행된다")
    void proceedsWithoutContextWhenNoTenant() throws Exception {
      when(tenantResolver.resolve(request)).thenReturn(Optional.empty());

      doAnswer(invocation -> {
        assertNull(TenantContextHolder.getContext());
        return null;
      }).when(filterChain).doFilter(request, response);

      filter.doFilterInternal(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("필터 체인에서 예외가 발생해도 컨텍스트가 정리된다")
    void clearsContextOnException() throws Exception {
      when(tenantResolver.resolve(request)).thenReturn(Optional.of(new TenantId("tenant-1")));
      doThrow(new RuntimeException("test error")).when(filterChain).doFilter(request, response);

      assertThrows(RuntimeException.class,
          () -> filter.doFilterInternal(request, response, filterChain));

      assertNull(TenantContextHolder.getContext());
    }
  }

  @Nested
  @DisplayName("테넌트 ID 검증")
  class ValidationTest {

    @Test
    @DisplayName("유효하지 않은 테넌트 ID는 거부되고 필터 체인이 실행되지 않는다")
    void rejectsInvalidTenantId() throws Exception {
      when(tenantResolver.resolve(request)).thenReturn(Optional.of(new TenantId("tenant@invalid!")));

      assertThrows(TenantNotValidException.class,
          () -> filter.doFilterInternal(request, response, filterChain));

      verify(filterChain, never()).doFilter(request, response);
      assertNull(TenantContextHolder.getContext());
    }
  }

  @Nested
  @DisplayName("경로 제외")
  class PathExclusionTest {

    @Test
    @DisplayName("Actuator 경로는 필터를 건너뛴다")
    void skipsExcludedPaths() throws Exception {
      when(request.getRequestURI()).thenReturn("/actuator/health");
      assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("/health 경로는 필터를 건너뛴다")
    void skipsHealthPath() throws Exception {
      when(request.getRequestURI()).thenReturn("/health");
      assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("일반 API 경로는 필터가 적용된다")
    void doesNotSkipNormalPaths() throws Exception {
      when(request.getRequestURI()).thenReturn("/api/orders");
      assertFalse(filter.shouldNotFilter(request));
    }
  }
}
