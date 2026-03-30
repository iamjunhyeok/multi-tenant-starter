package io.github.iamjunhyeok.multitenant.config;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(TenantProperties.class)
@ConditionalOnProperty(prefix = "tenant", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({TenantWebAutoConfig.class, TenantJpaAutoConfig.class, TenantMyBatisAutoConfig.class, TenantLoggingAutoConfig.class})
public class TenantAutoConfig {

}
