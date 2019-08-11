package com.tryprospect.todo.db;

import static com.tryprospect.todo.utils.TestUtils.lastModifiedNow;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.tryprospect.todo.api.Todo;
import com.tryprospect.todo.utils.TestUtils;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;

public class TodoDaoTest {

// TODO: See if we can get this working
//    public static DockerComposeRule docker = DockerComposeRule.builder()
//            .file("src/test/resources/test-compose.yml")
//            .build();

    public static final PostgreSQLContainer POSTGRES = TodoDaoTestConfiguration.getRunningInstanceOfPostgres();

    private static final DataSourceFactory DATA_SOURCE_FACTORY = TodoDaoTestConfiguration.getDataSourceFactory(POSTGRES);
    private static final Flyway FLYWAY = TodoDaoTestConfiguration.getFlywayDbMigrationObject(DATA_SOURCE_FACTORY);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final LifecycleEnvironment LIFECYCLE_ENVIRONMENT = mock(LifecycleEnvironment.class);
    private static final HealthCheckRegistry HEALTH_CHECKS = mock(HealthCheckRegistry.class);
    private static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();
    private static final Environment ENVIRONMENT = mock(Environment.class);
    private static final String TODO_TEXT = "test todo text";
    private static TodoDAO todoDAO;
    private List<Todo> expectedTodos;
    private List<Todo> actualTodos;

    @BeforeAll
    public static void setUp() {
        initializeTodoDAO();
    }

    private static void initializeTodoDAO() {
        setUpMocksRequiredToCreateJdbiFactory();
        Jdbi jdbi = getJdbiObjectFromFactory();
        todoDAO = getTodoDaoFromJdbiObject(jdbi);
    }

    private static void setUpMocksRequiredToCreateJdbiFactory() {
        when(ENVIRONMENT.metrics()).thenReturn(METRIC_REGISTRY);
        when(ENVIRONMENT.healthChecks()).thenReturn(HEALTH_CHECKS);
        when(ENVIRONMENT.lifecycle()).thenReturn(LIFECYCLE_ENVIRONMENT);
        when(ENVIRONMENT.getHealthCheckExecutorService()).thenReturn(Executors.newSingleThreadExecutor());
    }

    private static Jdbi getJdbiObjectFromFactory() {
        return new JdbiFactory().build(ENVIRONMENT, DATA_SOURCE_FACTORY, geSqlContainerName());
    }

    private static String geSqlContainerName() {
        return getPostgresContainerMemberField().getName();
    }

    private static Field getPostgresContainerMemberField() {
        return TodoDaoTest.class.getFields()[0];
    }

    private static TodoDAO getTodoDaoFromJdbiObject(Jdbi jdbi) {
        return jdbi.onDemand(TodoDAO.class);
    }

    @BeforeEach
    public void resetDb() {
        recreateSchemaToAvoidDataInterdependencies();
    }

    private void recreateSchemaToAvoidDataInterdependencies() {
        FLYWAY.clean();
        FLYWAY.migrate();
    }

    @Test
    public void containerHealthCheck() {
        assertThat(POSTGRES.isRunning()).isTrue();
    }

    @Test
    public void testInsert_successWhenNonNullStringPassed() {
        // given
        Date expectedCreatedDateOfNow = Date.from(Clock.systemDefaultZone().instant());
        String expectedCreatedAt = DATE_FORMAT.format(expectedCreatedDateOfNow);
        String expectedLastModifiedAtString = expectedCreatedAt;

        // when
        Todo actualTodo = todoDAO.insert(TODO_TEXT);
        String actualTodoCreatedAt = DATE_FORMAT.format(actualTodo.getCreatedAt());
        String actualTodoLastModifiedAt = DATE_FORMAT.format(actualTodo.getLastModifiedAt());

        // then
        assertThat(actualTodo.getCompleted()).isFalse();
        assertThat(actualTodo.getText()).isEqualTo(TODO_TEXT);
        assertThat(idIsValidUuid(actualTodo.getId().toString())).isTrue();
        assertThat(removeSeconds(actualTodo.getCreatedAt())).isEqualTo(expectedCreatedAt);
        assertThat(removeSeconds(actualTodo.getLastModifiedAt())).isEqualTo(expectedLastModifiedAt);
    }

    private String removeSeconds(Date dateToFormat) {
        return DATE_FORMAT.format(dateToFormat);
    }

    private Todo createTodo(String todoText) {
        return todoDAO.insert(todoText);
    }

    @Test
    public void testInsert_failureAsExceptionWhenNullStringPassed() {
        Assertions.assertThrows(UnableToExecuteStatementException.class, () ->
            todoDAO.insert(null)
        );
    }

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

    @Test
    public void testFindAll_allFound() {
        // given
        expectedTodos = addThreeTodos();

        // when
        actualTodos = todoDAO.findAll();

        // then
        assertThatAllAddedTodosWhereFound();
    }

    private List<Todo> addThreeTodos() {
        return IntStream.range(1, 4)
                .mapToObj(i -> todoDAO.insert("test todo " + i + " text"))
                .collect(toList());
    }

    private void assertThatAllAddedTodosWhereFound() {
        IntStream.range(0, 3).forEach(i ->
                assertThat(actualTodos.get(i).toString()).isEqualTo(expectedTodos.get(i).toString()));
    }

    @Test
    public void testFindAll_noneFound() {
        // given
        expectedTodos = Collections.emptyList();

        // when
        actualTodos = todoDAO.findAll();

        // then
        assertThat(actualTodos).isEqualTo(expectedTodos);
    }

    @Test
    public void testFindById_todoFound() {
        // given
        Todo todoToFind = todoDAO.insert("test todo text");

        // when
        Optional<Todo> todoFound = todoDAO.findById(todoToFind.getId());

        // then
        assertThat(todoFound.get().toString()).isEqualTo(todoToFind.toString());
    }

    @Test
    public void testFindById_todoNotFound() throws IOException {
        // given
        Todo todoToFind = TestUtils.createTestTodoFromJson();

        // when
        Optional<Todo> todoFound = todoDAO.findById(todoToFind.getId());

        // then
        assertThat(todoFound.isPresent()).isFalse();
    }


    @Test
    public void testDeleteById_existingTodo() {
        // given
        Todo todoToDelete = todoDAO.insert("test todo text");

        // when
        todoDAO.deleteById(todoToDelete.getId());

        // then
        Optional<Todo> deletedTodo = todoDAO.findById(todoToDelete.getId());
        assertThat(deletedTodo.isPresent()).isFalse();
    }


    @Test
    public void testDeleteById_nonExistingTodo() throws IOException {
        // given
        Todo todoToDelete = TestUtils.createTestTodoFromJson();

        // when
        todoDAO.deleteById(todoToDelete.getId());

        // then
        Optional<Todo> deletedTodo = todoDAO.findById(todoToDelete.getId());
        assertThat(deletedTodo.isPresent()).isFalse();
    }
}
