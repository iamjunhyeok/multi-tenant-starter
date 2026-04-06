package io.github.iamjunhyeok.multitenant.isolation.schema;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

@RequiredArgsConstructor
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

  private final TenantProperties tenantProperties;

  @Override
  public String resolveCurrentTenantIdentifier() {
    TenantContext context = TenantContextHolder.getContext();
    if (context == null) {
      return tenantProperties.getSchema().getDefaultSchema();
    }
    String tenantId = context.tenantId().value();
    Map<String, String> mappings = tenantProperties.getSchema().getMappings();
    return mappings.getOrDefault(tenantId, tenantId);
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }

}
