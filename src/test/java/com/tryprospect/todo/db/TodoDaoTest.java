package com.tryprospect.todo.db;

import static com.tryprospect.todo.db.TodoDaoTestConfiguration.*;
import static com.tryprospect.todo.utils.json.JsonHandler.TODO_TEMPLATE;
import static com.tryprospect.todo.utils.TestTodoCreator.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.tryprospect.todo.api.Todo;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;

public class TodoDaoTest {

    public static final PostgreSQLContainer POSTGRES = createRunningInstanceOfPostgres();
    private static final DataSourceFactory DATA_SOURCE_FACTORY = getDataSourceFactory(POSTGRES);
    private static final Flyway FLYWAY = getFlywayForDataSource(DATA_SOURCE_FACTORY);
    private static final LifecycleEnvironment LIFECYCLE_ENVIRONMENT = mock(LifecycleEnvironment.class);
    private static final HealthCheckRegistry HEALTH_CHECKS = mock(HealthCheckRegistry.class);
    private static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();
    private static final Environment ENVIRONMENT = mock(Environment.class);
    private static final String LAST_MODIFIED_AT = "lastModifiedAt";
    private static final String DUE_DATE = "dueDate";
    private static String[] fieldsToVerifyIndividually;
    private static Todo validTodoForCreation = copyCreateTodoForValidCreationExcludingDueDate();
    private static Todo newTodo;
    private static Todo expectedTodo;
    private static Todo updatedTodo;
    private List<Todo> expectedTodos;
    private List<Todo> actualTodos;
    private static TodoDAO todoDAO;

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
    public void testInsert_whenTodoValidForCreationThenNewTodoCreated() {
        // given/when
        newTodo = todoDAO.insert(validTodoForCreation);
        // then
        assertThat(newTodo).isNotNull();
        assertThat(newTodo.getCompleted()).isFalse();
        assertThat(newTodo.getText()).isEqualTo(validTodoForCreation.getText());
        assertThat(idIsValidUuid(newTodo.getId().toString())).isTrue();
        assertThat(truncateToDay(newTodo.getCreatedAt()).compareTo(now())).isEqualTo(0);
        assertThat(truncateToDay(newTodo.getLastModifiedAt()).compareTo(now())).isEqualTo(0);
        assertThat(newTodo.getDueDate().isPresent()).isFalse();
    }

    private Instant truncateToDay(Instant instant) {
        return instant.truncatedTo(ChronoUnit.DAYS);
    }

