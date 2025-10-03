package org.fnx.hub.validator.model;

/**
 * Represents a single validation error with an error message and an optional JSON path.
 *
 * @param message the error message
 * @param path the path within the payload where the error occurred, or {@code null} if not
 *     applicable
 */
public record ValidationError(String message, String path) {

  /**
   * Create a {@link ValidationError} with only a message (no path).
   *
   * @param message the error message
   * @return a {@link ValidationError} instance with {@code null} path
   */
  public static ValidationError withMessage(String message) {
    return new ValidationError(message, null);
  }
}
