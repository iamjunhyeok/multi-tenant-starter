package io.github.iamjunhyeok.multitenant.config;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.config.property.TenantProperties.DataSourceProperty;
import io.github.iamjunhyeok.multitenant.isolation.database.TenantRoutingDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(prefix = "tenant", name = "isolation", havingValue = "DATABASE")
public class TenantDatabaseAutoConfig {

  @Bean
  @Primary
  public DataSource tenantRoutingDataSource(TenantProperties tenantProperties) {
    TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource();

    Map<Object, Object> targetDataSources = new HashMap<>();
    DataSource defaultDataSource = null;

    for (Map.Entry<String, DataSourceProperty> entry :
        tenantProperties.getDatabase().getDatasources().entrySet()) {
      DataSource ds = buildDataSource(entry.getValue());
      targetDataSources.put(entry.getKey(), ds);
      if (defaultDataSource == null) {
        defaultDataSource = ds;
      }
    }

    routingDataSource.setTargetDataSources(targetDataSources);
    if (defaultDataSource != null) {
      routingDataSource.setDefaultTargetDataSource(defaultDataSource);
    }
    routingDataSource.afterPropertiesSet();

    return routingDataSource;
  }

  private DataSource buildDataSource(DataSourceProperty property) {
    DataSourceBuilder<?> builder = DataSourceBuilder.create()
        .url(property.getUrl())
        .username(property.getUsername())
        .password(property.getPassword());
    if (property.getDriverClassName() != null) {
      builder.driverClassName(property.getDriverClassName());
    }
    return builder.build();
  }

}
