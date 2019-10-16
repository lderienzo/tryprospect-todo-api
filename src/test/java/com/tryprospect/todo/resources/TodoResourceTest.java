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
import lombok.extern.slf4j.Slf4j;

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
import static com.tryprospect.todo.utils.TodoCreator.*;
import static com.tryprospect.todo.validation.ValidationMessageHandler.getMessageFromPropertiesFile;
import static com.tryprospect.todo.validation.ValidationMessages.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// TODO: GO OVER TESTS AND REFACTOR/CLEAN-UP TO MAKE SURE THEY'RE RELEVANT.
// TODO: look into a better way of organizing and cleaning up tests.
// TODO: The close() method should be invoked on all instances that contain an un-consumed entity input stream to ensure
//  the resources associated with the instance are properly cleaned-up and prevent potential memory leaks.
@Slf4j
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


    @Nested
    class testCreateTodo {

        @Test
        public void whenValidValuesReceivedExcludingDueDateThen201Status() {
            // given
            when(MOCK_TODO_DAO.insert(any())).thenReturn(expectedTodo);
            validTodo = validForCreationWithoutDueDate();
            // when
            makeRequestToCreateNewTodo(validTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.CREATED_201);
            verifyActualTodoAgainstExpected();
        }

        private void makeRequestToCreateNewTodo(Todo todoToCreate) {
            response = invokeForUri(baseTodoUri.getPath()).post(Entity.json(todoToCreate));
        }

        @Test
        public void whenValidValuesReceivedIncludingDueDateThen201Status() {
            // given
            expectedTodo = expectedValidTodoWithValueForDueDate();
            when(MOCK_TODO_DAO.insert(any())).thenReturn(expectedTodo);
            // when
            validTodo = validForCreationWithDueDate();
            makeRequestToCreateNewTodo(validTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.CREATED_201);
            verifyActualTodoAgainstExpected();
        }

        @Test
        public void whenIdIsNonNullThen400Error() {
            // given
            invalidTodo = invalidForCreationWithNonNullId();
            // when
            makeRequestToCreateNewTodo(invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenCreatedAtIsNonNullThen400Error() {
            // given
            invalidTodo = invalidForCreationWithNonNullCreatedAt();
            // when
            makeRequestToCreateNewTodo(invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenLastModifiedAtIsNonNullThen400Error() {
            // given
            invalidTodo = invalidForCreationWithNonNullLastModifiedAt();
            // when
            makeRequestToCreateNewTodo(invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenTextIsNullThen400Error() {
            // given
            invalidTodo = invalidForCreationWithNullText();
            // when
            makeRequestToCreateNewTodo(invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenTextIsEmptyThen400Error() {
            // given
            invalidTodo = invalidForCreationWithBlankText();
            // when
            makeRequestToCreateNewTodo(invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenIsCompletedIsNullThen400Error() {
            // given
            invalidTodo = invalidForCreateWithNullIsCompleted();
            // when
            makeRequestToCreateNewTodo(invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_CREATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenNullTodoReturnedByDaoMethodThen500Error() {
            // given
            when(MOCK_TODO_DAO.insert(any())).thenReturn(null);
            validTodo = validForCreationWithoutDueDate();
            // when
            makeRequestToCreateNewTodo(validTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
            checkForAppropriateErrorMessage(NULL_TODO_RETURNED_ERROR_MSG_KEY);
        }

        @Test
        public void whenInvalidTodoReturnedThen500Error() {
            // given
            validTodo = validForCreationWithoutDueDate();
            invalidTodo = returnedInvalidTodoWithNullId();
            when(MOCK_TODO_DAO.insert(any())).thenReturn(invalidTodo);
            // when
            makeRequestToCreateNewTodo(validTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
            checkForAppropriateErrorMessage(TODO_ID_ERROR_MSG_PREFIX_KEY, NULL_FIELD_ERROR_MSG_KEY);
        }

        @Test
        public void whenDaoMethodThrowsExceptionThen500Error() {
            // given
            when(MOCK_TODO_DAO.insert((any()))).thenThrow(UnableToExecuteStatementException.class);
            validTodo = validForCreationWithoutDueDate();
            // when
            makeRequestToCreateNewTodo(validTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
            assertThat(getResponseMessage()).isEqualTo(UNABLE_TO_EXECUTE_STATEMENT_ERROR);
        }
    }

    private final Invocation.Builder invokeForUri(String uri) {
        return getTargetForUri(uri).request().accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private final WebTarget getTargetForUri(String uri) {
        return TODO_RESOURCE.target(uri);
    }

    private int responseStatusCode() {
        return response.getStatusInfo().getStatusCode();
    }

    private void verifyActualTodoAgainstExpected() {
        Optional<Todo> actualTodo = Optional.of(response.readEntity(Todo.class));
        assertThat(actualTodo.isPresent()).isTrue();
        assertThat(actualTodo.get()).isEqualToComparingFieldByField(expectedTodo);
    }

    private void checkForAppropriateErrorMessage(String... expectedMessageKeys) {
        String actualMessageReceived = getResponseMessage();
        assertThat(actualMessageReceived).isNotEmpty();
        String expectedMessage;
        if (onlyOneMessageKeyPresent(expectedMessageKeys)) expectedMessage =
                getMessageFromPropertiesFile(expectedMessageKeys[0]);
        else expectedMessage = concatenateMultipleMessages(expectedMessageKeys);
        assertThat(actualMessageReceived).isEqualTo(expectedMessage);
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
    
    @Nested
    class testUpdateTodo {

        @Test
        public void whenValidValuesPresentExcludingDueDateThen204Status() {
            validTodo = validForUpdateWithoutDueDate();
            // when
            makeRequestToUpdateTodo(uriWithId.getPath(), validTodo);
            // then
            verifyDaoUpdateMethodWasCalledWithCorrectValue(validTodo);
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }

        private void makeRequestToUpdateTodo(String uri, Todo todoToUpdate) {
            response = invokeForUri(uri).put(Entity.entity(todoToUpdate, MediaType.APPLICATION_JSON_TYPE));
        }

        private void verifyDaoUpdateMethodWasCalledWithCorrectValue(Todo expectedTodo) {
            verify(MOCK_TODO_DAO, times(1)).update(expectedTodo);
        }

        @Test
        public void whenValidValuesPresentIncludingDueDateThen204Status() {
            // given
            validTodo = validForUpdateWithDueDate();
            // when
            makeRequestToUpdateTodo(uriWithId.getPath(), validTodo);
            // then
            verifyDaoUpdateMethodWasCalledWithCorrectValue(validTodo);
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }

        @Test
        public void whenIdNullThen400Error() {
            // given
            invalidTodo = invalidForUpdateWithNullId();
            // when
            makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenTextNullThen400Error() {
            // given
            invalidTodo = invalidForUpdateWithNullText();
            // when
            makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenTextBlankThen400Error() {
            // given
            invalidTodo = invalidForUpdateWithBlankText();
            // when
            makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenIsCompletedNullThen400Error() {
            // given
            invalidTodo = invalidForUpdateWithNullIsCompleted();
            // when
            makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenCreatedAtNonNullThen400Error() {
            // given
            invalidTodo = invalidForUpdateWithNonNullCreatedAt();
            // when
            makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenLastModifiedAtNonNullThen400Error() {
            // given
            invalidTodo = invalidForUpdateWithNonNullLastModifiedAt();
            // when
            makeRequestToUpdateTodo(uriWithId.getPath(), invalidTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
            checkForAppropriateErrorMessage(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);
        }

        @Test
        public void whenDaoMethodThrowsExceptionThen500Error() {
            // given
            doThrow(UnableToExecuteStatementException.class).when(MOCK_TODO_DAO).update(any());
            validTodo = validForUpdateWithoutDueDate();
            // when
            makeRequestToUpdateTodo(uriWithId.getPath(), validTodo);
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
            assertThat(getResponseMessage()).isEqualTo(UNABLE_TO_EXECUTE_STATEMENT_ERROR);
        }
    }

    @Nested
    class testGetTodo {

        @Test
        public void whenValidIdPassedThenCorrectTodoFoundWith200Status() {
            // given
            when(MOCK_TODO_DAO.findById(expectedTodo.getId())).thenReturn(Optional.of(expectedTodo));
            // when
            makeGetRequest(uriWithId.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.OK_200);
            verifyActualTodoAgainstExpected();
        }

        @Test
        public void whenInvalidIdPassedThen404Error() {
            // given
            uriWithId = buildRequestUriWithIdInPath(INVALID_ID);
            // when
            makeGetRequest(uriWithId.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
        }

        @Test
        public void whenDaoMethodThrowsExceptionThen500Error() {
            // given
            when(MOCK_TODO_DAO.findById(expectedTodo.getId()))
                    .thenThrow(UnableToExecuteStatementException.class);
            // when
            makeGetRequest(uriWithId.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
            assertThat(getResponseMessage()).isEqualTo(UNABLE_TO_EXECUTE_STATEMENT_ERROR);
        }

        @Test
        public void whenNullValueReturnedByDaoMethodThen204Status() {
            // given
            when(MOCK_TODO_DAO.findById(expectedTodo.getId())).thenReturn(null);
            // when
            makeGetRequest(uriWithId.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }

        @Test
        public void whenInvalidTodoReturnedThen500Error() {
            // given
            invalidTodo = returnedInvalidTodoWithNullId();
            when(MOCK_TODO_DAO.findById(expectedTodo.getId())).thenReturn(Optional.of(invalidTodo));
            // when
            makeGetRequest(uriWithId.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
            checkForAppropriateErrorMessage(TODO_ID_ERROR_MSG_PREFIX_KEY, NULL_FIELD_ERROR_MSG_KEY);
        }
    }

    private void makeGetRequest(String uri) {
        response = invokeForUri(uri).get();
    }

    @Nested
    class testGetTodos {

        @Test
        public void whenCalledNormallyThenExpectedListReturnedWith200Status() {
            // given
            List<Todo> expectedTodos = createListOfSingleTodo();
            mockFindAllMethodCallToReturnExpectedListOfTodos(expectedTodos);
            // when
            makeGetRequest(baseTodoUri.getPath());
            List<Todo> actualTodoListReturned = response.readEntity(new GenericType<List<Todo>>() {
            });
            ;
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.OK_200);
            assertThat(actualTodoListReturned).containsAll(expectedTodos);
        }

        private List<Todo> createListOfSingleTodo() {
            return Collections.singletonList(expectedTodo);
        }

        private void mockFindAllMethodCallToReturnExpectedListOfTodos(List<Todo> todos) {
            when(MOCK_TODO_DAO.findAll()).thenReturn(todos);
        }

        @Test
        public void whenInvalidTodoPresentInListThen500Error() {
            // given
            List<Todo> expectedTodos = createListOfSingleInvalidTodo();
            mockFindAllMethodCallToReturnExpectedListOfTodos(expectedTodos);
            // when
            makeGetRequest(baseTodoUri.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
            assertThat(getResponseMessage()).isEqualTo(constructExpectedErrorMessage());
        }

        private String constructExpectedErrorMessage() {
            return getMessageFromPropertiesFile(INVALID_TODO_PRESENT_IN_RETURN_LIST_MSG_KEY)
                    + " " + getMessageFromPropertiesFile(TODO_ID_ERROR_MSG_PREFIX_KEY)
                    + getMessageFromPropertiesFile(NULL_FIELD_ERROR_MSG_KEY);
        }

        private List<Todo> createListOfSingleInvalidTodo() {
            invalidTodo = returnedInvalidTodoWithNullId();
            return Collections.singletonList(invalidTodo);
        }

        @Test
        public void whenNullListReturnedFromDaoMethodThen500Error() {
            //given/when
            mockFindAllMethodCallToReturnExpectedListOfTodos(null);
            // when
            makeGetRequest(baseTodoUri.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
            checkForAppropriateErrorMessage(NULL_LIST_OF_TODOS_RETURNED_ERROR_MSG_KEY);
        }

        @Test
        public void whenDaoMethodThrowsExceptionThen500Error() {
            // given
            when(MOCK_TODO_DAO.findAll()).thenThrow(UnableToExecuteStatementException.class);
            // when
            makeGetRequest(baseTodoUri.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
            assertThat(getResponseMessage()).isEqualTo(UNABLE_TO_EXECUTE_STATEMENT_ERROR);
        }
    }

    @Nested
    class testDeleteTodo {

        @Test
        public void whenValidIdPassedThen204Status() {
            // given
            mockTodoDaoDeleteByIdMethodCall(expectedTodo.getId());
            // when
            makeDeleteRequest(uriWithId.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }

        private void mockTodoDaoDeleteByIdMethodCall(UUID id) {
            when(MOCK_TODO_DAO.findById(id)).thenReturn(Optional.of(expectedTodo));
        }

        private void makeDeleteRequest(String path) {
            response = invokeForUri(path).delete();
        }

        @Test
        public void whenEmptyIdPassedThen405Error() {
            // given
            uriWithId = buildRequestUriWithIdInPath("");
            // when
            makeDeleteRequest(uriWithId.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED_405);
        }

        @Test
        public void whenInvalidIdPassedThen404returned() {
            // given
            uriWithId = buildRequestUriWithIdInPath(INVALID_ID);
            // when
            makeDeleteRequest(uriWithId.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
        }

        @Test
        public void whenDaoMethodThrowsExceptionThen500Error() {
            // given
            doThrow(UnableToExecuteStatementException.class).when(MOCK_TODO_DAO).deleteById(expectedTodo.getId());
            // when
            makeDeleteRequest(uriWithId.getPath());
            // then
            assertThat(responseStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
            assertThat(getResponseMessage()).isEqualTo(UNABLE_TO_EXECUTE_STATEMENT_ERROR);
        }
    }
}