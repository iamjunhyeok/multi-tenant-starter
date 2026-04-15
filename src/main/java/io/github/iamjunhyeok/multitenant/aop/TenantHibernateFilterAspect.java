package io.github.iamjunhyeok.multitenant.aop;

import static io.github.iamjunhyeok.multitenant.constant.TenantConstants.TENANT_FILTER_NAME;

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

  @Before("execution(* org.springframework.data.repository.Repository+.*(..))")
  public void enableTenantFilter() {
    TenantContext context = TenantContextHolder.getContext();
    if (context == null) {
      return;
    }
    try {
      Session session = entityManager.unwrap(Session.class);
      session.enableFilter(TENANT_FILTER_NAME)
          .setParameter("tenantId", context.tenantId().value());
    } catch (UnknownFilterException e) {
      log.warn("Hibernate 필터 '{}'를 찾을 수 없습니다. 엔티티가 TenantAwareEntity를 상속하는지 확인하세요.", TENANT_FILTER_NAME);
    }
  }

}
