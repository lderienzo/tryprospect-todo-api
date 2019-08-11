package com.tryprospect.todo.db;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import io.dropwizard.db.DataSourceFactory;

public final class TodoDaoTestConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(TodoDaoTestConfiguration.class);

    public static final PostgreSQLContainer getRunningInstanceOfPostgres() {
        // TODO: it would be great to read db config info from config.yml
        final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();
        postgreSQLContainer.start();
        return postgreSQLContainer;
    }

    public static final DataSourceFactory getDataSourceFactory(final PostgreSQLContainer container) {
        logContainerConnectionInfo(container);
        return createDataSourceFactoryUsingContainerSettings(container);
    }

    private static void logContainerConnectionInfo(final PostgreSQLContainer container) {
        LOG.info("DB JDBC URL: " + container.getJdbcUrl());
        LOG.info("DB USER NAME: " + container.getUsername());
        LOG.info("DB USER PASSWORD: " + container.getPassword());   // TODO: not sure if this needs to be removed.
        LOG.info("DB DRIVER CLASS NAME: " + container.getDriverClassName());
    }

    private static DataSourceFactory createDataSourceFactoryUsingContainerSettings(final PostgreSQLContainer container) {
        DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setUrl(container.getJdbcUrl());
        dataSourceFactory.setUser(container.getUsername());
        dataSourceFactory.setPassword(container.getPassword());
        dataSourceFactory.setDriverClass(container.getDriverClassName());
        return dataSourceFactory;
    }

    public static final Flyway getFlywayDbMigrationObject(final DataSourceFactory dataSourceFactory) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSourceFactory.getUrl(),
                dataSourceFactory.getUser(), dataSourceFactory.getPassword());
        return flyway;
    }
}
