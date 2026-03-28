package io.github.iamjunhyeok.multitenant.resolver;

import io.github.iamjunhyeok.multitenant.core.TenantId;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface TenantResolver {

  Optional<TenantId> resolve(HttpServletRequest request);

}
