package io.github.iamjunhyeok.multitenant.config;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.jpa.TenantHibernateFilterInterceptor;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnClass(EntityManager.class)
public class TenantJpaAutoConfig implements WebMvcConfigurer {

  private final EntityManager entityManager;
  private final TenantProperties tenantProperties;

  public TenantJpaAutoConfig(EntityManager entityManager, TenantProperties tenantProperties) {
    this.entityManager = entityManager;
    this.tenantProperties = tenantProperties;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new TenantHibernateFilterInterceptor(entityManager, tenantProperties));
  }

}
