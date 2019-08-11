package com.tryprospect.todo.db;

import static com.tryprospect.todo.utils.TestTodoCreator.*;
import static com.tryprospect.todo.utils.TestUtils.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.*;
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
    private static final String TODO_TEXT = "Test todo text.";
    private static final String MODIFIED_TODO_TEXT = TODO_TEXT + " Plus something else.";
    private static TodoDAO todoDAO;
    private List<Todo> expectedTodos;
    private List<Todo> actualTodos;

    @BeforeAll
    public static void setUp() {
        containerHealthCheck();
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

    private static TodoDAO getTodoDaoFromJdbiObject(Jdbi jdbi) {
        return jdbi.onDemand(TodoDAO.class);
    }

    private static void containerHealthCheck() {
        assertThat(POSTGRES.isRunning()).isTrue();
    }

    private static String geSqlContainerName() {
        return getPostgresContainerMemberField().getName();
    }

    private static Field getPostgresContainerMemberField() {
        return TodoDaoTest.class.getFields()[0];
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
    public void testInsert_whenNonEmptyTextThenNewTodoCreated() {
        // given
        Date expectedLastModifiedDate = getPresentDate();
        String expectedLastModifiedAt = removeSeconds(expectedLastModifiedDate);
        String expectedCreatedAt = expectedLastModifiedAt;
        // when
        Todo actualTodo = createTodo(TODO_TEXT);
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
    public void testInsert_whenTextIsNullThenException() {
        Assertions.assertThrows(UnableToExecuteStatementException.class, () ->
            createTodo(null)
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
    public void testFindAll_whenTodosPresentThenAllFound() {
        // given
        expectedTodos = addThreeTodos();
        // when
        actualTodos = todoDAO.findAll();
        // then
        assertThatAllAddedTodosWhereFound();
    }

    private List<Todo> addThreeTodos() {
        return IntStream.range(1, 4)
                .mapToObj(i -> createTodo("test todo " + i + " text"))
                .collect(toList());
    }

    private void assertThatAllAddedTodosWhereFound() {
        IntStream.range(0, 3).forEach(i ->
                assertThat(actualTodos.get(i).toString()).isEqualTo(expectedTodos.get(i).toString()));
    }

    @Test
    public void testFindAll_whenTodosNotPresentThenNoneFound() {
        // given
        expectedTodos = Collections.emptyList();
        // when
        actualTodos = todoDAO.findAll();
        // then
        assertThat(actualTodos).isEqualTo(expectedTodos);
    }

    @Test
    public void testFindById_whenValidIdPassedThenFound() {
        // given
        Todo todoToFind = createTodo(TODO_TEXT);
        // when
        Optional<Todo> todoFound = todoDAO.findById(todoToFind.getId());
        // then
        assertThat(todoFound.get().toString()).isEqualTo(todoToFind.toString());
    }

    @Test
    public void testFindById_whenInvalidIdPassedThenNothingFound() throws IOException {
        // given
        Todo todoToFind = TestUtils.createTestTodoFromJson();
        // when
        Optional<Todo> todoFound = todoDAO.findById(todoToFind.getId());
        // then
        assertThat(todoFound.isPresent()).isFalse();
    }

    @Test
    public void testUpdate_whenTextChangedThenValueProperlySaved() {
        // given
        Todo newTodo = createTodo(TODO_TEXT);
        Todo expectedTodo = copyCreateTodoChangingTextAndLastModified(newTodo);
        // when
        todoDAO.update(expectedTodo);
        // then
        checkUpdateComparingLastModifedSeparately(expectedTodo);
    }

    private void checkUpdateComparingLastModifedSeparately(Todo expectedTodo) {
        Todo updatedTodo = todoDAO.findById(expectedTodo.getId()).get();
        assertThat(updatedTodo).isEqualToIgnoringGivenFields(expectedTodo, "lastModifiedAt");
        assertThat(removeSeconds(updatedTodo.getLastModifiedAt())).isEqualTo(removeSeconds(expectedTodo.getCreatedAt()));
    }

    @Test
    public void testUpdate_whenDueDateChangedThenValueProperlySaved() {
        // given
        Todo newTodo = createTodo(TODO_TEXT);
        Todo expectedTodo = copyCreateTodoChangingDueDateAndLastModified(newTodo);
        // when
        todoDAO.update(expectedTodo);
        // then
        checkUpdateComparingLastModifedAndDueDateSeparately(expectedTodo);
    }

    private void checkUpdateComparingLastModifedAndDueDateSeparately(Todo expectedTodo) {
        Todo updatedTodo = todoDAO.findById(expectedTodo.getId()).get();
        assertThat(updatedTodo).isEqualToIgnoringGivenFields(expectedTodo, "lastModifiedAt", "dueDate");
        assertThat(removeSeconds(updatedTodo.getLastModifiedAt())).isEqualTo(removeSeconds(expectedTodo.getCreatedAt()));
        assertThat(removeSeconds(updatedTodo.getDueDate())).isEqualTo(removeSeconds(expectedTodo.getDueDate()));
    }

    @Test
    public void testUpdate_whenIsCompletedChangedThenValueProperlySaved() {
        // given
        Todo newTodo = createTodo(TODO_TEXT);
        Todo expectedTodo = copyCreateTodoChangingIsCompletedAndLastModified(newTodo);
        // when
        todoDAO.update(expectedTodo);
        // then
        checkUpdateComparingLastModifedSeparately(expectedTodo);
    }

    @Test
    public void testUpdate_whenIsCompletedAndTextChangedThenValueProperlySaved() {
        // given
        Todo newTodo = createTodo(TODO_TEXT);
        Todo expectedTodo = copyCreateTodoChangingIsCompletedTextAndLastModified(newTodo);
        // when
        todoDAO.update(expectedTodo);
        // then
        checkUpdateComparingLastModifedSeparately(expectedTodo);
    }

    @Test
    public void testUpdate_whenDueDateAndTextChangedThenValueProperlySaved() {
        // given
        Todo newTodo = createTodo(TODO_TEXT);
        Todo expectedTodo = copyCreateTodoChangingDueDateTextAndLastModified(newTodo);
        // when
        todoDAO.update(expectedTodo);
        // then
        checkUpdateComparingLastModifedAndDueDateSeparately(expectedTodo);
    }

    @Test
    public void testUpdate_whenDueDateAndIsCompletedChangedThenValueProperlySaved() {
        // given
        Todo newTodo = createTodo(TODO_TEXT);
        Todo expectedTodo = copyCreateTodoChangingDueDateIsCompletedAndLastModified(newTodo);
        // when
        todoDAO.update(expectedTodo);
        // then
        checkUpdateComparingLastModifedAndDueDateSeparately(expectedTodo);
    }

    @Test
    public void testUpdate_whenDueDateIsCompletedAndTextChangedThenValueProperlySaved() {
        // given
        Todo newTodo = createTodo(TODO_TEXT);
        Todo expectedTodo = copyCreateTodoChangingDueDateIsCompletedTextAndLastModified(newTodo);
        // when
        todoDAO.update(expectedTodo);
        // then
        checkUpdateComparingLastModifedAndDueDateSeparately(expectedTodo);
    }

    @Test
    public void testUpdate_whenOnlyLastModifiedChangedThenValueProperlySaved() {
        // given
        Todo newTodo = createTodo(TODO_TEXT);
        Todo expectedTodo = newTodo;
        // when
        todoDAO.update(expectedTodo);
        // then
        checkUpdateComparingLastModifedSeparately(expectedTodo);
    }

    @Test
    public void testDeleteById_whenValidIdThenDeleted() {
        // given
        Todo todoToDelete = createTodo(TODO_TEXT);
        // when
        todoDAO.deleteById(todoToDelete.getId());
        // then
        Optional<Todo> deletedTodo = todoDAO.findById(todoToDelete.getId());
        assertThat(deletedTodo.isPresent()).isFalse();
    }

    @Test
    public void testDeleteById_whenInvalidIdThenNothingDeleted() throws IOException {
        // given
        Todo todoToDelete = TestUtils.createTestTodoFromJson();
        // when
        todoDAO.deleteById(todoToDelete.getId());
        // then
        Optional<Todo> deletedTodo = todoDAO.findById(todoToDelete.getId());
        assertThat(deletedTodo.isPresent()).isFalse();
    }
}
