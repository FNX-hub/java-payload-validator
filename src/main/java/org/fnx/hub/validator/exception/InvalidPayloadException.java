package org.fnx.hub.validator.exception;

import java.util.List;
import org.fnx.hub.validator.model.ValidationError;

/**
 * Exception thrown when payload validation fails. The exception message is constructed from the
 * collection of {@link ValidationError} instances.
 */
public class InvalidPayloadException extends RuntimeException {

  /**
   * Create an {@link InvalidPayloadException} with a list of validation errors.
   *
   * @param errors the validation errors that caused the exception
   */
  public InvalidPayloadException(List<ValidationError> errors) {
    super(buildMessage(errors));
  }

  private static String buildMessage(List<ValidationError> errors) {
    StringBuilder builder = new StringBuilder();

    errors.forEach(
        error -> {
          builder.append(error.message());
          if (error.path() != null) {
            builder.append(" at ").append(error.path());
          }
          builder.append("; ");
        });

    return builder.toString();
  }
}
