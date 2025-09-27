package org.fnx.hub.validator;

public interface PayloadValidator {

  ValidationResult validate(String payload);
}
