package io.github.iamjunhyeok.multitenant.config;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.resolver.HeaderTenantResolver;
import io.github.iamjunhyeok.multitenant.resolver.TenantResolver;
import io.github.iamjunhyeok.multitenant.web.TenantContextFilter;
import io.github.iamjunhyeok.multitenant.web.TenantIdArgumentResolver;
import io.github.iamjunhyeok.multitenant.web.TenantValidationInterceptor;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class TenantWebAutoConfig implements WebMvcConfigurer {

  private final TenantProperties tenantProperties;

  public TenantWebAutoConfig(TenantProperties tenantProperties) {
    this.tenantProperties = tenantProperties;
  }

  @Bean
  @ConditionalOnMissingBean(TenantResolver.class)
  public TenantResolver tenantResolver() {
    return new HeaderTenantResolver(tenantProperties);
  }

  @Bean
  public FilterRegistrationBean<TenantContextFilter> tenantContextFilter(TenantResolver tenantResolver) {
    FilterRegistrationBean<TenantContextFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new TenantContextFilter(tenantResolver, tenantProperties));
    registration.setOrder(tenantProperties.getFilter().getOrder());
    registration.addUrlPatterns("/*");
    return registration;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new TenantValidationInterceptor());
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new TenantIdArgumentResolver());
  }

}
