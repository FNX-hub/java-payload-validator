package org.fnx.hub.validator;

public record ValidationError(String message, String path) {

  public static ValidationError withMessage(String message) {
    return new ValidationError(message, null);
  }
}
