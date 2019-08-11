package com.tryprospect.todo.resources;

import com.tryprospect.todo.api.Todo;
import com.tryprospect.todo.db.TodoDAO;
import com.tryprospect.todo.container.StatusFilterFeature;
import com.tryprospect.todo.utils.TestTodoCreater;
import com.tryprospect.todo.utils.TestUtils;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import org.eclipse.jetty.http.HttpStatus;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.tryprospect.todo.validation.Messages.NULL_LIST_OF_TODOS_RETURNED_ERROR;
import static com.tryprospect.todo.validation.Messages.NULL_TODO_RETURNED_ERROR;
import static com.tryprospect.todo.validation.Messages.TODO_VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


// TODO: look into a better way of organizing and cleaning up tests.
@ExtendWith(DropwizardExtensionsSupport.class)
public class TodoResourceTest {

    private ArgumentCaptor<String> todoInsertCaptor = ArgumentCaptor.forClass(String.class);
    private static Todo expectedTodo;
    private static String newTodoText;
    private static URI baseTodoUri;
    private static URI uriWithId;
    private static final String INVALID_ID = "some_invalid_value_for_id";
    private static final TodoDAO MOCK_TODO_DAO = mock(TodoDAO.class);
    public static final ResourceExtension TODO_RESOURCE =
                         ResourceExtension.builder()
                            .addResource(new TodoResource(MOCK_TODO_DAO))
                            .addProvider(StatusFilterFeature.class).build();
    private static final Logger LOG = LoggerFactory.getLogger(TodoResourceTest.class);

    @BeforeEach
    public void setUp() throws IOException {
        initTodo();
    }

    private static void initTodo() throws IOException {
        expectedTodo = TestUtils.createTestTodoFromJson();
        newTodoText = expectedTodo.getText();
        baseTodoUri = getBaseUriFromResource().build();
        uriWithId = buildRequestUriWithIdInPath(expectedTodo.getId().toString());
    }

    private static UriBuilder getBaseUriFromResource() {
        return UriBuilder.fromResource(TodoResource.class);
    }

    private static URI buildRequestUriWithIdInPath(String id) {
        return getBaseUriFromResource().path("/{id}").build(id);
    }



    @AfterEach
    public void tearDown() {
        reset(MOCK_TODO_DAO);
    }

    @Test
    public void testCreateTodo_whenPassedTextThenNewTodoCreated() {
        // given
        mockInsertMethodCall();
        // when
        Response response = makeRequestToCreateNewTodo(newTodoText);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        verifyInsertMethodWasCalledWithExpectedValue();
        verifyExpectedValueWasReturned(response);
    }

    private void mockInsertMethodCall() {
        when(MOCK_TODO_DAO.insert(anyString())).thenReturn(expectedTodo);
    }

    private Response makeRequestToCreateNewTodo(String param) {
        return invokeForUri(baseTodoUri.getPath()).post(Entity.entity(param, MediaType.APPLICATION_JSON_TYPE));
    }

