package org.hibernate.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.util.PropertiesHelper;
import org.hibernate.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 基于C3P0数据库连接池的连接提供者，支持多个数据库连接
 * 
 * @author PanJun
 * 
 */
public class ZteC3p0ConnectionProvider implements ConnectionProvider, IPoolConsts {

    static class ClosableC3p0Ds implements ClosableDataSource {

        final ComboPooledDataSource ds;

        public ClosableC3p0Ds(ComboPooledDataSource ds) {
            this.ds = ds;
        }

        @Override
        public DataSource dataSource() {
            return ds;
        }

        @Override
        public void close() {
            ds.close();
        }

    }

    private static final Logger log = LoggerFactory.getLogger(C3P0ConnectionProvider.class);
    private String alias;

    public Connection getConnection() throws SQLException {
        ClosableDataSource ds = DataSourceManager.instance.get(alias);
        if (ds == null) {
            return null;
        }
        Connection ret = ds.dataSource().getConnection();
        return ret;
    }

    public void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    public void configure(Properties props) throws HibernateException {
        alias = PropertiesHelper.getString(POOL_ALIAS, props, "");
        String driverClass = props.getProperty(POOL_DRIVER);
        String jdbcUrl = props.getProperty(POOL_JDBC);
        Properties connectionProps = ConnectionProviderFactory.getConnectionProperties(props);

        log.info("C3P0 using driver: " + driverClass + " at URL: " + jdbcUrl);
        log.info("Connection properties: " + PropertiesHelper.maskOut(connectionProps, "password"));

        if (driverClass == null) {
            log.warn("No JDBC Driver class was specified by property " + Environment.DRIVER);
        } else {
            try {
                Class.forName(driverClass);
            } catch (ClassNotFoundException cnfe) {
                try {
                    ReflectHelper.classForName(driverClass);
                } catch (ClassNotFoundException e) {
                    String msg = "JDBC Driver class not found: " + driverClass;
                    log.error(msg, e);
                    throw new HibernateException(msg, e);
                }
            }
        }

        try {
            Integer poolSize = PropertiesHelper.getInteger(POOL_COUNT, props);
            String user = PropertiesHelper.getString(POOL_USER, props, "");
            String pswd = PropertiesHelper.getString(POOL_PASSWORD, props, "");
            String testSql = PropertiesHelper.getString(POOL_TEST_SQL, props, "");
            int maxPoolSize = poolSize == null || poolSize < 1 ? 1 : poolSize;
            int minPoolSize = maxPoolSize < 2 ? 1 : maxPoolSize / 2;

            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            dataSource.setMaxPoolSize(maxPoolSize);
            dataSource.setMinPoolSize(minPoolSize);
            dataSource.setInitialPoolSize(minPoolSize);
            dataSource.setUser(user);
            dataSource.setPassword(pswd);
            dataSource.setMaxStatementsPerConnection(255);
            dataSource.setJdbcUrl(jdbcUrl);
            dataSource.setDriverClass(driverClass);
            dataSource.setPreferredTestQuery(testSql);
            dataSource.setTestConnectionOnCheckin(true);
            dataSource.setTestConnectionOnCheckout(false);
            dataSource.setIdleConnectionTestPeriod(30);
            dataSource.setAutoCommitOnClose(false);

            DataSourceManager.instance.put(alias, new ClosableC3p0Ds(dataSource));
        } catch (Exception e) {
            log.error("could not instantiate C3P0 connection pool", e);
            throw new HibernateException("Could not instantiate C3P0 connection pool", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        try {
            DataSourceManager.instance.clearAll();
        } catch (Exception sqle) {
            log.warn("could not destroy C3P0 connection pool", sqle);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsAggressiveRelease() {
        return false;
    }

}
