package io.github.iamjunhyeok.multitenant.config.property;

import io.github.iamjunhyeok.multitenant.constant.TenantResolverStrategy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tenant")
public class TenantProperties {

  private boolean enabled = true;

  private final Resolver resolver = new Resolver();

  @Getter
  @Setter
  public static class Resolver {
    private TenantResolverStrategy strategy = TenantResolverStrategy.HEADER;
    private String headerName = "X-Tenant-ID";
  }
}
