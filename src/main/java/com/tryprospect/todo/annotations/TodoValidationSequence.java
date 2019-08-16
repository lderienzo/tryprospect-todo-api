package com.tryprospect.todo.annotations;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

@GroupSequence({Default.class, ValidateAfterDefaultConstraints.class})
public interface TodoValidationSequence {
}
