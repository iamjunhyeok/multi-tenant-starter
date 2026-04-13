package io.github.iamjunhyeok.multitenant.core;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.exception.TenantNotValidException;
import java.util.regex.Pattern;

public class TenantIdValidator {

  private final TenantProperties tenantProperties;
  private final Pattern compiledPattern;

  public TenantIdValidator(TenantProperties tenantProperties) {
    this.tenantProperties = tenantProperties;
    this.compiledPattern = Pattern.compile(tenantProperties.getId().getPattern());
  }

  public void validate(TenantId tenantId) {
    String value = tenantId.value();
    int maxLength = tenantProperties.getId().getMaxLength();

    if (value.length() > maxLength) {
      throw new TenantNotValidException(
          "Tenant ID exceeds maximum length of " + maxLength + " characters");
    }

    if (!compiledPattern.matcher(value).matches()) {
      throw new TenantNotValidException(
          "Tenant ID contains invalid characters. Allowed pattern: " + compiledPattern.pattern());
    }
  }

}
