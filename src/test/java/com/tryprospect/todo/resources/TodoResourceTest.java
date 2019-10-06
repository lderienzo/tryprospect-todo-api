package com.tryprospect.todo.resources;

import com.tryprospect.todo.api.Todo;
import com.tryprospect.todo.api.validation.CommonTodoTestMembers;
import com.tryprospect.todo.db.TodoDAO;
import com.tryprospect.todo.container.StatusFilterFeature;
import com.tryprospect.todo.exceptionmappers.ConstraintViolationExceptionMapper;
import com.tryprospect.todo.exceptionmappers.JdbiExceptionMapper;
import com.tryprospect.todo.exceptionmappers.TodoValidationExceptionMapper;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import org.eclipse.jetty.http.HttpStatus;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.logging.*;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

import java.net.URI;
import java.util.*;

import static com.tryprospect.todo.exceptionmappers.JdbiExceptionMapper.UNABLE_TO_EXECUTE_STATEMENT_ERROR;
import static com.tryprospect.todo.utils.json.JsonHandler.TODO_TEMPLATE;
import static com.tryprospect.todo.utils.TestTodoCreator.*;
import static com.tryprospect.todo.validation.ValidationMessageHandler.getMessageFromPropertiesFile;
import static com.tryprospect.todo.validation.ValidationMessages.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// TODO: GO OVER TESTS AND REFACTOR/CLEAN-UP TO MAKE SURE THEY'RE RELEVANT.
// TODO: look into a better way of organizing and cleaning up tests.
// TODO: The close() method should be invoked on all instances that contain an un-consumed entity input stream to ensure
//  the resources associated with the instance are properly cleaned-up and prevent potential memory leaks.
@ExtendWith(DropwizardExtensionsSupport.class)
public class TodoResourceTest extends CommonTodoTestMembers {

