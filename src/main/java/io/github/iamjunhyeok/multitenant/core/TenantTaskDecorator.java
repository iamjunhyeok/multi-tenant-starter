package io.github.iamjunhyeok.multitenant.core;

import org.springframework.core.task.TaskDecorator;

public class TenantTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    TenantContext context = TenantContextHolder.getContext();
    return () -> {
      try {
        if (context != null) {
          TenantContextHolder.setContext(context);
        }
        runnable.run();
      } finally {
        TenantContextHolder.clear();
      }
    };
  }

}
