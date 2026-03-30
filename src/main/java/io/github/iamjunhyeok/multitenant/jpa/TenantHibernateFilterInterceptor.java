package io.github.iamjunhyeok.multitenant.jpa;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class TenantHibernateFilterInterceptor implements HandlerInterceptor {

  private final EntityManager entityManager;
  private final TenantProperties tenantProperties;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    TenantContext context = TenantContextHolder.getContext();
    if (context == null) {
      return true;
    }
    String filterName = tenantProperties.getJpa().getFilterName();
    Session session = entityManager.unwrap(Session.class);
    session.enableFilter(filterName)
        .setParameter("tenantId", context.tenantId().value());
    return true;
  }

}
