package io.github.iamjunhyeok.multitenant.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Getter
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@EntityListeners(TenantEntityListener.class)
public abstract class BaseTenantEntity {

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  /**
   * Package-private method for TenantEntityListener to assign tenant ID on persist.
   * Not exposed as a public setter to prevent accidental tenant reassignment.
   */
  void assignTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

}
