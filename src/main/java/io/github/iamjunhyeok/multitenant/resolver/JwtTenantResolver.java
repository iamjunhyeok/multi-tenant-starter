package io.github.iamjunhyeok.multitenant.resolver;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantId;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@RequiredArgsConstructor
public class JwtTenantResolver implements TenantResolver {

  private final TenantProperties tenantProperties;

  @Override
  public Optional<TenantId> resolve(HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
      return Optional.empty();
    }
    Jwt jwt = jwtAuth.getToken();
    String value = jwt.getClaimAsString(tenantProperties.getResolver().getJwtClaimName());
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(new TenantId(value));
  }

}