    private Instant now() {
        return truncateToDay(Instant.now());
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
    public void testInsert_whenTodoIsNullThenException() {
        Assertions.assertThrows(NullPointerException.class, () ->
                todoDAO.insert(null)
        );
    }





    @Test
    public void testFindAll_whenTodosPresentThenFound() {
        // given
        addThreeTodos();
        // when
        actualTodos = todoDAO.findAll();
        // then
        assertThatAllAddedTodosAreFound();
    }

    private void addThreeTodos() {
        expectedTodos = IntStream.range(1, 4)
                .mapToObj(i -> todoDAO.insert(copyCreateTodoWithModifiedText(validTodoForCreation, i + "")))
                .collect(toList());
    }

    private void assertThatAllAddedTodosAreFound() {
        IntStream.range(0, 3).forEach(i ->
                assertThat(actualTodos.get(i)).isEqualToComparingFieldByField(expectedTodos.get(i)));
    }

    @Test
    public void testFindAll_whenTodoAbsentThenNotFound() {
        // given
        expectedTodos = Collections.emptyList();
        // when
        actualTodos = todoDAO.findAll();
        // then
        assertThat(actualTodos).isEqualTo(expectedTodos);
    }





    @Test
    public void testFindById_whenValidIdThenCorrectTodoFound() {
        // given
        newTodo = todoDAO.insert(validTodoForCreation);
        // when
        Optional<Todo> foundTodo = todoDAO.findById(newTodo.getId());
        // then
        assertThat(foundTodo.isPresent()).isTrue();
        assertThat(foundTodo.get()).isEqualToComparingFieldByField(newTodo);
    }

    @Test
    public void testFindById_whenInvalidIdThenTodoNotFound() {
        // given/when
        Optional<Todo> todoFound = todoDAO.findById(TODO_TEMPLATE.getId());
        // then
        assertThat(todoFound.isPresent()).isFalse();
    }





    @Test
    public void testUpdate_whenTextChangedThenSaved() {
        // given
        newTodo = createNewTodo();
        expectedTodo = copyCreateTodoWithModifiedText(newTodo);
        // when
        todoDAO.update(expectedTodo);
        // then
        updatedTodo = fetchUpdatedTodo();
        verifyUpdatedTodo();
    }

    private Todo createNewTodo() {
        return todoDAO.insert(validTodoForCreation);
    }

    private Todo fetchUpdatedTodo() {
        return todoDAO.findById(expectedTodo.getId()).get();
    }

    private void verifyUpdatedTodo() {
        String [] noExtraFieldsToIgnore = new String[]{};
        verifyFields(noExtraFieldsToIgnore);
    }

    private void verifyFields(String[] fieldsToIgnore) {
        fieldsToIgnore = addLastModifiedAtToIgnoredFields(fieldsToIgnore);
        assertThatUpdatedTodoIsWhatsExpectedMinusIgnoredFields(fieldsToIgnore);
        verifyIgnoredFieldsIndividually(fieldsToIgnore);
    }

    private String[] addLastModifiedAtToIgnoredFields(String[] fieldsToIgnore) {
        String fieldsToIgnoreString = combineFieldsToIgnoreIntoSingleString(fieldsToIgnore);
        fieldsToIgnoreString = addLastModifiedAt(fieldsToIgnoreString);
        return splitBackIntoArray(fieldsToIgnoreString);
    }

    private String combineFieldsToIgnoreIntoSingleString(String[] fieldsToIgnore) {
        return Joiner.on(",").join(fieldsToIgnore);
    }

    private String addLastModifiedAt(String combinedFields) {
        return (Strings.isNullOrEmpty(combinedFields) ? "" : combinedFields + ",") + LAST_MODIFIED_AT;
    }

    private String[] splitBackIntoArray(String combinedFields) {
        return combinedFields.split(",");
    }

    private void assertThatUpdatedTodoIsWhatsExpectedMinusIgnoredFields(String[] fieldsToIgnore) {
        assertThat(updatedTodo).isEqualToIgnoringGivenFields(expectedTodo, fieldsToIgnore);
    }

    private void verifyIgnoredFieldsIndividually(String[] fieldsToIgnore) {
        verifyIgnoredFields(fieldsToIgnore);
    }

    private void verifyIgnoredFields(String[] ignoredFields) {
        verifyLastModifiedAt();
        if (dueDateIsPresentIn(ignoredFields)) {
            verifyDueDate();
        }
    }

    private void verifyLastModifiedAt() {
        verifyUpdatedTodoLastModifiedAtIsNotNull();
        verifyUpdatedTodoLastModifiedAtDateComesAfterThatOfExpected();
        verifyUpdatedTodoLastModifiedAtDateIsEqualToNow();
    }

    private void verifyUpdatedTodoLastModifiedAtIsNotNull() {
        assertThat(updatedTodo.getLastModifiedAt()).isNotNull();
    }

    private void verifyUpdatedTodoLastModifiedAtDateComesAfterThatOfExpected() {
        assertThat(updatedTodo.getLastModifiedAt().isAfter(expectedTodo.getLastModifiedAt())).isTrue();
    }

    private void verifyUpdatedTodoLastModifiedAtDateIsEqualToNow() {
        assertThat(truncateToDay(updatedTodo.getLastModifiedAt()).compareTo(now())).isEqualTo(0);
    }

    private boolean dueDateIsPresentIn(String[] fields) {
        return Arrays.stream(fields).filter(field -> field.equals(DUE_DATE)).count() == 1;
    }

    private void verifyDueDate() {
        assertThat(updatedTodo.getDueDate().isPresent()).isTrue();
        assertThat(truncateToDay(updatedTodo.getDueDate().get()))
                .isEqualTo(truncateToDay(expectedTodo.getDueDate().get()));
    }

    @Test
    public void testUpdate_whenDueDatePresentThenSaved() {
        // given
        newTodo = todoDAO.insert(validTodoForCreation);
        expectedTodo = copyCreateTodoAddingDueDate(newTodo);
        fieldsToVerifyIndividually = new String[]{DUE_DATE};
        // when
        todoDAO.update(expectedTodo);
        // then
        updatedTodo = fetchUpdatedTodo();
        verifyFields(fieldsToVerifyIndividually);
    }

    @Test
    public void testUpdate_whenIsCompletedChangedThenSaved() {
        // given
        newTodo = todoDAO.insert(validTodoForCreation);
        expectedTodo = copyCreateTodoChangingIsCompleted(newTodo);
        // when
        todoDAO.update(expectedTodo);
        // then
        updatedTodo = fetchUpdatedTodo();
        verifyUpdatedTodo();
    }





    @Test
    public void testDeleteById_whenValidIdThenDeleted() {
        // given
        Todo todoToDelete = todoDAO.insert(validTodoForCreation);
        // when
        todoDAO.deleteById(todoToDelete.getId());
        // then
        Optional<Todo> deletedTodo = todoDAO.findById(todoToDelete.getId());
        assertThat(deletedTodo.isPresent()).isFalse();
    }

    @Test
    public void testDeleteById_whenInvalidIdThenNothingDeleted() {
        // given/when
        todoDAO.deleteById(TODO_TEMPLATE.getId());
        // then
        Optional<Todo> deletedTodo = todoDAO.findById(TODO_TEMPLATE.getId());
        assertThat(deletedTodo.isPresent()).isFalse();
    }
}
