package io.github.iamjunhyeok.multitenant.config;

import io.github.iamjunhyeok.multitenant.mybatis.TenantMyBatisInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = {"org.apache.ibatis.plugin.Interceptor", "net.sf.jsqlparser.parser.CCJSqlParserUtil"})
@ConditionalOnProperty(prefix = "tenant", name = "isolation", havingValue = "ROW", matchIfMissing = true)
public class TenantMyBatisAutoConfig {

  @Bean
  public TenantMyBatisInterceptor tenantMyBatisInterceptor() {
    return new TenantMyBatisInterceptor();
  }

}
