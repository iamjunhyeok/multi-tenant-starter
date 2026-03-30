package io.github.iamjunhyeok.multitenant.core;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.exception.TenantNotValidException;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TenantIdValidator {

  private final TenantProperties tenantProperties;

  public void validate(TenantId tenantId) {
    String value = tenantId.value();
    int maxLength = tenantProperties.getId().getMaxLength();
    String patternStr = tenantProperties.getId().getPattern();

    if (value.length() > maxLength) {
      throw new TenantNotValidException(
          "Tenant ID exceeds maximum length of " + maxLength + " characters");
    }

    if (!Pattern.matches(patternStr, value)) {
      throw new TenantNotValidException(
          "Tenant ID contains invalid characters. Allowed pattern: " + patternStr);
    }
  }

}
