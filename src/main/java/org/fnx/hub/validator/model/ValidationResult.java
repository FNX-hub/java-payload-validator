package org.fnx.hub.validator.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents the outcome of a payload validation operation.
 *
 * <p>The record contains a boolean flag indicating if validation succeeded and a list of validation
 * errors when validation failed.
 *
 * @param valid true if the payload is valid, false otherwise
 * @param errors list of validation errors (empty when valid is true)
 */
public record ValidationResult(boolean valid, List<ValidationError> errors) {

  /**
   * Create a successful {@link ValidationResult} with no errors.
   *
   * @return a {@link ValidationResult} representing success
   */
  public static ValidationResult success() {
    return new ValidationResult(true, Collections.emptyList());
  }

  /**
   * Create a failed {@link ValidationResult} containing the provided errors.
   *
   * @param errors one or more ValidationError instances describing the validation failures
   * @return a {@link ValidationResult} representing failure
   */
  public static ValidationResult failure(ValidationError... errors) {
    return new ValidationResult(false, Arrays.asList(errors));
  }
}
