package org.fnx.hub.validator;

import java.util.Collections;
import java.util.List;

public record ValidationResult(boolean valid, List<ValidationError> errors) {

  public static ValidationResult success() {
    return new ValidationResult(true, Collections.emptyList());
  }

  public static ValidationResult failure(ValidationError... errors) {
    return ValidationResult.failure(List.of(errors));
  }

  public static ValidationResult failure(List<ValidationError> errors) {
    return new ValidationResult(false, errors);
  }
}
