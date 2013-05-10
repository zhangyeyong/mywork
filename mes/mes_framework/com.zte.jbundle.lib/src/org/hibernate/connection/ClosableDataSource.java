package org.hibernate.connection;

import javax.sql.DataSource;

public interface ClosableDataSource {

    public DataSource dataSource();

    public void close();

}