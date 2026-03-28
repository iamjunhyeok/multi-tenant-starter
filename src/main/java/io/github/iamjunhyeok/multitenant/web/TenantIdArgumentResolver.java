package io.github.iamjunhyeok.multitenant.web;

import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import io.github.iamjunhyeok.multitenant.core.TenantId;
import io.github.iamjunhyeok.multitenant.exception.TenantNotFoundException;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TenantIdArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return TenantId.class.isAssignableFrom(parameter.getParameterType());
  }

  @Nullable
  @Override
  public Object resolveArgument(MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) throws Exception {
    TenantContext context = TenantContextHolder.getContext();
    if (context == null) {
      throw new TenantNotFoundException("Tenant context is not set");
    }
    return context.tenantId();
  }
}
