package com.tryprospect.todo.jackson;

import static com.tryprospect.todo.utils.JSONTestUtils.*;
import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TodoJSONTest {

    @Test
    public void testJsonSerializationDeserialization() {
        // When
        String expectedTodoJson = "{\"id\":\"ec8a31b2-6e83-43f3-ae12-e53fb5c19b1b\",\"text\":\"Some test todo text\"," +
                "\"is_completed\":false,\"created_at\":1559424504961,\"last_modified_at\":1562089781522,\"due_date\":\"\"," +
                "\"isCompleted\":false}";
        // Then
        assertThat(serializeFromTodoObjectIntoJson(TODO_TEMPLATE)).isEqualTo(expectedTodoJson);
    }
}
