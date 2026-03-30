package io.github.iamjunhyeok.multitenant.config;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.aop.TenantHibernateFilterAspect;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(EntityManager.class)
public class TenantJpaAutoConfig {

  @Bean
  public TenantHibernateFilterAspect tenantHibernateFilterAspect(
      EntityManager entityManager, TenantProperties tenantProperties) {
    return new TenantHibernateFilterAspect(entityManager, tenantProperties);
  }

}
