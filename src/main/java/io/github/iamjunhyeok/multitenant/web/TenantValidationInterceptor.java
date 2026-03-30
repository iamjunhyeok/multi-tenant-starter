package io.github.iamjunhyeok.multitenant.web;

import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import io.github.iamjunhyeok.multitenant.exception.TenantNotValidException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class TenantValidationInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }
    boolean required = handlerMethod.hasMethodAnnotation(TenantRequired.class)
        || handlerMethod.getBeanType().isAnnotationPresent(TenantRequired.class);
    if (required && TenantContextHolder.getContext() == null) {
      throw new TenantNotValidException("@TenantRequired: tenant context is missing");
    }
    return true;
  }
}
