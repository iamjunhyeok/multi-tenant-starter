package io.github.iamjunhyeok.multitenant.web;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import io.github.iamjunhyeok.multitenant.core.TenantId;
import io.github.iamjunhyeok.multitenant.core.TenantIdValidator;
import io.github.iamjunhyeok.multitenant.resolver.TenantResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

  private final TenantResolver tenantResolver;
  private final TenantIdValidator tenantIdValidator;
  private final TenantProperties tenantProperties;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {
      Optional<TenantId> tenantId = tenantResolver.resolve(request);
      tenantId.ifPresent(id -> {
        tenantIdValidator.validate(id);
        TenantContextHolder.setContext(new TenantContext(id));
      });
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
