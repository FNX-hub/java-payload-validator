package org.fnx.hub.validator;

import org.fnx.hub.validator.model.ValidationResult;

/**
 * Defines a contract for validating payloads represented as strings.
 *
 * <p>Implementations should validate the provided payload according to a specific schema or rules
 * and return a {@link ValidationResult} describing success or failure and any associated errors.
 */
public interface PayloadValidator {

  /**
   * Validate the given payload string.
   *
   * @param payload the payload to validate, encoded as a {@link String}
   * @return {@link ValidationResult} containing validation outcome and possible errors
   */
  ValidationResult validate(String payload);
}
