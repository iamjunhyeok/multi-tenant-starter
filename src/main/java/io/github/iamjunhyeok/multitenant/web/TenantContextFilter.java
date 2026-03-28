package io.github.iamjunhyeok.multitenant.web;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import io.github.iamjunhyeok.multitenant.core.TenantId;
import io.github.iamjunhyeok.multitenant.exception.TenantNotFoundException;
import io.github.iamjunhyeok.multitenant.resolver.TenantResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

  private final TenantResolver tenantResolver;
  private final TenantProperties tenantProperties;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {
      TenantId tenantId = tenantResolver.resolve(request).orElseThrow(() -> new TenantNotFoundException("Tenant could not be resolved from request"));
      TenantContextHolder.setContext(new TenantContext(tenantId));
      filterChain.doFilter(request, response);
    } finally {
      TenantContextHolder.clear();
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    return tenantProperties.getFilter().getExcludePaths().stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
  }
}
