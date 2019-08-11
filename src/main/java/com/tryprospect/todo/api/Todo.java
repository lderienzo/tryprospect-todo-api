package com.tryprospect.todo.api;

import static com.tryprospect.todo.validation.Messages.TODO_VALIDATION_ERROR_TEXT;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

import lombok.Data;


/*

If task marked as completed, and due date has also been changed/is present, remove value for due date.
If task marked as completed, and text has also been changed, save the changed text.

Note to self -- Plan:
Make a class / group ? validator to validate/govern the above isCompleted/dueDate relationship.
If isCompleted == TRUE && dueDate is NotEmpty
Then
  raise constraint violation. Due date can not be populated if is completed marked "true".
  Due date "may" only have a value if is completed is "false".
  Due date can be empty and iscompleted can be "false"
 */
@Data
public final class Todo {

  @NotNull
  private UUID id;

  @NotBlank
  private final String text;

  @NotNull
  private final Boolean isCompleted;

  @NotNull
//  @Past -- Create Custom Annotation to PastOrPrezent
  private final Date createdAt; // TODO: should be LocalDateTime

  @NotNull
//  @Past
  private final Date lastModifiedAt;

  @Future
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
  public Todo(@JsonProperty("id") String id,  // TODO: change to UUID
              @JsonProperty("text") String text,
              @JsonProperty("is_completed") Boolean isCompleted,
              @JsonProperty("created_at") Date createdAt,
              @JsonProperty("last_modified_at") Date lastModifiedAt,
              @JsonProperty("due_date") Date dueDate) {

    if (!Strings.isNullOrEmpty(id))
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
