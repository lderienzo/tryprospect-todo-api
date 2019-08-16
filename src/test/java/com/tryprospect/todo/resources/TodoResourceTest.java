package com.tryprospect.todo.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryprospect.todo.api.Todo;
import com.tryprospect.todo.db.TodoDAO;
import com.tryprospect.todo.container.StatusFilterFeature;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import org.eclipse.jetty.http.HttpStatus;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.logging.*;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

import java.net.URI;
import java.util.*;

import static com.tryprospect.todo.utils.JSONTestUtils.TODO_TEMPLATE;
import static com.tryprospect.todo.utils.TestTodoCreator.*;
import static com.tryprospect.todo.validation.ValidationMessages.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// TODO: GO OVER TESTS AND REFACTOR/CLEAN-UP TO MAKE SURE THEY'RE RELEVANT.
// TODO: look into a better way of organizing and cleaning up tests.
@ExtendWith(DropwizardExtensionsSupport.class)
public class TodoResourceTest {

    private ArgumentCaptor<Todo> todoInsertCaptor = ArgumentCaptor.forClass(Todo.class);
    private static Todo expectedTodo;
    private static URI baseTodoUri;
    private static URI uriWithId;
    private static Response response;
    private static Todo validTodoForCreate;
    private static final String INVALID_ID = "some_invalid_value_for_id";
    private static final TodoDAO MOCK_TODO_DAO = mock(TodoDAO.class);
    private static final Logger LOG = LoggerFactory.getLogger(TodoResourceTest.class);
    public static final ResourceExtension TODO_RESOURCE =
                         ResourceExtension.builder()
                            .addResource(new TodoResource(MOCK_TODO_DAO))
                            .addProvider(StatusFilterFeature.class).build();

    private class ResponseObject<T> {
        private static final String ERR_MSG_KEY = "errors";
        private final Class<T> type;
        private Response response;
        private Object readObject;
        private T responseEntity;
        private boolean isError;

        ResponseObject(Response response, Class<T> type) {
            this.response = response;
            this.type = type;
            getObjectFromResponse();
            isError = isErrorInResponse();
        }

        private void getObjectFromResponse() {
            try {
                readObject = response.readEntity(Object.class);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                response.close();
            }
        }

        private boolean isErrorInResponse() {
            return readObject instanceof Map && ((Map)readObject).containsKey(ERR_MSG_KEY);
        }

        String getErrorMsgFromResponse() {
            return ((List)((Map)readObject).get("errors")).get(0).toString();
        }

        Optional<T> getResponseEntity() {
            if (isError == false) {
                responseEntity = new ObjectMapper().convertValue(readObject, type);
            }
            return Optional.of(responseEntity);
        }
    }

    @BeforeEach
    public void setUp() {
        initTodo();
    }

