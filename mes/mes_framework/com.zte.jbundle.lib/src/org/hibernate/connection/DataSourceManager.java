package org.hibernate.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataSourceManager {

    public static final DataSourceManager instance = new DataSourceManager();

    private DataSourceManager() {
    }

    private Map<String, ClosableDataSource> dataSources = new ConcurrentHashMap<String, ClosableDataSource>();

    public void clearAll() {
        for (ClosableDataSource ds : dataSources.values()) {
            ds.close();
        }
    }

    public void put(String alias, ClosableDataSource ds) {
        dataSources.put(alias, ds);
    }

    public ClosableDataSource get(String alias) {
        return dataSources.get(alias);
    }

}
