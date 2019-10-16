package com.tryprospect;

import static com.tryprospect.todo.utils.TodoCreator.validForCreationWithoutDueDate;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;

import com.tryprospect.todo.TodoApplication;
import com.tryprospect.todo.TodoConfiguration;
import com.tryprospect.todo.api.Todo;
import com.tryprospect.todo.db.TodoDaoTestConfiguration;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

@ExtendWith(DropwizardExtensionsSupport.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TodoIntegrationTest {
    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-config.yml");
    public static final PostgreSQLContainer POSTGRES = TodoDaoTestConfiguration.createRunningInstanceOfPostgres();
    public static final DropwizardAppExtension<TodoConfiguration> APP = new DropwizardAppExtension<>(
            TodoApplication.class, CONFIG_PATH,
            ConfigOverride.config("database.url", POSTGRES.getJdbcUrl()),
            ConfigOverride.config("database.user", POSTGRES.getUsername()),
            ConfigOverride.config("database.password", POSTGRES.getPassword()),
            ConfigOverride.config("database.driverClass", POSTGRES.getDriverClassName()));
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.US )
            .withZone(ZoneId.systemDefault());

    @Test
    @Order(1)
    public void testCreateTodo() {
        // given
        Todo todoToCreate = validForCreationWithoutDueDate();
        // when
         Response response = APP.client().target("http://localhost:" + APP.getLocalPort() + "/todos")
                .request()
                .post(Entity.entity(todoToCreate, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);

        Todo actualCreatedTodo = response.readEntity(Todo.class);
        // then
        assertThat(actualCreatedTodo).isNotNull();
        assertThat(actualCreatedTodo.getId()).isNotNull();
        assertThat(idIsValidUuid(actualCreatedTodo.getId().toString())).isTrue();
        Instant now = Clock.tickMinutes(ZoneId.systemDefault()).instant();
        assertThat(formattedDate(actualCreatedTodo.getCreatedAt())).isEqualTo(formattedDate(now));
        assertThat(formattedDate(actualCreatedTodo.getLastModifiedAt())).isEqualTo(formattedDate(now));
        assertThat(actualCreatedTodo).isEqualToComparingOnlyGivenFields(todoToCreate, "text","isCompleted");
    }

// TODO: REPEATED CODE !!
    private boolean idIsValidUuid(String newTodoId) {
        boolean isValid;
        try {
            isValid = stringRepresentsValidUuid(newTodoId);
        } catch (IllegalArgumentException e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            isValid = false;
        }
        return isValid;
    }

    private boolean stringRepresentsValidUuid(String newTodoId) {
        return ableToCreateUuid(newTodoId);
    }

    private boolean ableToCreateUuid(String newTodoId) {
        return UUID.fromString(newTodoId) != null;
    }
// TODO: REPEATED CODE !!

    private String formattedDate(Instant instantToFormat) {
        return FORMATTER.format(instantToFormat);
    }


}
