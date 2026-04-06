package io.github.iamjunhyeok.multitenant.isolation.schema;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

@RequiredArgsConstructor
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

  private final TenantProperties tenantProperties;

  @Override
  public String resolveCurrentTenantIdentifier() {
    TenantContext context = TenantContextHolder.getContext();
    if (context != null) {
      return context.tenantId().value();
    }
    return tenantProperties.getSchema().getDefaultSchema();
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }

}
