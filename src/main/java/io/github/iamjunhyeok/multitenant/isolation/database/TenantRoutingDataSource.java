package io.github.iamjunhyeok.multitenant.isolation.database;

import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

  @Override
  protected Object determineCurrentLookupKey() {
    TenantContext context = TenantContextHolder.getContext();
    return context != null ? context.tenantId().value() : null;
  }

}
