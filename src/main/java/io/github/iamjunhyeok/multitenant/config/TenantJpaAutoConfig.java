package io.github.iamjunhyeok.multitenant.config;

import io.github.iamjunhyeok.multitenant.aop.TenantHibernateFilterAspect;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(EntityManager.class)
@ConditionalOnProperty(prefix = "tenant", name = "isolation", havingValue = "ROW", matchIfMissing = true)
public class TenantJpaAutoConfig {

  @Bean
  public TenantHibernateFilterAspect tenantHibernateFilterAspect(EntityManager entityManager) {
    return new TenantHibernateFilterAspect(entityManager);
  }

}
