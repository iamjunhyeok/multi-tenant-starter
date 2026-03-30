package io.github.iamjunhyeok.multitenant.config.property;

import io.github.iamjunhyeok.multitenant.constant.TenantResolverStrategy;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tenant")
public class TenantProperties {

  private boolean enabled = true;

  private final Resolver resolver = new Resolver();

  private final Filter filter = new Filter();

  private final Logging logging = new Logging();

  @Getter
  @Setter
  public static class Resolver {
    private TenantResolverStrategy strategy = TenantResolverStrategy.HEADER;
    private String headerName = "X-Tenant-ID";
    private String jwtClaimName = "tenant_id";
  }

  @Getter
  @Setter
  public static class Filter {
    private List<String> excludePaths = new ArrayList<>(List.of("/actuator/**", "/health"));
    private int order = -100;
  }

  @Getter
  @Setter
  public static class Logging {
    private String mdcKey = "tenantId";
  }
}
