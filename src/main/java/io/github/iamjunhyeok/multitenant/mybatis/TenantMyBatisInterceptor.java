package io.github.iamjunhyeok.multitenant.mybatis;

import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import io.github.iamjunhyeok.multitenant.mybatis.TenantSqlModifier.SqlModifyResult;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

@Slf4j
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class TenantMyBatisInterceptor implements Interceptor {

  private static final String TENANT_PARAM_PREFIX = "_tenantId_";
  private final TenantSqlModifier sqlModifier = new TenantSqlModifier();

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    TenantContext context = TenantContextHolder.getContext();
    if (context == null) {
      return invocation.proceed();
    }

    StatementHandler handler = (StatementHandler) invocation.getTarget();
    MetaObject metaObject = SystemMetaObject.forObject(handler);

    MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
    if (ms == null) {
      return invocation.proceed();
    }
    SqlCommandType commandType = ms.getSqlCommandType();
    if (commandType == null
        || commandType == SqlCommandType.FLUSH
        || commandType == SqlCommandType.UNKNOWN) {
      return invocation.proceed();
    }

    BoundSql boundSql = handler.getBoundSql();
    String originalSql = boundSql.getSql();

    SqlModifyResult result;
    try {
      result = sqlModifier.modify(originalSql);
    } catch (JSQLParserException e) {
      log.warn("Failed to parse SQL for tenant injection, executing original SQL: {}", originalSql, e);
      return invocation.proceed();
    }

    metaObject.setValue("delegate.boundSql.sql", result.sql());

    // 추가된 ? 플레이스홀더 수만큼 ParameterMapping 추가
    List<ParameterMapping> mappings = new ArrayList<>(boundSql.getParameterMappings());
    String tenantId = context.tenantId().value();

    for (int i = 0; i < result.addedParamCount(); i++) {
      String paramKey = TENANT_PARAM_PREFIX + i;
      ParameterMapping tenantMapping = new ParameterMapping.Builder(
          ms.getConfiguration(), paramKey, String.class).build();

      if (commandType == SqlCommandType.INSERT) {
        mappings.add(tenantMapping);
      } else {
        // SELECT/UPDATE/DELETE: tenant 조건이 기존 WHERE 앞에 추가됨
        mappings.add(i, tenantMapping);
      }

      boundSql.setAdditionalParameter(paramKey, tenantId);
    }

    metaObject.setValue("delegate.boundSql.parameterMappings", mappings);

    return invocation.proceed();
  }

}
