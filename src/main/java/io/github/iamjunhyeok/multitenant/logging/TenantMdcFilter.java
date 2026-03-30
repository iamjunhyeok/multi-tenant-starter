package io.github.iamjunhyeok.multitenant.logging;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class TenantMdcFilter extends OncePerRequestFilter {

  private final TenantProperties tenantProperties;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String mdcKey = tenantProperties.getLogging().getMdcKey();
    try {
      TenantContext context = TenantContextHolder.getContext();
      if (context != null) {
        MDC.put(mdcKey, context.tenantId().value());
      }
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(mdcKey);
    }
  }

}
