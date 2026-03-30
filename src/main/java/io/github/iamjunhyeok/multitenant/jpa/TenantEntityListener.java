package io.github.iamjunhyeok.multitenant.jpa;

import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import io.github.iamjunhyeok.multitenant.exception.TenantNotFoundException;
import jakarta.persistence.PrePersist;

public class TenantEntityListener {

  @PrePersist
  public void setTenantId(Object entity) {
    if (!(entity instanceof TenantAwareEntity tenantEntity)) {
      return;
    }
    if (tenantEntity.getTenantId() != null) {
      return;
    }
    TenantContext context = TenantContextHolder.getContext();
    if (context == null) {
      throw new TenantNotFoundException("Tenant context is not set during entity persist");
    }
    tenantEntity.assignTenantId(context.tenantId().value());
  }

}
