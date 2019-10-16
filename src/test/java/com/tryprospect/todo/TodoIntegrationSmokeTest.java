package com.tryprospect.todo;

import static com.tryprospect.todo.utils.TodoCreator.validForCreationWithoutDueDate;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.omg.PortableInterceptor.INACTIVE;
import org.testcontainers.containers.PostgreSQLContainer;

import com.tryprospect.todo.api.Todo;
import com.tryprospect.todo.db.TodoDaoTestConfiguration;
import com.tryprospect.todo.resources.TodoResource;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import lombok.extern.slf4j.Slf4j;

// TODO: Figure out an alternative test similar to this that's more reliable.
@Slf4j
@ExtendWith(DropwizardExtensionsSupport.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TodoIntegrationSmokeTest {

    private Clock clock;
    private static Client client;
    private static Instant now;
    private static URI relativeUri;
    private static URI relativeUriWithId;
    private static WebTarget resourceTarget;
    private static Response response;
    private static Todo todo;
    private static Todo updatedTodo;
    private Todo validTodoForCreation;
    private static Optional<Todo> actualTodoOptionalCreated;
    private static List<UUID> idsOfTodosCreatedForTests;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                                        .withLocale(Locale.US )
                                                        .withZone(ZoneId.systemDefault());
    // TODO: figure out a way to perhaps use the main test-config.yml file. This statement references a copy in the tes resources folder.
    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-config.yml");
    public static final PostgreSQLContainer POSTGRES = TodoDaoTestConfiguration.createRunningInstanceOfPostgres();
    public static final DropwizardAppExtension<TodoConfiguration> DROPWIZARD = new DropwizardAppExtension<>(
                            TodoApplication.class, CONFIG_PATH,
                            ConfigOverride.config("database.url", POSTGRES.getJdbcUrl()),
                            ConfigOverride.config("database.user", POSTGRES.getUsername()),
                            ConfigOverride.config("database.password", POSTGRES.getPassword()),
                            ConfigOverride.config("database.driverClass", POSTGRES.getDriverClassName()));


    public TodoIntegrationSmokeTest() {
        validTodoForCreation = validForCreationWithoutDueDate();
        initClock();
    }

    private void initClock() {
        clock = Clock.tickMinutes(ZoneId.systemDefault());
    }

    @BeforeAll
    public static void setUp() {
        initClient();
        initBaseResourceUri();
        idsOfTodosCreatedForTests = new ArrayList<>();
    }

    private static void initClient() {
        client = ClientBuilder.newClient();
    }

    private static void initBaseResourceUri() {
        relativeUri = getUriFromResource().build();
    }

    private static UriBuilder getUriFromResource() {
        return UriBuilder.fromResource(TodoResource.class);
    }



    @BeforeEach
    public void createTodo() {

    }

    private WebTarget setResourceTarget(URI uri) {
        return client.target(String.format("http://localhost:%d%s", DROPWIZARD.getLocalPort(), uri.getPath()));
    }

    private boolean todoWasCreated(Response r) {
        Optional<Todo> actualTodoOptionalCreated = getTodoFromResponse(r);
        assertThat(actualTodoOptionalCreated.isPresent()).isTrue();
        assertThat(actualTodoOptionalCreated.get().getId()).isNotNull();
        saveIdFromCreatedTodo();
        return true;
    }

    private void saveIdFromCreatedTodo() {
        idsOfTodosCreatedForTests.add(actualTodoOptionalCreated.get().getId());
    }

    private void verifyActualEqualsExpected(Todo actualCreatedTodo) {
//        todo = actualTodoOptionalCreated.get();
        assertThat(actualCreatedTodo.getText()).isEqualTo(validTodoForCreation.getText());
        assertThat(actualCreatedTodo.getCompleted()).isFalse();
        assertThat(actualCreatedTodo.getDueDate().isPresent()).isFalse();
        assertThat(formattedDate(actualCreatedTodo.getCreatedAt())).isEqualTo(formattedDate(now));
        assertThat(formattedDate(actualCreatedTodo.getLastModifiedAt())).isEqualTo(formattedDate(now));
    }

    private Object getObjectFromResponse(Response r) {
        return r.readEntity(Object.class);
    }

    private static Optional<Todo> getTodoFromResponse(Response r) {
        Optional<Todo> readObject = Optional.empty();
        try {
            readObject = Optional.of(r.readEntity(Todo.class));
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            r.close();
        }
        return readObject;
    }

    private String formattedDate(Instant instantToFormat) {
        return FORMATTER.format(instantToFormat);
    }

    @Test
    public void testUpdate() {
        // given
//        now = clock.instant();
//        validTodoForCreation = validForCreationWithoutDueDate();
        resourceTarget = setResourceTarget(relativeUri);
        // when
        response = resourceTarget.request().post(Entity.json(validTodoForCreation));
        // then
        Optional<Todo> actualTodoOptionalCreated = getTodoFromResponse(response);
        assertThat(actualTodoOptionalCreated.isPresent()).isTrue();
        Todo actualTodoCreated = actualTodoOptionalCreated.get();
        assertThat(actualTodoCreated.getId()).isNotNull();
        idsOfTodosCreatedForTests.add(actualTodoCreated.getId());
        verifyActualEqualsExpected(actualTodoCreated);


        assertThat(idsOfTodosCreatedForTests).isNotEmpty();
        Instant dueInFiveDays = getDueDateOfFiveDaysFromNow();
        Todo validTodoForUpdate = createTodoForUpdate(actualTodoCreated, dueInFiveDays);
        resourceTarget = setResourceTarget(relativeUriWithId(validTodoForUpdate.getId().toString()));
        // when
        response = resourceTarget.request().get();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    private Todo createTodoForUpdate(Todo todoToUpdate, Instant dueDate) {
     return Todo.builder().id(todoToUpdate.getId())
             .text(todoToUpdate.getText()+" plus something else.")
             .isCompleted(todoToUpdate.getIsCompleted())
             .dueDate(dueDate).build();
    }

    private Instant getDueDateOfFiveDaysFromNow() {
        return clock.instant().plus(5, ChronoUnit.DAYS);
    }

//    @Test
//    @Order(1)
//    public void testCreateTodo() {
//        log.info("** EXECUTING METHOD 1 **");
//        // given
////        Todo todoToCreate = validForCreationWithoutDueDate();
//        // when
////        Client client = ClientBuilder.newClient();
////        WebTarget resourceTarget = client.target(String.format("http://localhost:%d%s", DROPWIZARD.getLocalPort(), relativeUri.getPath()));
//        Response response = resourceTarget.request().post(Entity.json(todoToCreate));
//        // then
//
//        assertThat(actualTodoOptionalCreated.isPresent()).isTrue();
//
////        todo = makePostRequestToCreateNewTodo(relativeUri, todoToCreate);
//
//        assertThat(todo.getId()).isNotNull();
//        assertThat(todo.getText()).isEqualTo(todoToCreate.getText());
//        assertThat(todo.getCompleted()).isFalse();
//        assertThat(todo.getDueDate().isPresent()).isFalse();
//        Instant now = clock.instant();
//        assertThat(formattedDate(todo.getCreatedAt())).isEqualTo(formattedDate(now));
//        assertThat(formattedDate(todo.getLastModifiedAt())).isEqualTo(formattedDate(now));
//    }
//
//    private String formattedDate(Instant instantToFormat) {
//        return FORMATTER.format(instantToFormat);
//    }
//
//    private Todo makePostRequestToCreateNewTodo(URI relativeUri, Todo todoToCreate) {
//        URI absoluteUri = buildAbsoluteUriWithRelative(relativeUri);
//        return makeRequest(absoluteUri).post(Entity.entity(Todo.class, MediaType.APPLICATION_JSON_TYPE), new GenericType<Todo>() {});
//    }
//
//    private static URI buildAbsoluteUriWithRelative(URI relativeUri) {
//        return combineHostPortAndRelativeUri(relativeUri);
//    }
//
//    private static URI combineHostPortAndRelativeUri(URI relativeUri) {
//        return UriBuilder.fromPath(String.format("http://localhost:%d%s", DROPWIZARD.getLocalPort(), relativeUri.getPath())).build();
//    }
//
//    private Invocation.Builder makeRequest(URI absoluteUri) {
//        return DROPWIZARD.client().target(absoluteUri).request()
//                .accept(MediaType.APPLICATION_JSON_TYPE);
//    }
//
//    @Test
//    @Order(2)
//    public void testGetTodo() {
//        log.info("** EXECUTING METHOD 2 **");
//
//        assertThat(todo).isNotNull();
//        // given
//        relativeUriWithId = buildRelativeUriWithId(todo.getId().toString());
//        // when
//        Optional<Todo> actualTodoOptional = makeGetRequestToReturnTodo(relativeUriWithId);
//        // then
//        assertThat(actualTodoOptional.isPresent()).isTrue();
//        assertThat(actualTodoOptional.get()).isEqualToComparingFieldByField(todo);
//    }
//
//    private Optional<Todo> makeGetRequestToReturnTodo(URI relativeUri) {
//        URI absoluteUri = buildAbsoluteUriWithRelative(relativeUri);
//        return makeRequest(absoluteUri).get(new GenericType<Optional<Todo>>() {});
//    }
//
//    private URI buildRelativeUriWithId(String id) {
//        return getUriFromResource().path("/{id}").build(id);
//    }
//
//    @Test
//    @Order(3)
//    public void testGetTodoS() throws JsonProcessingException {
//        log.info("** EXECUTING METHOD 3 **");
//        // given
//        List<Todo> expectedTodos = createListOfSingleTodo();
//        // when
//        List<Todo> actualTodos = makeGetRequestToReturnTodos(relativeUri);
//        String expectedTodosJson = convertListToJsonString(expectedTodos);
//        String actualTodosJson = convertListToJsonString(actualTodos);
//        // Then
//        assertThat(actualTodosJson).isEqualTo(expectedTodosJson);
//    }
//
//    private List<Todo> createListOfSingleTodo() {
//        assertThat(todo).isNotNull();
//        return Collections.singletonList(todo);
//    }
//
//    private List<Todo> makeGetRequestToReturnTodos(URI relativeUri) {
//        URI absoluteUri = buildAbsoluteUriWithRelative(relativeUri);
//        return makeRequest(absoluteUri).get(new GenericType<List<Todo>>() {});
//    }
//
//    private String convertListToJsonString(List<Todo> todos) throws JsonProcessingException {
//        return JsonHandler.OBJECT_MAPPER.writeValueAsString(todos);
//    }
//
//    @Test
//    @Order(4)
//    public void testUpdateTodo() {
//        log.info("** EXECUTING METHOD 4 **");
//        assertThat(todo).isNotNull();
//        // given
//        Todo todoToUpdate = validForUpdateWithDueDate();
//        // when
//        Response response = makeRequestToUpdateTodo(relativeUriWithId, todoToUpdate);
//        // then
//        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
//
//        prepareForNextTestByAssigningUpdatedTodo(todoToUpdate);
//    }
//
//    private Response makeRequestToUpdateTodo(URI relativeUri, Todo todoToUpdate) {
//        URI absoluteUri = buildAbsoluteUriWithRelative(relativeUri);
//        return makeRequest(absoluteUri).put(Entity.entity(todoToUpdate, MediaType.APPLICATION_JSON_TYPE));
//    }
//
//    private void prepareForNextTestByAssigningUpdatedTodo(Todo todo) {
//        updatedTodo = todo;
//    }
//
//    @Test
//    @Order(5)   // TODO: check that createdAt unchanged, and lastModifiedAt was updated.
//    public void checkTodoWasUpdated() {
//        log.info("** EXECUTING METHOD 5 **");
//        // given
//        assertThat(todo).isNotNull();
//        // when
//        Optional<Todo> actualTodoOptional = makeGetRequestToReturnTodo(relativeUriWithId);
//        // then
//        assertThat(actualTodoOptional.isPresent()).isTrue();
//        assertThat(actualTodoOptional.get()).isEqualToComparingFieldByField(updatedTodo);
//    }
//
//
//    // TODO: incorporate the following test scenarios:
//    /*
//    - marking a task as completed (or uncompleted)
//    - editing the task text
//    - changing the due date
//     */
//
//    @Test
//    @Order(6)
//    public void testDeleteTodo() {
//        log.info("** EXECUTING METHOD 6 **");
//        // given/when
//        Response response = makeRequestToDeleteTodo(relativeUriWithId);
//        // then
//        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
//    }
//
//    private Response makeRequestToDeleteTodo(URI relativeUri) {
//        URI absoluteUri = buildAbsoluteUriWithRelative(relativeUri);
//        return makeRequest(absoluteUri).delete();
//    }

    @AfterEach
    public void deleteCreatedTodos() {
        for (UUID id : idsOfTodosCreatedForTests) {
            resourceTarget = setResourceTarget(relativeUriWithId(id.toString()));
            response = resourceTarget.request().delete();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }
    }

    private URI relativeUriWithId(String id) {
        return getUriFromResource().path("/{id}").build(id);
    }
}
