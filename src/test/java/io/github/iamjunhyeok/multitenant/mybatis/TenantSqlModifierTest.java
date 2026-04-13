package io.github.iamjunhyeok.multitenant.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.iamjunhyeok.multitenant.mybatis.TenantSqlModifier.SqlModifyResult;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TenantSqlModifierTest {

  private TenantSqlModifier modifier;

  @BeforeEach
  void setUp() {
    modifier = new TenantSqlModifier();
  }

  @Nested
  @DisplayName("SELECT")
  class SelectTest {

    @Test
    @DisplayName("단순 SELECT에 테넌트 조건을 추가한다")
    void simpleSelect() throws JSQLParserException {
      SqlModifyResult result = modifier.modify("SELECT * FROM orders");
      assertContains(result.sql(), "tenant_id = ?");
      assertEquals(1, result.addedParamCount());
    }

    @Test
    @DisplayName("WHERE 절이 있는 SELECT에 테넌트 조건을 추가한다")
    void selectWithWhere() throws JSQLParserException {
      SqlModifyResult result = modifier.modify("SELECT * FROM orders WHERE status = ?");
      assertContains(result.sql(), "tenant_id = ?");
      assertContains(result.sql(), "status = ?");
      assertEquals(1, result.addedParamCount());
    }

    @Test
    @DisplayName("테이블 alias가 있으면 alias.tenant_id 조건을 추가한다")
    void selectWithAlias() throws JSQLParserException {
      SqlModifyResult result = modifier.modify("SELECT * FROM orders o WHERE o.status = ?");
      assertContains(result.sql(), "o.tenant_id = ?");
      assertEquals(1, result.addedParamCount());
    }

    @Test
    @DisplayName("JOIN 시 모든 테이블에 테넌트 조건을 추가한다")
    void selectWithJoin() throws JSQLParserException {
      SqlModifyResult result = modifier.modify(
          "SELECT * FROM orders o JOIN items i ON o.id = i.order_id");
      assertContains(result.sql(), "o.tenant_id = ?");
      assertContains(result.sql(), "i.tenant_id = ?");
      assertEquals(2, result.addedParamCount());
    }

    @Test
    @DisplayName("LEFT JOIN 시에도 모든 테이블에 테넌트 조건을 추가한다")
    void selectWithLeftJoin() throws JSQLParserException {
      SqlModifyResult result = modifier.modify(
          "SELECT * FROM orders o LEFT JOIN items i ON o.id = i.order_id");
      assertContains(result.sql(), "o.tenant_id = ?");
      assertContains(result.sql(), "i.tenant_id = ?");
      assertEquals(2, result.addedParamCount());
    }

    @Test
    @DisplayName("UNION의 각 SELECT에 테넌트 조건을 추가한다")
    void selectWithUnion() throws JSQLParserException {
      SqlModifyResult result = modifier.modify(
          "SELECT * FROM orders UNION SELECT * FROM archive_orders");
      assertEquals(2, result.addedParamCount());
    }
  }

  @Nested
  @DisplayName("서브쿼리")
  class SubqueryTest {

    @Test
    @DisplayName("IN 서브쿼리 내부 테이블에도 테넌트 조건을 추가한다")
    void selectWithInSubquery() throws JSQLParserException {
      SqlModifyResult result = modifier.modify(
          "SELECT * FROM orders WHERE id IN (SELECT order_id FROM items)");
      assertEquals(2, result.addedParamCount());
    }

    @Test
    @DisplayName("EXISTS 서브쿼리 내부 테이블에도 테넌트 조건을 추가한다")
    void selectWithExistsSubquery() throws JSQLParserException {
      SqlModifyResult result = modifier.modify(
          "SELECT * FROM orders o WHERE EXISTS (SELECT 1 FROM items i WHERE i.order_id = o.id)");
      assertEquals(2, result.addedParamCount());
    }

    @Test
    @DisplayName("NOT EXISTS 서브쿼리 내부 테이블에도 테넌트 조건을 추가한다")
    void selectWithNotExistsSubquery() throws JSQLParserException {
      SqlModifyResult result = modifier.modify(
          "SELECT * FROM orders o WHERE NOT EXISTS (SELECT 1 FROM items i WHERE i.order_id = o.id)");
      assertEquals(2, result.addedParamCount());
    }

    @Test
    @DisplayName("FROM 절 서브쿼리 내부 테이블에도 테넌트 조건을 추가한다")
    void selectWithFromSubquery() throws JSQLParserException {
      SqlModifyResult result = modifier.modify(
          "SELECT * FROM (SELECT * FROM orders) sub");
      assertEquals(1, result.addedParamCount());
    }

    @Test
    @DisplayName("스칼라 서브쿼리 내부 테이블에도 테넌트 조건을 추가한다")
    void selectWithScalarSubquery() throws JSQLParserException {
      SqlModifyResult result = modifier.modify(
          "SELECT * FROM orders WHERE amount > (SELECT AVG(amount) FROM orders)");
      assertEquals(2, result.addedParamCount());
    }
  }

  @Nested
  @DisplayName("INSERT")
  class InsertTest {

    @Test
    @DisplayName("INSERT 문에 tenant_id 컬럼과 값을 추가한다")
    void insert() throws JSQLParserException {
      SqlModifyResult result = modifier.modify("INSERT INTO orders (name) VALUES (?)");
      assertContains(result.sql(), "tenant_id");
      assertEquals(1, result.addedParamCount());
    }
  }

  @Nested
  @DisplayName("UPDATE")
  class UpdateTest {

    @Test
    @DisplayName("UPDATE 문에 테넌트 조건을 추가한다")
    void update() throws JSQLParserException {
      SqlModifyResult result = modifier.modify("UPDATE orders SET name = ? WHERE id = ?");
      assertContains(result.sql(), "tenant_id = ?");
      assertEquals(1, result.addedParamCount());
    }

    @Test
    @DisplayName("UPDATE의 WHERE 서브쿼리 내부에도 테넌트 조건을 추가한다")
    void updateWithSubqueryInWhere() throws JSQLParserException {
      SqlModifyResult result = modifier.modify(
          "UPDATE orders SET status = ? WHERE id IN (SELECT order_id FROM items WHERE qty = 0)");
      assertEquals(2, result.addedParamCount());
    }
  }

  @Nested
  @DisplayName("DELETE")
  class DeleteTest {

    @Test
    @DisplayName("DELETE 문에 테넌트 조건을 추가한다")
    void delete() throws JSQLParserException {
      SqlModifyResult result = modifier.modify("DELETE FROM orders WHERE id = ?");
      assertContains(result.sql(), "tenant_id = ?");
      assertEquals(1, result.addedParamCount());
    }

    @Test
    @DisplayName("DELETE의 WHERE 서브쿼리 내부에도 테넌트 조건을 추가한다")
    void deleteWithSubqueryInWhere() throws JSQLParserException {
      SqlModifyResult result = modifier.modify(
          "DELETE FROM orders WHERE id IN (SELECT order_id FROM items WHERE qty = 0)");
      assertEquals(2, result.addedParamCount());
    }
  }

  private void assertContains(String sql, String expected) {
    assertTrue(sql.contains(expected),
        "Expected SQL to contain '" + expected + "' but was: " + sql);
  }
}
