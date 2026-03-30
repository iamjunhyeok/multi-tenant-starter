package io.github.iamjunhyeok.multitenant.config;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.logging.TenantMdcFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class TenantLoggingAutoConfig {

  @Bean
  public FilterRegistrationBean<TenantMdcFilter> tenantMdcFilter(TenantProperties tenantProperties) {
    FilterRegistrationBean<TenantMdcFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new TenantMdcFilter(tenantProperties));
    registration.setOrder(tenantProperties.getFilter().getOrder() + 1);
    registration.addUrlPatterns("/*");
    return registration;
  }

}
