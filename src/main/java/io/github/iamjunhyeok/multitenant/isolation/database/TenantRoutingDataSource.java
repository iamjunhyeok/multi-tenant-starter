package io.github.iamjunhyeok.multitenant.isolation.database;

import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import java.util.Map;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

  private Map<String, String> mappings = Map.of();

  public void setMappings(Map<String, String> mappings) {
    this.mappings = mappings;
  }

  @Override
  protected Object determineCurrentLookupKey() {
    TenantContext context = TenantContextHolder.getContext();
    if (context == null) {
      return null;
    }
    String tenantId = context.tenantId().value();
    return mappings.getOrDefault(tenantId, tenantId);
  }

}