    private static final Invocation.Builder invokeForUri(String uri) {
        return getTargetForUri(uri).request().accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private static final WebTarget getTargetForUri(String uri) {
        return TODO_RESOURCE.target(uri);
    }

    private void verifyInsertMethodWasCalledWithExpectedValue() {
        verify(MOCK_TODO_DAO).insert(todoInsertCaptor.capture());
        assertThat(todoInsertCaptor.getValue()).isEqualTo(expectedTodo.getText());
    }

    private void verifyExpectedValueWasReturned(Response response) {
        assertThat(response.hasEntity()).isTrue();
        Todo newTodo = getNewTodoFromResponse(response);
        assertThat(newTodo).isEqualToComparingFieldByField(expectedTodo);
    }

    private Todo getNewTodoFromResponse(Response response) {
        Todo newTodo;
        try {
            newTodo = response.readEntity(Todo.class);
        } finally {
            response.close();
        }
        return newTodo;
    }

    @Test
    public void testCreateTodo_whenTodoTextIsBlankThen422returned() {
        // given/when
        Response response = makeRequestToCreateNewTodo("");
        // then
        assertThat(response.getStatusInfo().getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
        assertThat(responseMessage(response)).contains(TODO_VALIDATION_ERROR);
    }

    private String responseMessage(Response response) {
        return getEntityFromResponse(response).get();
    }

    private static Optional<String> getEntityFromResponse(Response response) {
        Optional<String> entityOptional = Optional.empty();
        try {
            entityOptional = Optional.of(response.readEntity(String.class));
        } catch (ProcessingException e) {
            LOG.error(e, () -> "Could not read entity from response.");
        }
        return entityOptional;
    }

    @Test
    public void testCreateTodo_whenNullTodoReturnedByDaoMethodThen500returned() {
        // given
        when(MOCK_TODO_DAO.insert(anyString())).thenReturn(null);
        // when
        Response response = makeRequestToCreateNewTodo(newTodoText);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        assertThat(responseMessage(response)).contains(NULL_TODO_RETURNED_ERROR);
    }

    @Test
    public void testCreateTodo_whenDaoMethodThrowsExceptionThen500returned() {
        // given
        when(MOCK_TODO_DAO.insert((anyString())))
                .thenThrow(UnableToExecuteStatementException.class);
        // when
        Response response = makeRequestToCreateNewTodo(newTodoText);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void testUpdateTodo_whenAllValuesExceptDueDateArePresentAndValidThen204Returned() {

        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), expectedTodo);
        // then
        verifyDaoUpdateMethodWasCalledWithCorrectValue(expectedTodo);
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    private Response makeRequestToUpdateTodo(String uri, Todo todoToUpdate) {
        return invokeForUri(uri).put(Entity.entity(todoToUpdate, MediaType.APPLICATION_JSON_TYPE));
    }

    private vo(Todo expectedTodo) {
        verify(MOCK_TODO_DAO, times(1)).update(expectedTodo);
    }

    @Test
    public void testUpdateTodo_whenAllValuesValidIncludingFutureValueForDueDateThen204Returned() {

        // given
        Todo validTodoWithFutureValueForDueDate = TestTodoCreater.copyCreateNewTodoWithFutureValueForDueDate(expectedTodo);
        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), validTodoWithFutureValueForDueDate);
        // then
        verifyDaoUpdateMethodWasCalledWithCorrectValue(validTodoWithFutureValueForDueDate);
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void testUpdateTodo_whenNullValueForIdThen422Returned() {

        // given
        Todo todoWithNullId = TestTodoCreater.copyCreateNewTodoWithNullId(expectedTodo);
        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), todoWithNullId);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    private void assertThatResponseStatusIsUnprocessableEntity422(Response response) {
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    @Test
    public void testUpdateTodo_whenNullValueForTextThen422Returned() {

        // given
        Todo todoWithNullText = TestTodoCreater.copyCreateNewTodoWithNullText(expectedTodo);
        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), todoWithNullText);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenBlankValueForTextThen422Returned() {

        // given
        Todo todoWithBlankText = TestTodoCreater.copyCreateNewTodoWithBlankText(expectedTodo);
        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), todoWithBlankText);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenNullValueForIsCompletedThen422Returned() {

        // given
        Todo todoWithNullIsCompleted = TestTodoCreater.copyCreateNewTodoWithNullForIsCompleted(expectedTodo);
        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), todoWithNullIsCompleted);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenNullValueForCreatedAtThen422Returned() {

        // given
        Todo todoWithNullCreatedAt = TestTodoCreater.copyCreateNewTodoWithNullForCreatedAt(expectedTodo);
        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), todoWithNullCreatedAt);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenFutureValueForCreatedAtThen422Returned() {

        // given
        Todo todoWithFutureCreatedAt = TestTodoCreater.copyCreateNewTodoWithFutureValueForCreatedAt(expectedTodo);
        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), todoWithFutureCreatedAt);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenNullValueForLastModifiedAtThen422Returned() {

        // given
        Todo todoWithNullLastModifiedAt = TestTodoCreater.copyCreateNewTodoWithNullForLastModifiedAt(expectedTodo);
        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), todoWithNullLastModifiedAt);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenFutureValueForLastModifiedAtThen422Returned() {

        // given
        Todo todoWithFutureLastModifiedAt = TestTodoCreater.copyCreateNewTodoWithFutureValueForLastModifiedAt(expectedTodo);
        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), todoWithFutureLastModifiedAt);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenPresentValueForDueDateThen422Returned() {

        // given
        Todo todoWithPresentValueForDueDate = TestTodoCreater.copyCreateNewTodoWithPresentValueForDueDate(expectedTodo);
        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), todoWithPresentValueForDueDate);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenPastValueForDueDateThen422Returned() {

        // given
        Todo todoWithPastValueForDueDate = TestTodoCreater.copyCreateNewTodoWithPastValueForDueDate(expectedTodo);
        // when
        Response response = makeRequestToUpdateTodo(baseTodoUri.getPath(), todoWithPastValueForDueDate);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testGetTodo_whenValidIdPassedThenCorrectTodoReturned() {
        // given
        when(MOCK_TODO_DAO.findById(expectedTodo.getId())).thenReturn(Optional.of(expectedTodo));
        // when
        Optional<Todo> returnedTodoOptional = makeRequestToReturnOptional(uriWithId.getPath());
        // then
        verifyDaoFindByIdMethodWasCalledWithCorrectValue();
        assertThat(returnedTodoOptional.isPresent()).isTrue();
        assertThat(returnedTodoOptional.get()).isEqualToComparingFieldByField(expectedTodo);
    }

    private Optional<Todo> makeRequestToReturnOptional(String uriToUse) {
        return invokeForUri(uriToUse).get(new GenericType<Optional<Todo>>() {});
    }

    private void verifyDaoFindByIdMethodWasCalledWithCorrectValue() {
        verify(MOCK_TODO_DAO).findById(expectedTodo.getId());
    }

    @Test
    public void testGetTodo_whenInvalidIdPassedThen404returned() {
        // given
        uriWithId = buildRequestUriWithIdInPath(INVALID_ID);
        // when
        Response response = makeRequestToReturnResponse(uriWithId.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void testGetTodo_whenDaoMethodThrowsExceptionThen500returned() {
        // given
        when(MOCK_TODO_DAO.findById(expectedTodo.getId()))
                .thenThrow(UnableToExecuteStatementException.class);
        // when
        Response response = makeRequestToReturnResponse(uriWithId.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void testGetTodo_whenNullValueReturnedByDaoMethodThen204returned() {
        // given
        when(MOCK_TODO_DAO.findById(expectedTodo.getId())).thenReturn(null);
        // when
        Response response = makeRequestToReturnResponse(uriWithId.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void testGetTodoS_whenCalledNormallyThenExpectedListReturned() {
        // given
        List<Todo> expectedTodos = createListOfSingleTodo();
        mockFindAllMethodCallToReturnExpectedListOfTodos(expectedTodos);
        // when
        List<Todo> returnedTodos = makeRequestToReturnList(baseTodoUri.getPath());
        // then
        assertThat(returnedTodos).containsAll(expectedTodos);
    }

    private List<Todo> createListOfSingleTodo() {
        return Collections.singletonList(expectedTodo);
    }

    private void mockFindAllMethodCallToReturnExpectedListOfTodos(List<Todo> todos) {
        when(MOCK_TODO_DAO.findAll()).thenReturn(todos);
    }

    private List<Todo> makeRequestToReturnList(String uri) {
        return invokeForUri(uri).get(new GenericType<List<Todo>>() {});
    }

    @Test
    public void testGetTodoS_whenNullListReturnedFromDaoMethodThen422returned() {
        //given/when
        mockFindAllMethodCallToReturnExpectedListOfTodos(null);
        // when
        Response response = makeRequestToReturnResponse(baseTodoUri.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
        assertThat(responseMessage(response)).contains(NULL_LIST_OF_TODOS_RETURNED_ERROR);
    }

    private Response makeRequestToReturnResponse(String uriToUse) {
        return invokeForUri(uriToUse).get();
    }

    @Test
    public void testGetTodoS_whenDaoMethodThrowsExceptionThen500returned() {
        // given
        when(MOCK_TODO_DAO.findAll())
                .thenThrow(UnableToExecuteStatementException.class);
        // when
        Response response = makeRequestToReturnResponse(baseTodoUri.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void testDeleteTodo_whenValidIdPassedThenCorrectTodoDeleted() {
        // given
        mockTodoDaoDeleteByIdMethodCall(expectedTodo.getId());
        // when
        int returnStatusCode = makeRequestToDeleteTodoAndReturnStatus(uriWithId.getPath());
        // then
        verifyDaoDeleteByIdMethodWasCalledWithCorrectValue();
        assertThat(returnStatusCode).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    private void mockTodoDaoDeleteByIdMethodCall(UUID id) {
        when(MOCK_TODO_DAO.findById(id)).thenReturn(Optional.of(expectedTodo));
    }

    private int makeRequestToDeleteTodoAndReturnStatus(String path) {
        return makeDeleteTodoRequestWithPathAndReturnResponse(path).getStatusInfo().getStatusCode();
    }

    private Response makeDeleteTodoRequestWithPathAndReturnResponse(String path) {
        return invokeForUri(path).delete();
    }

    private void verifyDaoDeleteByIdMethodWasCalledWithCorrectValue() {
        verify(MOCK_TODO_DAO).deleteById(expectedTodo.getId());
    }

    @Test
    public void testDeleteTodo_whenEmptyIdPassedThen405returned() {
        // given
        uriWithId = buildRequestUriWithIdInPath("");
        // when
        int returnStatusCode = makeRequestToDeleteTodoAndReturnStatus(uriWithId.getPath());
        // then
        assertThat(returnStatusCode).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED_405);
    }

    @Test
    public void testDeleteTodo_whenInvalidIdPassedThen404returned() {
        // given
        uriWithId = buildRequestUriWithIdInPath(INVALID_ID);
        // when
        Response response = makeDeleteTodoRequestWithPathAndReturnResponse(uriWithId.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void testDeleteTodo_whenDaoMethodThrowsExceptionThen500returned() {
        // given
        doThrow(UnableToExecuteStatementException.class).when(MOCK_TODO_DAO).deleteById(expectedTodo.getId());
        // when
        Response response = makeDeleteTodoRequestWithPathAndReturnResponse(uriWithId.getPath());
        // then
        verifyDaoDeleteByIdMethodWasCalledWithCorrectValue();
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }
}

