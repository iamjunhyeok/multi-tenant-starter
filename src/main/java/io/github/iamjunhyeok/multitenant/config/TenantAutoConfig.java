package io.github.iamjunhyeok.multitenant.config;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(TenantProperties.class)
public class TenantAutoConfig {

}