    private static void initTodo() {
        expectedTodo = TODO_TEMPLATE;
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
    public void tearDownAfterEach() {
        reset(MOCK_TODO_DAO);
    }

    @Test
    public void testCreateTodo_whenValidValuesReceivedThenCreatedStatusReturned() {
        // given
        mockInsertMethodCall();
        // when
        validTodoForCreate = copyCreateTodoForValidCreation();
        response = makeRequestToCreateNewTodo(validTodoForCreate);
        // then
        verifyInsertMethodWasCalledWithExpectedValue(validTodoForCreate);
        verifyExpectedResponseObject();
        String noErrorMsgExpected = "";
//        verifyExpectedValueWasReturned(response, expectedTodo, noErrorMsgExpected);
    }

    private void mockInsertMethodCall() {
        when(MOCK_TODO_DAO.insert(any())).thenReturn(expectedTodo);
    }

    private Response  makeRequestToCreateNewTodo(Todo todoToCreate) {
        return invokeForUri(baseTodoUri.getPath()).post(Entity.entity(todoToCreate, MediaType.APPLICATION_JSON_TYPE));
    }

    private static final Invocation.Builder invokeForUri(String uri) {
        return getTargetForUri(uri).request().accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private static final WebTarget getTargetForUri(String uri) {
        return TODO_RESOURCE.target(uri);
    }

    private void verifyInsertMethodWasCalledWithExpectedValue(Todo insertedTodo) {
        verify(MOCK_TODO_DAO).insert(todoInsertCaptor.capture());
        assertThat(todoInsertCaptor.getValue()).isEqualTo(insertedTodo);
    }

    private void verifyExpectedResponseObject() {
        verifyCreatedStatusReceived();
        verifyExpectedTodoObject();
    }

    private void verifyCreatedStatusReceived() {
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
    }

    private void verifyExpectedTodoObject() {
        Todo returnedTodo = getTodoObjectFromResponse();
        checkThatFieldsOriginallyRequiredToBeNullNowHaveValues(returnedTodo);
        checkThatRequiredFieldValuesHaveNotChanged(returnedTodo);
    }

    private Todo getTodoObjectFromResponse() {
        ResponseObject responseObject = new ResponseObject(response, Todo.class);
        return (Todo) responseObject.getResponseEntity().get();
    }

    private void checkThatFieldsOriginallyRequiredToBeNullNowHaveValues(Todo todoToCheck) {
        assertThat(todoToCheck.getId()).isNotNull();
        assertThat(todoToCheck.getCreatedAt()).isNotNull();
        assertThat(todoToCheck.getLastModifiedAt()).isNotNull();
    }

    private void checkThatRequiredFieldValuesHaveNotChanged(Todo todoToCheck) {
        assertThat(todoToCheck).isEqualToIgnoringGivenFields(expectedTodo, "id","createdAt","lastModifiedAt");
    }

    private void verifyExpectedValueWasReturned(Response response, Todo todoToCheck, String expectedErrMsg) {
        assertThat(response.hasEntity()).isTrue();
        ResponseObject responseObject = new ResponseObject(response, Todo.class);
        if (responseObject.isError)
            assertThat(responseObject.getErrorMsgFromResponse()).isEqualTo(expectedErrMsg);
        else {
            assertThat(responseObject.getResponseEntity().isPresent()).isTrue();
            assertThat(responseObject.getResponseEntity().get()).isEqualToComparingFieldByField(todoToCheck);
        }
    }



    @Test
    public void testCreateTodo_whenIdIsNullThenUnprocessableEntityStatusReturned() {
        // given
        Todo newTodoWithNullId = copyCreateNewTodoWithNullId();
        // when
        Response response = makeRequestToCreateNewTodo(newTodoWithNullId);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    @Test
    public void testCreateTodo_whenCreatedAtIsNullThenUnprocessableEntityStatusReturned() {
        // given
        Todo newTodoWithNullForCreatedAt = copyCreateNewTodoWithNullForCreatedAt();
        // when
        Response response = makeRequestToCreateNewTodo(newTodoWithNullForCreatedAt);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    @Test
    public void testCreateTodo_whenModifiedAtIsNullThenUnprocessableEntityStatusReturned() {
        // given
        Todo newTodoWithNullForModifiedAt = copyCreateNewTodoWithNullForLastModifiedAt();
        // when
        Response response = makeRequestToCreateNewTodo(newTodoWithNullForModifiedAt);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    @Test
    public void testCreateTodo_whenRequiredNullFieldsReceivedAndTextIsNullThenUnprocessableEntityStatusReturned() {
        // given
        Todo newTodoWithNullTextString= copyCreateTodoForValidCreationButWithNullText();
        // when
        Response response = makeRequestToCreateNewTodo(newTodoWithNullTextString);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    @Test
    public void testCreateTodo_whenRequiredNullFieldsReceivedAndTextIsEmptyThenUnprocessableEntityStatusReturned() {
        // given
        Todo newTodoWithEmptyTextString = copyCreateTodoWithRequiredNullAndEmptyTextString();
        // when
        Response response = makeRequestToCreateNewTodo(newTodoWithEmptyTextString);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    @Test
    public void testCreateTodo_whenRequiredNullFieldsReceivedAndIsCompletedIsNullThenUnprocessableEntityStatusReturned() {
        // given
        Todo invalidTodoForCreate = copyCreateTodoForValidCreationButWithNullIsCompleted();
        // when
        Response response = makeRequestToCreateNewTodo(invalidTodoForCreate);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    @Test
    public void testCreateTodo_whenRequiredNullFieldsReceivedAndDueDateOptionalIsNullThenUnprocessableEntityStatusReturned() {
        // given
        Todo invalidTodoForCreate = copyCreateTodoForValidCreationButWithNullDueDate();
        // when
        Response response = makeRequestToCreateNewTodo(invalidTodoForCreate);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    private void mockInsertMethodCallForTodoWithBlankText(Todo todoToInsert) {
        when(MOCK_TODO_DAO.insert(any())).thenReturn(todoToInsert);
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
        when(MOCK_TODO_DAO.insert(any())).thenReturn(null);
        validTodoForCreate = copyCreateTodoForValidCreation();
        // when
        response = makeRequestToCreateNewTodo(validTodoForCreate);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        assertThat(responseMessage(response)).contains(NULL_TODO_RETURNED_ERROR_MSG_KEY);
    }

    @Test
    public void testCreateTodo_whenDaoMethodThrowsExceptionThen500returned() {
        // given
        when(MOCK_TODO_DAO.insert((any())))
                .thenThrow(UnableToExecuteStatementException.class);
        validTodoForCreate = copyCreateTodoForValidCreation();
        // when
        response = makeRequestToCreateNewTodo(validTodoForCreate);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void testUpdateTodo_whenAllValuesExceptDueDateArePresentAndValidThen204Returned() {
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), expectedTodo);
        // then
        verifyDaoUpdateMethodWasCalledWithCorrectValue(expectedTodo);
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    private Response makeRequestToUpdateTodo(String uri, Todo todoToUpdate) {
        return invokeForUri(uri).put(Entity.entity(todoToUpdate, MediaType.APPLICATION_JSON_TYPE));
    }

    private void verifyDaoUpdateMethodWasCalledWithCorrectValue(Todo expectedTodo) {
        verify(MOCK_TODO_DAO, times(1)).update(expectedTodo);
    }

    @Test
    public void testUpdateTodo_whenAllValuesValidIncludingFutureValueForDueDateThen204Returned() {
        // given
        Todo validTodoWithFutureValueForDueDate = copyCreateTodoWithAllRequiredFieldsPresent();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), validTodoWithFutureValueForDueDate);
        // then
        verifyDaoUpdateMethodWasCalledWithCorrectValue(validTodoWithFutureValueForDueDate);
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void testUpdateTodo_whenNullValueForIdThen422Returned() {
        // given
        Todo todoWithNullId = copyCreateNewTodoWithNullId();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoWithNullId);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    private void assertThatResponseStatusIsUnprocessableEntity422(Response response) {
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
    }

    @Test
    public void testUpdateTodo_whenNullValueForTextThen422Returned() {
        // given
        Todo todoWithNullText = copyCreateNewTodoWithNullText();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoWithNullText);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenBlankValueForTextThen422Returned() {
        // given
        Todo todoWithBlankText = copyCreateNewTodoWithBlankText();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoWithBlankText);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenNullValueForIsCompletedThen422Returned() {
        // given
        Todo todoWithNullIsCompleted = copyCreateNewTodoWithNullForIsCompleted();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoWithNullIsCompleted);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenNullValueForCreatedAtThen422Returned() {
        // given
        Todo todoWithNullCreatedAt = copyCreateNewTodoWithNullForCreatedAt();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoWithNullCreatedAt);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenFutureValueForCreatedAtThen422Returned() {
        // given
        Todo todoWithFutureCreatedAt = copyCreateNewTodoWithFutureValueForCreatedAt();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoWithFutureCreatedAt);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenNullValueForLastModifiedAtThen422Returned() {
        // given
        Todo todoWithNullLastModifiedAt = copyCreateNewTodoWithNullForLastModifiedAt();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoWithNullLastModifiedAt);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenFutureValueForLastModifiedAtThen422Returned() {
        // given
        Todo todoWithFutureLastModifiedAt = copyCreateNewTodoWithFutureValueForLastModifiedAt();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoWithFutureLastModifiedAt);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenPresentValueForDueDateThen422Returned() {
        // given
        Todo todoWithPresentValueForDueDate = copyCreateNewTodoWithPresentValueForDueDate();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoWithPresentValueForDueDate);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_whenPastValueForDueDateThen422Returned() {
        // given
        Todo todoWithPastValueForDueDate = copyCreateNewTodoWithPastValueForDueDate();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoWithPastValueForDueDate);
        // then
        assertThatResponseStatusIsUnprocessableEntity422(response);
    }

    @Test
    public void testUpdateTodo_testValidForUpdateAnnotation_whenAllFieldsNonNullExceptDueDateThen204Returned() {
        // given/when
        Todo todoAllFieldsNonNullExceptDueDate = copyCreateNewTodoAllFieldValuesPresentExceptDueDate();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoAllFieldsNonNullExceptDueDate);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void testUpdateTodo_testValidForUpdateAnnotation_whenValueForDueDateAndIsCompletedFalseThen204Returned() {
        // given/when
        Todo todoValueForDueDateAndIsCompletedFalse = copyCreateNewTodoValueForDueDateAndIsCompletedFalse();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoValueForDueDateAndIsCompletedFalse);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void testUpdateTodo_testValidForUpdateAnnotation_whenValueForDueDateAndIsCompletedTrueThen422Returned() {
        // given/when
        Todo todoValueForDueDateAndIsCompletedTrue = copyCreateNewTodoValueForDueDateAndIsCompletedTrue();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), todoValueForDueDateAndIsCompletedTrue);
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
        assertThat(responseMessage(response)).contains(NULL_LIST_OF_TODOS_RETURNED_ERROR_MSG_KEY);
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

