package com.tryprospect.todo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.tryprospect.todo.db.TodoDAO;
import com.tryprospect.todo.container.StatusFilterFeature;
import com.tryprospect.todo.exceptionmappers.ConstraintViolationExceptionMapper;
import com.tryprospect.todo.exceptionmappers.JdbiExceptionMapper;
import com.tryprospect.todo.exceptionmappers.TodoValidationExceptionMapper;
import com.tryprospect.todo.lifecycle.ManagedFlywayMigration;
import com.tryprospect.todo.resources.TodoResource;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;

public class TodoApplication extends Application<TodoConfiguration> {

    public static void main(final String[] args) throws Exception {
        new TodoApplication().run(args);
    }

    @Override
    public String getName() {
        return "Todo API";
    }

    @Override
    public void initialize(final Bootstrap<TodoConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(final TodoConfiguration configuration, final Environment environment) {
        // DB
        DataSourceFactory dataSourceFactory = configuration.getDataSourceFactory();

        // Flyway
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
        // Automatically run Flyway migrations on startup
        environment.lifecycle().manage(new ManagedFlywayMigration(flyway));

        // JDBI
        JdbiFactory factory = new JdbiFactory();
        Jdbi jdbi = factory.build(environment, dataSourceFactory, "postgresql");

        // DAOs
        TodoDAO todoDAO = jdbi.onDemand(TodoDAO.class);

        // Resources
        environment.jersey().register(new TodoResource(todoDAO));

        // Filters
        environment.jersey().register(StatusFilterFeature.class);

        // Exception mappers
        environment.jersey().register(ConstraintViolationExceptionMapper.class);
        environment.jersey().register(TodoValidationExceptionMapper.class);
        environment.jersey().register(JdbiExceptionMapper.class);

        // Misc TODO: what is this? and what is it for?
        environment.getObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

}
