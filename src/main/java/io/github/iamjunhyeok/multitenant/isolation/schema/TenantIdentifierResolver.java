package io.github.iamjunhyeok.multitenant.isolation.schema;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import io.github.iamjunhyeok.multitenant.exception.TenantNotFoundException;
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
    String schema = mappings.get(tenantId);
    if (schema == null) {
      throw new TenantNotFoundException("Unknown tenant: " + tenantId);
    }
    return schema;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }

}
