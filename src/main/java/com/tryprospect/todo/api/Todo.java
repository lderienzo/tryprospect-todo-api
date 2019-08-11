package com.tryprospect.todo.api;

import static com.tryprospect.todo.validation.Messages.TODO_VALIDATION_ERROR_TEXT;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;




@Data
public final class Todo {

  // TODO: Add validations
  private UUID id;
  @NotBlank(message = TODO_VALIDATION_ERROR_TEXT)
  private final String text;
  private final Boolean isCompleted;
  private final Date createdAt; // TODO: should be LocalDateTime
  private final Date lastModifiedAt;
  private final Date dueDate;

  public Todo(){
    this.id = null;
    this.text = "";
    this.isCompleted = Boolean.FALSE;
    this.createdAt = null;
    this.lastModifiedAt = null;
    this.dueDate = null;
  }

  @JsonCreator
  public Todo(@JsonProperty("id") String id,
              @JsonProperty("text") String text,
              @JsonProperty("is_completed") Boolean isCompleted,
              @JsonProperty("created_at") Date createdAt,
              @JsonProperty("last_modified_at") Date lastModifiedAt,
              @JsonProperty("due_date") Date dueDate) {

    this.id = UUID.fromString(id);
    this.text = text;
    this.isCompleted = isCompleted;
    this.createdAt = createdAt;
    this.lastModifiedAt = lastModifiedAt;
    this.dueDate = dueDate;

//    this.createdAt = convertToLocalDateTime(createdAt);
//    this.lastModifiedAt = convertToLocalDateTime(lastModifiedAt);
  }

  private LocalDateTime convertToLocalDateTime(Date dateToConvert) {
    return convertViaMilliseconds(dateToConvert);
  }

  private LocalDateTime convertViaMilliseconds(Date dateToConvert) {
    return Instant.ofEpochMilli(dateToConvert.getTime())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
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
  public Date getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("last_modified_at")
  public Date getLastModifiedAt() {
    return lastModifiedAt;
  }


  @JsonProperty("due_date")
  public Date getDueDate() {
    return dueDate;
  }
}
