package io.github.iamjunhyeok.multitenant.mybatis;

import static io.github.iamjunhyeok.multitenant.constant.TenantConstants.TENANT_COLUMN;

import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
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

@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class TenantMyBatisInterceptor implements Interceptor {

  private static final String TENANT_PARAM_KEY = "_tenantId";

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    TenantContext context = TenantContextHolder.getContext();
    if (context == null) {
      return invocation.proceed();
    }

    StatementHandler handler = (StatementHandler) invocation.getTarget();
    MetaObject metaObject = SystemMetaObject.forObject(handler);

    MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
    SqlCommandType commandType = ms.getSqlCommandType();

    BoundSql boundSql = handler.getBoundSql();
    String originalSql = boundSql.getSql();

    String modifiedSql;
    if (commandType == SqlCommandType.SELECT
        || commandType == SqlCommandType.UPDATE
        || commandType == SqlCommandType.DELETE) {
      modifiedSql = addWhereCondition(originalSql);
    } else if (commandType == SqlCommandType.INSERT) {
      modifiedSql = addInsertColumn(originalSql);
    } else {
      return invocation.proceed();
    }

    metaObject.setValue("delegate.boundSql.sql", modifiedSql);

    // ParameterMapping 추가 (? 플레이스홀더에 대응)
    ParameterMapping tenantMapping = new ParameterMapping.Builder(
        ms.getConfiguration(), TENANT_PARAM_KEY, String.class).build();

    List<ParameterMapping> mappings = new ArrayList<>(boundSql.getParameterMappings());
    if (commandType == SqlCommandType.SELECT
        || commandType == SqlCommandType.UPDATE
        || commandType == SqlCommandType.DELETE) {
      // WHERE tenant_id = ? AND ... → 기존 파라미터 앞에 추가
      mappings.addFirst(tenantMapping);
    } else {
      // INSERT ... VALUES (..., ?) → 기존 파라미터 뒤에 추가
      mappings.add(tenantMapping);
    }
    metaObject.setValue("delegate.boundSql.parameterMappings", mappings);

    // additionalParameter로 tenant 값 바인딩
    boundSql.setAdditionalParameter(TENANT_PARAM_KEY, context.tenantId().value());

    return invocation.proceed();
  }

  private String addWhereCondition(String sql) {
    String placeholder = TENANT_COLUMN + " = ?";
    String upperSql = sql.toUpperCase();

    int whereIndex = upperSql.lastIndexOf("WHERE");
    if (whereIndex >= 0) {
      return sql.substring(0, whereIndex + 5) + " " + placeholder + " AND" + sql.substring(whereIndex + 5);
    }

    int insertPos = findClausePosition(upperSql, sql.length());
    return sql.substring(0, insertPos) + " WHERE " + placeholder + " " + sql.substring(insertPos);
  }

  private String addInsertColumn(String sql) {
    String upperSql = sql.toUpperCase();
    int valuesIndex = upperSql.indexOf("VALUES");
    if (valuesIndex < 0) {
      return sql;
    }

    int closingParen = sql.indexOf(')', sql.indexOf('('));
    int valuesOpenParen = sql.indexOf('(', valuesIndex);
    int valuesCloseParen = sql.indexOf(')', valuesOpenParen);

    if (closingParen < 0 || valuesOpenParen < 0 || valuesCloseParen < 0) {
      return sql;
    }

    String withColumn = sql.substring(0, closingParen) + ", " + TENANT_COLUMN + sql.substring(closingParen);

    int newValuesCloseParen = withColumn.indexOf(')',
        withColumn.indexOf('(', withColumn.toUpperCase().indexOf("VALUES")));
    return withColumn.substring(0, newValuesCloseParen) + ", ?" + withColumn.substring(newValuesCloseParen);
  }

  private int findClausePosition(String upperSql, int defaultPos) {
    int pos = defaultPos;
    for (String clause : new String[]{"ORDER BY", "GROUP BY", "HAVING", "LIMIT"}) {
      int idx = upperSql.indexOf(clause);
      if (idx >= 0) {
        pos = Math.min(pos, idx);
      }
    }
    return pos;
  }

}
