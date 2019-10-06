package com.tryprospect.todo.db;

import static com.tryprospect.todo.utils.yaml.ConfigYamlReader.CONFIG_YAML_FILE;
import static com.tryprospect.todo.utils.yaml.DockerComposeYamlReader.DOCKER_COMPOSE_FILE;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import com.tryprospect.todo.db.jackson.Config;
import com.tryprospect.todo.db.jackson.DockerCompose;
import com.tryprospect.todo.utils.yaml.ConfigYamlReader;
import com.tryprospect.todo.utils.yaml.DockerComposeYamlReader;

import io.dropwizard.db.DataSourceFactory;

public final class TodoDaoTestConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(TodoDaoTestConfiguration.class);

    public static final PostgreSQLContainer createRunningInstanceOfPostgres() {
        PostgreSQLContainer postgreSQLContainer = createUsingInfoFromYamlFiles();
        postgreSQLContainer.start();
        return postgreSQLContainer;
    }

    private static PostgreSQLContainer createUsingInfoFromYamlFiles() {
        Config config = getDbConnectionSettingsFromConfigYamlFile();
        String dockerImageName = getDbImageNameFromDockerComposeYamlFile();
        return createPostgresContainer(config, dockerImageName);
    }

    private static Config getDbConnectionSettingsFromConfigYamlFile() {
        return new ConfigYamlReader().readObjectFromFile(CONFIG_YAML_FILE, Config.class);
    }

    private static String getDbImageNameFromDockerComposeYamlFile() {
        return  new DockerComposeYamlReader()
                .readObjectFromFile(DOCKER_COMPOSE_FILE, DockerCompose.class)
                .getServices().getPostgres().getImage();
    }

    private static PostgreSQLContainer createPostgresContainer(Config config, String dockerImageName) {
        return new PostgreSQLContainer(dockerImageName).withUsername(config.getDatabase().getUser())
                .withPassword(config.getDatabase().getPassword()).withDatabaseName(config.getDatabase().getDbName());
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

    public static final Flyway getFlywayForDataSource(final DataSourceFactory dsf) {
        return setFlywayDataSource(new Flyway(), dsf);
    }

    private static Flyway setFlywayDataSource(Flyway flyway, DataSourceFactory dsf) {
        flyway.setDataSource(dsf.getUrl(), dsf.getUser(), dsf.getPassword());
        return flyway;
    }
}
