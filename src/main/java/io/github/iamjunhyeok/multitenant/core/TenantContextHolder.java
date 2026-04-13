package io.github.iamjunhyeok.multitenant.core;

public final class TenantContextHolder {

  private static final ThreadLocal<TenantContext> holder = new ThreadLocal<>();

  private TenantContextHolder() {
  }

  public static void setContext(TenantContext context) {
    holder.set(context);
  }

  public static TenantContext getContext() {
    return holder.get();
  }

  public static void clear() {
    holder.remove();
  }
}
