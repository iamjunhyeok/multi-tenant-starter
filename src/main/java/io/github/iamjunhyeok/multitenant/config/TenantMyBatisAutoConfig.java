package io.github.iamjunhyeok.multitenant.config;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.mybatis.TenantMyBatisInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Interceptor.class)
public class TenantMyBatisAutoConfig {

  @Bean
  public TenantMyBatisInterceptor tenantMyBatisInterceptor(TenantProperties tenantProperties) {
    return new TenantMyBatisInterceptor(tenantProperties);
  }

}
