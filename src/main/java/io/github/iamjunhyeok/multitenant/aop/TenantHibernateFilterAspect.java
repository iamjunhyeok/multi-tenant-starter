package io.github.iamjunhyeok.multitenant.aop;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.hibernate.UnknownFilterException;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class TenantHibernateFilterAspect {

  private final EntityManager entityManager;
  private final TenantProperties tenantProperties;

  @Before("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
  public void enableTenantFilter() {
    TenantContext context = TenantContextHolder.getContext();
    if (context == null) {
      return;
    }
    String filterName = tenantProperties.getJpa().getFilterName();
    try {
      Session session = entityManager.unwrap(Session.class);
      session.enableFilter(filterName)
          .setParameter("tenantId", context.tenantId().value());
    } catch (UnknownFilterException e) {
      log.warn("Hibernate filter '{}' not found. Ensure your entity extends BaseTenantEntity.", filterName);
    }
  }

}
