//package com.tryprospect;
//
//import static com.tryprospect.todo.utils.TodoCreator.validForCreationWithoutDueDate;
//import static io.restassured.RestAssured.*;
//import static java.time.Clock.tickMinutes;
//import static org.assertj.core.api.Java6Assertions.assertThat;
//
//import java.time.Clock;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.time.format.FormatStyle;
//import java.util.Locale;
//
//import org.junit.jupiter.api.Test;
//
//import io.restassured.http.ContentType;
//import io.restassured.response.Response;
//import io.restassured.RestAssured.*;
//import io.restassured.matcher.RestAssuredMatchers.*;
//import org.hamcrest.Matchers.*;
//
//import com.tryprospect.todo.api.Todo;
//
//public class TodoRestAssuredTest {
//
//    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
//            .withLocale(Locale.US )
//            .withZone(ZoneId.systemDefault());
//
//    @Test
//    public void testGetBookByTitle() {
//        Todo validTodoForCreation = validForCreationWithoutDueDate();
////        BookRequest request = new BookRequest();
////        request.setTitle("War and peace");
//
//        Response response = given()
//                .body(validTodoForCreation)
//                .contentType(ContentType.JSON)
//                .expect()
//                .contentType(ContentType.JSON)
//                .statusCode(javax.ws.rs.core.Response.Status.OK.getStatusCode())
//                .when()
//                .post("http://localhost:8080/todos");
//
//        Todo actualTodoCreted = response.as(Todo.class);
//
//        assertThat(actualTodoCreted.getText()).isEqualTo(validTodoForCreation.getText());
//        assertThat(actualTodoCreted.getCompleted()).isFalse();
//        assertThat(actualTodoCreted.getDueDate().isPresent()).isFalse();
//        assertThat(formattedDate(actualTodoCreted.getCreatedAt())).isEqualTo(formattedDate(now()));
//        assertThat(formattedDate(actualTodoCreted.getLastModifiedAt())).isEqualTo(formattedDate(now()));
//    }
//
//    private Instant now() {
//        return Clock.tickMinutes(ZoneId.systemDefault()).instant();
//    }
//
//    private String formattedDate(Instant instantToFormat) {
//        return FORMATTER.format(instantToFormat);
//    }
//
//}
