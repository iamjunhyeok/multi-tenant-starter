package io.github.iamjunhyeok.multitenant.config;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.isolation.schema.SchemaPerTenantConnectionProvider;
import io.github.iamjunhyeok.multitenant.isolation.schema.TenantIdentifierResolver;
import java.util.Map;
import javax.sql.DataSource;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "org.hibernate.cfg.AvailableSettings")
@ConditionalOnProperty(prefix = "tenant", name = "isolation", havingValue = "SCHEMA")
public class TenantSchemaAutoConfig {

  @Bean
  public TenantIdentifierResolver tenantIdentifierResolver(TenantProperties tenantProperties) {
    return new TenantIdentifierResolver(tenantProperties);
  }

  @Bean
  public SchemaPerTenantConnectionProvider schemaPerTenantConnectionProvider(
      DataSource dataSource, TenantProperties tenantProperties) {
    return new SchemaPerTenantConnectionProvider(dataSource, tenantProperties);
  }

  @Bean
  public HibernatePropertiesCustomizer tenantSchemaHibernateCustomizer(
      TenantIdentifierResolver identifierResolver,
      SchemaPerTenantConnectionProvider connectionProvider) {
    return (Map<String, Object> properties) -> {
      properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
      properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, identifierResolver);
    };
  }

}
