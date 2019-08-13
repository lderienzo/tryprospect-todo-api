package com.tryprospect.todo;

import static com.tryprospect.todo.utils.TestTodoCreator.TODO_TEMPLATE;
import static com.tryprospect.todo.utils.TestTodoCreator.copyCreateNewTodoWithIsCompletedTrue;
import static com.tryprospect.todo.utils.TestTodoCreator.copyCreateTodoWithDueDateValue;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.assertj.core.api.Assertions;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.Preconditions;
import org.testcontainers.containers.PostgreSQLContainer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tryprospect.todo.api.Todo;
import com.tryprospect.todo.db.TodoDaoTestConfiguration;
import com.tryprospect.todo.resources.TodoResource;
import com.tryprospect.todo.utils.TestUtils;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

@ExtendWith(DropwizardExtensionsSupport.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TodoIntegrationSmokeTest {

    private static URI relativeUri;
    private static URI relativeUriWithId;
    private static Todo todo;
    private static Todo updatedTodo;
    private static final String NEW_TODO_IS_NULL_MSG = "New Todo is null.";
    private static final String UPDATED_TODO_IS_NULL_MSG = "Updated Todo is null.";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    // TODO: figure out a way to perhaps use the main test-config.yml file. This statement references a copy in the tes resources folder.
    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-config.yml");
    public static final PostgreSQLContainer POSTGRES = TodoDaoTestConfiguration.getRunningInstanceOfPostgres();
    public static final DropwizardAppExtension<TodoConfiguration> DROPWIZARD = new DropwizardAppExtension<>(
                            TodoApplication.class, CONFIG_PATH,
                            ConfigOverride.config("database.url", POSTGRES.getJdbcUrl()),
                            ConfigOverride.config("database.user", POSTGRES.getUsername()),
                            ConfigOverride.config("database.password", POSTGRES.getPassword()),
                            ConfigOverride.config("database.driverClass", POSTGRES.getDriverClassName()));


    @BeforeAll
    public static void setUp() {
        buildRelativeUriForApplication();
    }

    private static void buildRelativeUriForApplication() {
        relativeUri = getUriFromResource().build();
    }

    private static UriBuilder getUriFromResource() {
        return UriBuilder.fromResource(TodoResource.class);
    }

    @Test
    @Order(1)
    public void testCreateTodo() {
        // given
        Date expectedCreatedDateOfNow = Date.from(Clock.systemDefaultZone().instant());
        String expectedCreatedAt = DATE_FORMAT.format(expectedCreatedDateOfNow);
        String expectedLastModifiedAt = expectedCreatedAt;
        // when - make call to create new Todo.
        todo = makePostRequestToCreateNewTodo(relativeUri, TODO_TEMPLATE);
        // then
        assertThat(todo.getId()).isNotNull();
        assertThat(todo.getText()).isEqualTo(TODO_TEMPLATE.getText());
        assertThat(todo.getCompleted()).isFalse();
        assertThat(todo.getDueDate()).isNull();
        assertThat(DATE_FORMAT.format(todo.getCreatedAt())).isEqualTo(expectedCreatedAt);
        assertThat(DATE_FORMAT.format(todo.getLastModifiedAt())).isEqualTo(expectedLastModifiedAt);
    }

    private Todo makePostRequestToCreateNewTodo(URI relativeUri, Todo todoToCreate) {
        URI absoluteUri = buildAbsoluteUriWithRelative(relativeUri);
        return makeRequest(absoluteUri).post(Entity.entity(todoToCreate, MediaType.APPLICATION_JSON_TYPE), new GenericType<Todo>() {});
    }

    private static URI buildAbsoluteUriWithRelative(URI relativeUri) {
        return combineHostPortAndRelativeUri(relativeUri);
    }

    private static URI combineHostPortAndRelativeUri(URI relativeUri) {
        return UriBuilder.fromPath(String.format("http://localhost:%d%s", DROPWIZARD.getLocalPort(), relativeUri.getPath())).build();
    }

    private Invocation.Builder makeRequest(URI absoluteUri) {
        return DROPWIZARD.client().target(absoluteUri).request()
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    @Order(2)
    public void testGetTodo() {
        Preconditions.notNull(todo, NEW_TODO_IS_NULL_MSG);
        // given
        relativeUriWithId = buildRelativeUriWithId(todo.getId().toString());
        // when
        Optional<Todo> actualTodoOptional = makeGetRequestToReturnTodo(relativeUriWithId);
        // then
        assertThat(actualTodoOptional.isPresent()).isTrue();
        assertThat(actualTodoOptional.get()).isEqualToComparingFieldByField(todo);
    }

    private Optional<Todo> makeGetRequestToReturnTodo(URI relativeUri) {
        URI absoluteUri = buildAbsoluteUriWithRelative(relativeUri);
        return makeRequest(absoluteUri).get(new GenericType<Optional<Todo>>() {});
    }

    private URI buildRelativeUriWithId(String id) {
        return getUriFromResource().path("/{id}").build(id);
    }

    @Test
    @Order(3)
    public void testGetTodoS() throws JsonProcessingException {
        // given
        List<Todo> expectedTodos = createListOfSingleTodo();
        // when
        List<Todo> actualTodos = makeGetRequestToReturnTodos(relativeUri);
        String expectedTodosJson = convertListToJsonString(expectedTodos);
        String actualTodosJson = convertListToJsonString(actualTodos);
        // Then
        assertThat(actualTodosJson).isEqualTo(expectedTodosJson);
    }

    private List<Todo> createListOfSingleTodo() {
        Preconditions.notNull(todo, NEW_TODO_IS_NULL_MSG);
        return Collections.singletonList(todo);
    }

    private List<Todo> makeGetRequestToReturnTodos(URI relativeUri) {
        URI absoluteUri = buildAbsoluteUriWithRelative(relativeUri);
        return makeRequest(absoluteUri).get(new GenericType<List<Todo>>() {});
    }

    private String convertListToJsonString(List<Todo> todos) throws JsonProcessingException {
        return TestUtils.OBJECT_MAPPER.writeValueAsString(todos);
    }

    @Test
    @Order(4)
    public void testUpdateTodo() {
        Preconditions.notNull(todo, NEW_TODO_IS_NULL_MSG);
        // given
        Todo todoToUpdate = copyCreateTodoWithDueDateValue(todo);
        // when
        Response response = makeRequestToUpdateTodo(relativeUriWithId, todoToUpdate);
        // then
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);

        prepareForNextTestByAssigningUpdatedTodo(todoToUpdate);
    }

    private Response makeRequestToUpdateTodo(URI relativeUri, Todo todoToUpdate) {
        URI absoluteUri = buildAbsoluteUriWithRelative(relativeUri);
        return makeRequest(absoluteUri).put(Entity.entity(todoToUpdate, MediaType.APPLICATION_JSON_TYPE));
    }

    private void prepareForNextTestByAssigningUpdatedTodo(Todo todo) {
        updatedTodo = todo;
    }

    @Test
    @Order(5)
    public void checkTodoWasUpdated() {
        // given
        Preconditions.notNull(updatedTodo, UPDATED_TODO_IS_NULL_MSG);
        // when
        Optional<Todo> actualTodoOptional = makeGetRequestToReturnTodo(relativeUriWithId);
        // then
        assertThat(actualTodoOptional.isPresent()).isTrue();
        assertThat(actualTodoOptional.get()).isEqualToComparingFieldByField(updatedTodo);
    }


    // TODO: incorporate the following test scenarios:
    /*
    - marking a task as completed (or uncompleted)
    - editing the task text
    - changing the due date
     */

    @Test
    @Order(6)
    public void testUpdateTodo_whenIsCompletedTrueAndValueForDueDateThen422Error() {
        Preconditions.notNull(updatedTodo, NEW_TODO_IS_NULL_MSG);
        // given
        Todo todoToUpdate = copyCreateNewTodoWithIsCompletedTrue(updatedTodo);
        // when
        Response response = makeRequestToUpdateTodo(relativeUriWithId, todoToUpdate);
        // then
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    @Test
    @Order(7)
    public void testDeleteTodo() {
        // given/when
        Response response = makeRequestToDeleteTodo(relativeUriWithId);
        // then
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    private Response makeRequestToDeleteTodo(URI relativeUri) {
        URI absoluteUri = buildAbsoluteUriWithRelative(relativeUri);
        return makeRequest(absoluteUri).delete();
    }
}
