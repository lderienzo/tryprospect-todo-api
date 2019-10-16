package com.tryprospect.todo.api;

import static com.tryprospect.todo.validation.ValidationMessages.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tryprospect.todo.jackson.deserializer.TodoInstantDeserializer;
import com.tryprospect.todo.jackson.serializer.NonOptionalInstantSerializer;
import com.tryprospect.todo.jackson.serializer.OptionalInstantSerializer;

import lombok.Data;


@Data
public final class Todo {

  @NotNull(message = "{"+TODO_ID_ERROR_MSG_PREFIX_KEY+"}"+"{"+NULL_FIELD_ERROR_MSG_KEY+"}")
  private UUID id;

  @NotEmpty(message = "{"+TODO_TEXT_ERROR_MSG_PREFIX_KEY+"}"+"{"+NULL_FIELD_ERROR_MSG_KEY+"}")
  private final String text;

  @NotNull(message = "{"+TODO_IS_COMPLETED_ERROR_MSG_PREFIX_KEY+"}"+"{"+NULL_FIELD_ERROR_MSG_KEY+"}")
  private final Boolean isCompleted;

  @NotNull(message = "{"+TODO_CREATED_AT_ERROR_MSG_PREFIX_KEY+"}"+"{"+NULL_FIELD_ERROR_MSG_KEY+"}")
  @Past(message = "{"+TODO_CREATED_AT_ERROR_MSG_PREFIX_KEY+"}"+"{"+PAST_DATE_ERROR_MSG_KEY+"}")
  @JsonProperty("created_at")
  @JsonDeserialize(using = TodoInstantDeserializer.class, as = Instant.class)
  @JsonSerialize(using = NonOptionalInstantSerializer.class)
  private final Instant createdAt;

  @NotNull(message = "{"+TODO_LAST_MODIFIED_AT_ERROR_MSG_PREFIX_KEY+"}"+"{"+NULL_FIELD_ERROR_MSG_KEY+"}")
  @Past(message = "{"+TODO_LAST_MODIFIED_AT_ERROR_MSG_PREFIX_KEY+"}"+"{"+PAST_DATE_ERROR_MSG_KEY+"}")
  @JsonProperty("last_modified_at")
  @JsonDeserialize(using = TodoInstantDeserializer.class, as = Instant.class)
  @JsonSerialize(using = NonOptionalInstantSerializer.class)  // TODO: fix deserializer/serializer classes
  private final Instant lastModifiedAt;

  @JsonProperty("due_date")
  @JsonDeserialize(using = TodoInstantDeserializer.class, as = Instant.class)
  @JsonSerialize(using = NonOptionalInstantSerializer.class)
  private final Instant dueDate;

  public Todo(){
    this.id = null;
    this.text = "";
    this.isCompleted = Boolean.FALSE;
    this.createdAt = null;
    this.lastModifiedAt = null;
    this.dueDate = null;
  }

  @JsonCreator
  public Todo(@JsonProperty("id") UUID id,
              @JsonProperty("text") String text,
              @JsonProperty("is_completed") Boolean isCompleted,
              @JsonProperty("created_at") Instant createdAt,
              @JsonProperty("last_modified_at") Instant lastModifiedAt,
              @JsonProperty("due_date") Instant dueDate) {

    this.id = id;
    this.text = text;
    this.isCompleted = isCompleted;
    this.createdAt = createdAt;
    this.lastModifiedAt = lastModifiedAt;
    this.dueDate = dueDate;
  }

  @JsonProperty
  public UUID getId() {
    return id;
  }

  @JsonProperty
  public String getText() {
    return text;
  }

  @JsonProperty("is_completed")
  public Boolean getCompleted() {
    return isCompleted;
  }

  @JsonProperty("created_at")
  public Instant getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("last_modified_at")
  public Instant getLastModifiedAt() {
    return lastModifiedAt;
  }

  @JsonProperty("due_date")
  @JsonSerialize(using = OptionalInstantSerializer.class)
  public Optional<Instant> getDueDate() {
    return Optional.ofNullable(dueDate);
  }
}
