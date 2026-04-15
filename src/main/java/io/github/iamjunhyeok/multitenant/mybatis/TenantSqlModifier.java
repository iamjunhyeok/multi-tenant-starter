package io.github.iamjunhyeok.multitenant.mybatis;

import static io.github.iamjunhyeok.multitenant.constant.TenantConstants.TENANT_COLUMN;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.update.Update;

public class TenantSqlModifier {

  public SqlModifyResult modify(String sql) throws JSQLParserException {
    Statement statement = CCJSqlParserUtil.parse(sql);
    TenantExpressionVisitor visitor = new TenantExpressionVisitor();
    int addedParams = 0;

    if (statement instanceof Select select) {
      addedParams = visitor.processSelect(select);
    } else if (statement instanceof Update update) {
      addedParams = visitor.processUpdate(update);
    } else if (statement instanceof Delete delete) {
      addedParams = visitor.processDelete(delete);
    } else if (statement instanceof Insert insert) {
      addedParams = processInsert(insert);
    }

    return new SqlModifyResult(statement.toString(), addedParams);
  }

  private int processInsert(Insert insert) {
    List<Column> columns = insert.getColumns();
    if (columns != null) {
      columns.add(new Column(TENANT_COLUMN));
    }

    if (insert.getValues() != null) {
      Values values = insert.getValues();
      @SuppressWarnings("unchecked")
      List<Expression> expressions =
          (List<Expression>) values.getExpressions();
      expressions.add(new JdbcParameter());
    }

    return 1;
  }

  private static Expression appendTenantCondition(Expression existing, Table table) {
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

  static class TenantExpressionVisitor extends ExpressionVisitorAdapter<Void> {

    private int addedParamCount;

    int processSelect(Select select) {
      addedParamCount = 0;
      visitSelect(select);
      return addedParamCount;
    }

    int processUpdate(Update update) {
      addedParamCount = 0;
      update.setWhere(appendTenantCondition(update.getWhere(), update.getTable()));
      addedParamCount++;
      if (update.getWhere() != null) {
        update.getWhere().accept(this, null);
      }
      return addedParamCount;
    }

    int processDelete(Delete delete) {
      addedParamCount = 0;
      delete.setWhere(appendTenantCondition(delete.getWhere(), delete.getTable()));
      addedParamCount++;
      if (delete.getWhere() != null) {
        delete.getWhere().accept(this, null);
      }
      return addedParamCount;
    }

    private void visitSelect(Select select) {
      if (select instanceof PlainSelect ps) {
        visitPlainSelect(ps);
      } else if (select instanceof SetOperationList sol) {
        for (Select s : sol.getSelects()) {
          visitSelect(s);
        }
      } else if (select instanceof ParenthesedSelect paren) {
        visitSelect(paren.getSelect());
      }
    }

    private void visitPlainSelect(PlainSelect ps) {
      FromItem from = ps.getFromItem();
      if (from instanceof Table table) {
        ps.setWhere(appendTenantCondition(ps.getWhere(), table));
        addedParamCount++;
      } else if (from instanceof Select sub) {
        visitSelect(sub);
      }

      List<Join> joins = ps.getJoins();
      if (joins != null) {
        for (Join join : joins) {
          FromItem joinFrom = join.getFromItem();
          if (joinFrom instanceof Table table) {
            ps.setWhere(appendTenantCondition(ps.getWhere(), table));
            addedParamCount++;
          } else if (joinFrom instanceof Select sub) {
            visitSelect(sub);
          }
        }
      }

      if (ps.getWhere() != null) {
        ps.getWhere().accept(this, null);
      }
    }

    @Override
    public <S> Void visit(Select select, S context) {
      visitSelect(select);
      return null;
    }

    @Override
    public <S> Void visit(ParenthesedSelect paren, S context) {
      visitSelect(paren.getSelect());
      return null;
    }

    @Override
    public <S> Void visit(InExpression expr, S context) {
      if (expr.getRightExpression() != null) {
        expr.getRightExpression().accept(this, context);
      }
      return null;
    }

    @Override
    public <S> Void visit(ExistsExpression expr, S context) {
      if (expr.getRightExpression() != null) {
        expr.getRightExpression().accept(this, context);
      }
      return null;
    }

    @Override
    public <S> Void visit(NotExpression expr, S context) {
      expr.getExpression().accept(this, context);
      return null;
    }

    @Override
    protected <S> Void visitBinaryExpression(BinaryExpression expr, S context) {
      expr.getLeftExpression().accept(this, context);
      expr.getRightExpression().accept(this, context);
      return null;
    }
  }

  public record SqlModifyResult(String sql, int addedParamCount) {
  }
}
