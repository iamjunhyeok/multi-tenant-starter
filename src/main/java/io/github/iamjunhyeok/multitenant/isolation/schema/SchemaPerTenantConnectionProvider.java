package io.github.iamjunhyeok.multitenant.isolation.schema;

import io.github.iamjunhyeok.multitenant.config.property.TenantProperties;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

@RequiredArgsConstructor
public class SchemaPerTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

  private final DataSource dataSource;
  private final TenantProperties tenantProperties;

  @Override
  public Connection getAnyConnection() throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public void releaseAnyConnection(Connection connection) throws SQLException {
    connection.close();
  }

  @Override
  public Connection getConnection(String tenantIdentifier) throws SQLException {
    Connection connection = getAnyConnection();
    connection.setSchema(tenantIdentifier);
    return connection;
  }

  @Override
  public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
    connection.setSchema(tenantProperties.getSchema().getDefaultSchema());
    releaseAnyConnection(connection);
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return false;
  }

  @Override
  public boolean isUnwrappableAs(Class<?> unwrapType) {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    throw new UnsupportedOperationException("Cannot unwrap " + unwrapType);
  }

}
