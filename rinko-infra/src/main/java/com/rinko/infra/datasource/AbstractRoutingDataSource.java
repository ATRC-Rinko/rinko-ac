package com.rinko.infra.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多数据源路由抽象类。
 * 子类实现 {@link #determineCurrentLookupKey()} 来决定当前请求使用哪个数据源。
 */
public abstract class AbstractRoutingDataSource implements DataSource {

    private static final Logger log = LoggerFactory.getLogger(AbstractRoutingDataSource.class);

    private DataSource defaultTargetDataSource;
    private final Map<Object, DataSource> targetDataSources = new ConcurrentHashMap<>();

    /**
     * 决定当前使用的数据源 key。子类必须实现此方法。
     *
     * @return 当前数据源查找键
     */
    protected abstract Object determineCurrentLookupKey();

    public void setDefaultTargetDataSource(DataSource defaultTargetDataSource) {
        this.defaultTargetDataSource = defaultTargetDataSource;
    }

    public void setTargetDataSources(Map<Object, DataSource> targetDataSources) {
        this.targetDataSources.putAll(targetDataSources);
    }

    private DataSource determineDataSource() {
        Object lookupKey = determineCurrentLookupKey();
        if (lookupKey == null) {
            return defaultTargetDataSource;
        }
        DataSource dataSource = targetDataSources.get(lookupKey);
        if (dataSource == null) {
            log.warn("No DataSource found for key '{}', falling back to default.", lookupKey);
            return defaultTargetDataSource;
        }
        return dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return determineDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineDataSource().getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return determineDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        determineDataSource().setLogWriter(out);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return determineDataSource().getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        determineDataSource().setLoginTimeout(seconds);
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return determineDataSource().getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return determineDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return determineDataSource().isWrapperFor(iface);
    }
}
