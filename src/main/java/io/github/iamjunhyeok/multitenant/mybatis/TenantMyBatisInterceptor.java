package io.github.iamjunhyeok.multitenant.mybatis;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import io.github.iamjunhyeok.multitenant.core.TenantContext;
import io.github.iamjunhyeok.multitenant.core.TenantContextHolder;
import java.sql.Connection;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

@RequiredArgsConstructor
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class TenantMyBatisInterceptor implements Interceptor {

  private final TenantProperties tenantProperties;

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
    String tenantColumn = tenantProperties.getMybatis().getTenantColumn();
    String tenantId = context.tenantId().value();

    String modifiedSql;
    if (commandType == SqlCommandType.SELECT) {
      modifiedSql = addWhereCondition(originalSql, tenantColumn, tenantId);
    } else if (commandType == SqlCommandType.INSERT) {
      modifiedSql = addInsertColumn(originalSql, tenantColumn, tenantId);
    } else if (commandType == SqlCommandType.UPDATE || commandType == SqlCommandType.DELETE) {
      modifiedSql = addWhereCondition(originalSql, tenantColumn, tenantId);
    } else {
      modifiedSql = originalSql;
    }

    metaObject.setValue("delegate.boundSql.sql", modifiedSql);
    return invocation.proceed();
  }

  private String addWhereCondition(String sql, String column, String tenantId) {
    String condition = column + " = '" + tenantId + "'";
    String upperSql = sql.toUpperCase();

    int whereIndex = upperSql.lastIndexOf("WHERE");
    if (whereIndex >= 0) {
      return sql.substring(0, whereIndex + 5) + " " + condition + " AND" + sql.substring(whereIndex + 5);
    }

    int orderByIndex = upperSql.indexOf("ORDER BY");
    int limitIndex = upperSql.indexOf("LIMIT");
    int groupByIndex = upperSql.indexOf("GROUP BY");

    int insertPos = sql.length();
    if (orderByIndex >= 0) insertPos = Math.min(insertPos, orderByIndex);
    if (limitIndex >= 0) insertPos = Math.min(insertPos, limitIndex);
    if (groupByIndex >= 0) insertPos = Math.min(insertPos, groupByIndex);

    return sql.substring(0, insertPos) + " WHERE " + condition + " " + sql.substring(insertPos);
  }

  private String addInsertColumn(String sql, String column, String tenantId) {
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

    String beforeColumnClose = sql.substring(0, closingParen);
    String afterColumnClose = sql.substring(closingParen);

    String withColumn = beforeColumnClose + ", " + column + afterColumnClose;

    int newValuesCloseParen = withColumn.indexOf(')', withColumn.indexOf('(', withColumn.toUpperCase().indexOf("VALUES")));
    String beforeValueClose = withColumn.substring(0, newValuesCloseParen);
    String afterValueClose = withColumn.substring(newValuesCloseParen);

    return beforeValueClose + ", '" + tenantId + "'" + afterValueClose;
  }
}