    private static Todo expectedTodo;
    private static URI baseTodoUri;
    private static URI uriWithId;
    private static Response response;
    private static final String INVALID_ID = "ec8a31b2-6e83-43f3-ae12-e53fb5c19b1z";
    private static final TodoDAO MOCK_TODO_DAO = mock(TodoDAO.class);
    private static final Logger LOG = LoggerFactory.getLogger(TodoResourceTest.class);
    public static final ResourceExtension TODO_RESOURCE = ResourceExtension.builder()
            .addResource(new TodoResource(MOCK_TODO_DAO))
            .addProvider(StatusFilterFeature.class)
            .setRegisterDefaultExceptionMappers(false)
            .addProvider(ConstraintViolationExceptionMapper.class)
            .addProvider(TodoValidationExceptionMapper.class)
            .addProvider(JdbiExceptionMapper.class)
            .build();

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
    public void testCreateTodo_whenValidValuesReceivedExcludingDueDateThen201Status() {
        // given
        mockInsertMethodCall();
        validTodo = copyCreateTodoForValidCreationExcludingDueDate();
        // when
        response = makeRequestToCreateNewTodo(validTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        Optional<Todo> returnedTodo = getTodoObjectFromResponse();
        assertThat(returnedTodo.isPresent()).isTrue();
        assertThat(returnedTodo.get()).isEqualToComparingFieldByField(expectedTodo);
    }

    private void mockInsertMethodCall() {
        when(MOCK_TODO_DAO.insert(any())).thenReturn(expectedTodo);
    }

    private Response makeRequestToCreateNewTodo(Todo todoToCreate) {
        return invokeForUri(baseTodoUri.getPath()).post(Entity.entity(todoToCreate, MediaType.APPLICATION_JSON_TYPE));
    }

    private static final Invocation.Builder invokeForUri(String uri) {
        return getTargetForUri(uri).request().accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private static final WebTarget getTargetForUri(String uri) {
        return TODO_RESOURCE.target(uri);
    }

    // TODO: REPEATED CODE!! SHAME!
    private Optional<Todo> getTodoObjectFromResponse() {
        Optional<Todo> readObject = Optional.empty();
        try {
            readObject = Optional.of(response.readEntity(Todo.class));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.close();
        }
        return readObject;
    }

    @Test
    public void testCreateTodo_whenValidValuesReceivedIncludingDueDateThen201Status() {
        // given
        expectedTodo = copyCreateNewTodoWithValueForDueDate();
        mockInsertMethodCall();
        // when
        validTodo = copyCreateTodoForValidCreationIncludingDueDate();
        response = makeRequestToCreateNewTodo(validTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        Optional<Todo> returnedTodo = getTodoObjectFromResponse();
        assertThat(returnedTodo.isPresent()).isTrue();
        assertThat(returnedTodo.get()).isEqualToComparingFieldByField(expectedTodo);
    }

    @Test
    public void testCreateTodo_whenIdIsNonNullThen400Error() {
        // given
        invalidTodo = copyCreateTodoForValidCreationButWithNonNullId();
        // when
        response = makeRequestToCreateNewTodo(invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
    }

    private void checkForAppropriateErrorMessage(String... expectedMessageKeys) {
        String actualMessageReceived = getResponseMessage();
        assertThat(actualMessageReceived).isNotEmpty();
        String expectedMessage;
        if (onlyOneMessageKeyPresent(expectedMessageKeys))
            expectedMessage = getMessageFromPropertiesFile(expectedMessageKeys[0]);
        else
            expectedMessage = concatenateMultipleMessages(expectedMessageKeys);
        assertThat(actualMessageReceived).isEqualTo(expectedMessage);
    }

    private boolean onlyOneMessageKeyPresent(String... expectedMessageKeys) {
        return expectedMessageKeys.length == 1;
    }

    private String concatenateMultipleMessages(String[] messageKeys) {
        String expectedMessage = "";
        for (int i = 0; i < messageKeys.length; i++) {
            expectedMessage += getMessageFromPropertiesFile(messageKeys[i])
                    + insertSpaceAfterFirstStringButNotAfterLast(i, messageKeys);
        }
        return expectedMessage;
    }

    private String insertSpaceAfterFirstStringButNotAfterLast(int index, String[] array) {
        return (index > 0 && index == array.length ? " " : "");
    }

    @Test
    public void testCreateTodo_whenCreatedAtIsNonNullThen400Error() {
        // given
        invalidTodo = copyCreateTodoForCreationWithNonNullCreatedAt();
        // when
        response = makeRequestToCreateNewTodo(invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
    }

    @Test
    public void testCreateTodo_whenLastModifiedAtIsNonNullThen400Error() {
        // given
        invalidTodo = copyCreateTodoForCreationWithNonNullLastModifiedAt();
        // when
        response = makeRequestToCreateNewTodo(invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
    }

    @Test
    public void testCreateTodo_whenTextIsNullThen400Error() {
        // given
        invalidTodo = copyCreateTodoForCreationWithNullText();
        // when
        response = makeRequestToCreateNewTodo(invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
    }

    @Test
    public void testCreateTodo_whenTextIsEmptyThen400Error() {
        // given
        invalidTodo = copyCreateTodoForCreationWithEmptyText();
        // when
        response = makeRequestToCreateNewTodo(invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
    }

    @Test
    public void testCreateTodo_whenIsCompletedIsNullThen400Error() {
        // given
        invalidTodo = copyCreateTodoForValidCreationButWithNullIsCompleted();
        // when
        response = makeRequestToCreateNewTodo(invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
    }

    @Test
    public void testCreateTodo_whenNullTodoReturnedByDaoMethodThen500Error() {
        // given
        when(MOCK_TODO_DAO.insert(any())).thenReturn(null);
        validTodo = copyCreateTodoForValidCreationExcludingDueDate();
        // when
        response = makeRequestToCreateNewTodo(validTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        checkForAppropriateErrorMessage(NULL_TODO_RETURNED_ERROR_MSG_KEY);
    }

    private String getResponseMessage() {
        Optional<String> entityOptional = Optional.empty();
        try {
            response.bufferEntity();
            entityOptional = Optional.of(response.readEntity(String.class));
        } catch (ProcessingException e) {
            LOG.error(e, () -> "Could not read entity from response.");
        }
        return entityOptional.orElse("");
    }

    @Test
    public void testCreateTodo_whenInvalidTodoReturnedThen500Error() {
        // given
        validTodo = copyCreateTodoForValidCreationExcludingDueDate();
        invalidTodo = copyCreateNewTodoWithNullId();
        when(MOCK_TODO_DAO.insert(any())).thenReturn(invalidTodo);
        // when
        response = makeRequestToCreateNewTodo(validTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        checkForAppropriateErrorMessage(TODO_ID_ERROR_MSG_PREFIX_KEY, NULL_FIELD_ERROR_MSG_KEY);
    }

    @Test
    public void testCreateTodo_whenDaoMethodThrowsExceptionThen500Error() {
        // given
        when(MOCK_TODO_DAO.insert((any()))).thenThrow(UnableToExecuteStatementException.class);
        validTodo = copyCreateTodoForValidCreationExcludingDueDate();
        // when
        response = makeRequestToCreateNewTodo(validTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        assertThat(getResponseMessage()).isEqualTo(UNABLE_TO_EXECUTE_STATEMENT_ERROR);
    }





    @Test
    public void testUpdateTodo_whenValidValuesPresentExcludingDueDateThen204Status() {
        validTodo = copyCreateTodoForUpdateExcludingDueDate();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), validTodo);
        // then
        verifyDaoUpdateMethodWasCalledWithCorrectValue(validTodo);
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    private Response makeRequestToUpdateTodo(String uri, Todo todoToUpdate) {
        return invokeForUri(uri).put(Entity.entity(todoToUpdate, MediaType.APPLICATION_JSON_TYPE));
    }

    private void verifyDaoUpdateMethodWasCalledWithCorrectValue(Todo expectedTodo) {
        verify(MOCK_TODO_DAO, times(1)).update(expectedTodo);
    }

    @Test
    public void testUpdateTodo_whenValidValuesPresentIncludingDueDateThen204Status() {
        // given
        validTodo = copyCreateTodoForUpdateIncludingDueDate();
        // when
        Response response = makeRequestToUpdateTodo(uriWithId.getPath(), validTodo);
        // then
        verifyDaoUpdateMethodWasCalledWithCorrectValue(validTodo);
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void testUpdateTodo_whenIdNullThen400Error() {
        // given
        invalidTodo = copyCreateNewTodoForUpdateWithNullId();
        // when
        response = makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
    }

    @Test
    public void testUpdateTodo_whenTextNullThen400Error() {
        // given
        invalidTodo = copyCreateTodoForUpdateButTextNull();
        // when
        response = makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
    }

    @Test
    public void testUpdateTodo_whenTextBlankThen400Error() {
        // given
        invalidTodo = copyCreateTodoForUpdateButTextBlank();
        // when
        response = makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
    }

    @Test
    public void testUpdateTodo_whenIsCompletedNullThen400Error() {
        // given
        invalidTodo = copyCreateTodoForUpdateIsCompletedNull();
        // when
        response = makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
    }

    @Test
    public void testUpdateTodo_whenCreatedAtNonNullThen400Error() {
        // given
        invalidTodo = copyCreateNewTodoForUpdateWithNonNullCreatedAt();
        // when
        response = makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
    }

    @Test
    public void testUpdateTodo_whenLastModifiedAtNonNullThen400Error() {
        // given
        invalidTodo = copyCreateNewTodoForUpdateWithNonNullLastModifiedAt();
        // when
        response = makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
    }

    @Test
    public void testUpdateTodo_whenDaoMethodThrowsExceptionThen500Error() {
        // given
        doThrow(UnableToExecuteStatementException.class).when(MOCK_TODO_DAO).update(any());
        validTodo = copyCreateTodoForUpdateExcludingDueDate();
        // when
        response = makeRequestToUpdateTodo(uriWithId.getPath(), validTodo);
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        assertThat(getResponseMessage()).isEqualTo(UNABLE_TO_EXECUTE_STATEMENT_ERROR);
    }




    // here
    @Test
    public void testGetTodo_whenValidIdPassedThenCorrectTodoFoundWith200Status() {
        // given
        when(MOCK_TODO_DAO.findById(expectedTodo.getId())).thenReturn(Optional.of(expectedTodo));
        // when
        response = makeGetRequestToReturnResponse(uriWithId.getPath());
        Optional<Todo> returnedTodoOptional = getTodoObjectFromResponse();
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(returnedTodoOptional.isPresent()).isTrue();
        assertThat(returnedTodoOptional.get()).isEqualToComparingFieldByField(expectedTodo);
    }

    private Response makeGetRequestToReturnResponse(String uri) {
        return invokeForUri(uri).get();
    }

    @Test
    public void testGetTodo_whenInvalidIdPassedThen404Error() {
        // given
        uriWithId = buildRequestUriWithIdInPath(INVALID_ID);
        // when
        response = makeRequestToReturnResponse(uriWithId.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void testGetTodo_whenDaoMethodThrowsExceptionThen500Error() {
        // given
        when(MOCK_TODO_DAO.findById(expectedTodo.getId()))
                .thenThrow(UnableToExecuteStatementException.class);
        // when
        response = makeRequestToReturnResponse(uriWithId.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        assertThat(getResponseMessage()).isEqualTo(UNABLE_TO_EXECUTE_STATEMENT_ERROR);
    }

    @Test
    public void testGetTodo_whenNullValueReturnedByDaoMethodThen204Status() {
        // given
        when(MOCK_TODO_DAO.findById(expectedTodo.getId())).thenReturn(null);
        // when
        Response response = makeRequestToReturnResponse(uriWithId.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void testGetTodo_whenInvalidTodoReturnedThen500Error() {
        // given
        invalidTodo = copyCreateNewTodoWithNullId();
        when(MOCK_TODO_DAO.findById(expectedTodo.getId())).thenReturn(Optional.of(invalidTodo));
        // when
        response = makeRequestToReturnResponse(uriWithId.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        checkForAppropriateErrorMessage(TODO_ID_ERROR_MSG_PREFIX_KEY, NULL_FIELD_ERROR_MSG_KEY);
    }





    @Test
    public void testGetTodoS_whenCalledNormallyThenExpectedListReturnedWith200Status() {
        // given
        List<Todo> expectedTodos = createListOfSingleTodo();
        mockFindAllMethodCallToReturnExpectedListOfTodos(expectedTodos);
        // when
        response = makeGetRequestToReturnResponse(baseTodoUri.getPath());
        List<Todo> returnedTodos = getListOfTodosFromResponse();
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(returnedTodos).containsAll(expectedTodos);
    }

    private List<Todo> getListOfTodosFromResponse() {
        List<Todo> readObject = null;
        try {
            readObject = response.readEntity(new GenericType<List<Todo>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.close();
        }
        return readObject;
    }

    // TODO: COME BACK... FRIED RIGHT NOW.
    private <T> T getObjectFromResponse(Class<?> cls) {
        T readObject = null;
        try {
            readObject = (T) response.readEntity(cls);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.close();
        }
        return readObject;
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
    public void testGetTodoS_whenInvalidTodoPresentInListThen500Error() {
        // given
        List<Todo> expectedTodos = createListOfSingleInvalidTodo();
        mockFindAllMethodCallToReturnExpectedListOfTodos(expectedTodos);
        // when
        response = makeRequestToReturnResponse(baseTodoUri.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        assertThat(getResponseMessage()).isEqualTo(constructExpectedErrorMessage());
    }

    private String constructExpectedErrorMessage() {
        return getMessageFromPropertiesFile(INVALID_TODO_PRESENT_IN_RETURN_LIST_MSG_KEY)
                +" "+getMessageFromPropertiesFile(TODO_ID_ERROR_MSG_PREFIX_KEY)
                +getMessageFromPropertiesFile(NULL_FIELD_ERROR_MSG_KEY);
    }

    private List<Todo> createListOfSingleInvalidTodo() {
        invalidTodo = copyCreateNewTodoWithNullId();
        return Collections.singletonList(invalidTodo);
    }

    @Test
    public void testGetTodoS_whenNullListReturnedFromDaoMethodThen500Error() {
        //given/when
        mockFindAllMethodCallToReturnExpectedListOfTodos(null);
        // when
        response = makeRequestToReturnResponse(baseTodoUri.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        checkForAppropriateErrorMessage(NULL_LIST_OF_TODOS_RETURNED_ERROR_MSG_KEY);
    }

    private Response makeRequestToReturnResponse(String uri) {
        return invokeForUri(uri).get();
    }

    @Test
    public void testGetTodoS_whenDaoMethodThrowsExceptionThen500Error() {
        // given
        when(MOCK_TODO_DAO.findAll()).thenThrow(UnableToExecuteStatementException.class);
        // when
        response = makeRequestToReturnResponse(baseTodoUri.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        assertThat(getResponseMessage()).isEqualTo(UNABLE_TO_EXECUTE_STATEMENT_ERROR);
    }





    @Test
    public void testDeleteTodo_whenValidIdPassedThen204Status() {
        // given
        mockTodoDaoDeleteByIdMethodCall(expectedTodo.getId());
        // when
        int returnStatusCode = makeRequestToDeleteTodoAndReturnStatus(uriWithId.getPath());
        // then
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

    @Test
    public void testDeleteTodo_whenEmptyIdPassedThen405Error() {
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
        response = makeDeleteTodoRequestWithPathAndReturnResponse(uriWithId.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void testDeleteTodo_whenDaoMethodThrowsExceptionThen500Error() {
        // given
        doThrow(UnableToExecuteStatementException.class).when(MOCK_TODO_DAO).deleteById(expectedTodo.getId());
        // when
        response = makeDeleteTodoRequestWithPathAndReturnResponse(uriWithId.getPath());
        // then
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        assertThat(getResponseMessage()).isEqualTo(UNABLE_TO_EXECUTE_STATEMENT_ERROR);
    }
}