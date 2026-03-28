package io.github.iamjunhyeok.multitenant.resolver;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantId;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HeaderTenantResolver implements TenantResolver {

  private final TenantProperties tenantProperties;

  @Override
  public Optional<TenantId> resolve(HttpServletRequest request) {
    String value = request.getHeader(tenantProperties.getResolver().getHeaderName());
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(new TenantId(value));
  }
}
