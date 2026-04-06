package io.github.iamjunhyeok.multitenant.config.property;

import io.github.iamjunhyeok.multitenant.constant.IsolationStrategy;
import io.github.iamjunhyeok.multitenant.constant.TenantResolverStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tenant")
public class TenantProperties {

  private boolean enabled = true;

  private IsolationStrategy isolation = IsolationStrategy.ROW;

  private final Resolver resolver = new Resolver();

  private final Filter filter = new Filter();

  private final Id id = new Id();

  private final Logging logging = new Logging();

  private final Schema schema = new Schema();

  private final Database database = new Database();

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

  @Getter
  @Setter
  public static class Id {
    private int maxLength = 64;
    private String pattern = "^[a-zA-Z0-9_-]+$";
  }

  @Getter
  @Setter
  public static class Schema {
    private String defaultSchema = "public";
  }

  @Getter
  @Setter
  public static class Database {
    private Map<String, DataSourceProperty> datasources = new HashMap<>();
  }

  @Getter
  @Setter
  public static class DataSourceProperty {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
  }
}
