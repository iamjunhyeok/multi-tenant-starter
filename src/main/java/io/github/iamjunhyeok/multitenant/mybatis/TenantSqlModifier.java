package io.github.iamjunhyeok.multitenant.mybatis;

import static io.github.iamjunhyeok.multitenant.constant.TenantConstants.TENANT_COLUMN;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.update.Update;

public class TenantSqlModifier {

  public SqlModifyResult modify(String sql) throws JSQLParserException {
    Statement statement = CCJSqlParserUtil.parse(sql);
    int addedParams = 0;

    if (statement instanceof Select select) {
      addedParams = processSelect(select);
    } else if (statement instanceof Update update) {
      addedParams = processUpdate(update);
    } else if (statement instanceof Delete delete) {
      addedParams = processDelete(delete);
    } else if (statement instanceof Insert insert) {
      addedParams = processInsert(insert);
    }

    return new SqlModifyResult(statement.toString(), addedParams);
  }

  private int processSelect(Select select) {
    if (select instanceof SetOperationList setOp) {
      int count = 0;
      for (Select s : setOp.getSelects()) {
        count += processSelect(s);
      }
      return count;
    }
    if (select instanceof PlainSelect plainSelect) {
      return processPlainSelect(plainSelect);
    }
    return 0;
  }

  private int processPlainSelect(PlainSelect plainSelect) {
    int count = 0;

    // FROM 테이블에 tenant 조건 추가
    FromItem fromItem = plainSelect.getFromItem();
    if (fromItem instanceof Table table) {
      plainSelect.setWhere(appendTenantCondition(plainSelect.getWhere(), table));
      count++;
    }

    // JOIN 테이블에도 tenant 조건 추가
    List<Join> joins = plainSelect.getJoins();
    if (joins != null) {
      for (Join join : joins) {
        if (join.getFromItem() instanceof Table joinTable) {
          plainSelect.setWhere(appendTenantCondition(plainSelect.getWhere(), joinTable));
          count++;
        }
      }
    }

    return count;
  }

  private int processUpdate(Update update) {
    Table table = update.getTable();
    update.setWhere(appendTenantCondition(update.getWhere(), table));
    return 1;
  }

  private int processDelete(Delete delete) {
    Table table = delete.getTable();
    delete.setWhere(appendTenantCondition(delete.getWhere(), table));
    return 1;
  }

  private int processInsert(Insert insert) {
    // 컬럼 목록에 tenant_id 추가
    List<Column> columns = insert.getColumns();
    if (columns != null) {
      columns.add(new Column(TENANT_COLUMN));
    }

    // VALUES에 ? 플레이스홀더 추가
    if (insert.getValues() != null) {
      Values values = insert.getValues();
      @SuppressWarnings("unchecked")
      List<net.sf.jsqlparser.expression.Expression> expressions =
          (List<net.sf.jsqlparser.expression.Expression>) values.getExpressions();
      expressions.add(new JdbcParameter());
    }

    return 1;
  }

  private net.sf.jsqlparser.expression.Expression appendTenantCondition(
      net.sf.jsqlparser.expression.Expression existing, Table table) {
    String alias = table.getAlias() != null ? table.getAlias().getName() : null;
    String columnRef = alias != null ? alias + "." + TENANT_COLUMN : TENANT_COLUMN;

    EqualsTo tenantCondition = new EqualsTo();
    tenantCondition.setLeftExpression(new Column(columnRef));
    tenantCondition.setRightExpression(new JdbcParameter());

    if (existing == null) {
      return tenantCondition;
    }
    return new AndExpression(tenantCondition, existing);
  }

  public record SqlModifyResult(String sql, int addedParamCount) {

  }

}
