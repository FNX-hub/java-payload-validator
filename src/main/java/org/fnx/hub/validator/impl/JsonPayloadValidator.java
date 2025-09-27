package org.fnx.hub.validator.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.HashSet;
import java.util.Set;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.fnx.hub.validator.PayloadValidator;
import org.fnx.hub.validator.ValidationError;
import org.fnx.hub.validator.ValidationResult;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonPayloadValidator implements PayloadValidator {

  private static final JsonMapper mapper = new JsonMapper();
  private final Schema schema;

  public JsonPayloadValidator(String jsonSchema) {
    JSONObject rawSchema = new JSONObject(new JSONTokener(jsonSchema));
    this.schema = SchemaLoader.load(rawSchema);
  }

  public static void isStrictJson(String json) throws JsonProcessingException {
    mapper.readTree(json);
  }

  @Override
  public ValidationResult validate(String payload) {
    try {
      isStrictJson(payload);
      JSONObject jsonPayload = new JSONObject(new JSONTokener(payload));
      schema.validate(jsonPayload);
      return ValidationResult.success();
    } catch (ValidationException e) {
      return fromValidationException(e);
    } catch (JsonProcessingException e) {
      return ValidationResult.failure(ValidationError.withMessage(e.getMessage()));
    }
  }

  private ValidationResult fromValidationException(ValidationException ex) {
    Set<ValidationError> errors = new HashSet<>();
    String pointer;
    if (!ex.getCausingExceptions().isEmpty()) {
      for (ValidationException child : ex.getCausingExceptions()) {
        pointer = child.getPointerToViolation();
        errors.add(new ValidationError(child.getMessage(), pointer));
      }
    }
    pointer = ex.getPointerToViolation();
    errors.add(new ValidationError(ex.getMessage(), pointer));
    return ValidationResult.failure(errors.toArray(new ValidationError[0]));
  }
}
