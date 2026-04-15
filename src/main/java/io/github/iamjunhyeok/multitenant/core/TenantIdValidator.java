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
          "테넌트 ID 최대 길이 초과 (최대: " + maxLength + "자)");
    }

    if (!compiledPattern.matcher(value).matches()) {
      throw new TenantNotValidException(
          "허용되지 않는 테넌트 ID 형식 (허용 패턴: " + compiledPattern.pattern() + ")");
    }
  }

}
