package org.fnx.hub.validator.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashSet;
import java.util.Set;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.fnx.hub.mapper.SingletonMapper;
import org.fnx.hub.validator.PayloadValidator;
import org.fnx.hub.validator.model.ValidationError;
import org.fnx.hub.validator.model.ValidationResult;
import org.json.JSONObject;
import org.json.JSONTokener;

/** Payload validator that validates JSON payloads against a provided JSON Schema using Everit. */
public class JsonPayloadValidator implements PayloadValidator {
  private final Schema schema;

  /**
   * Construct a {@link JsonPayloadValidator} from a JSON Schema string.
   *
   * @param jsonSchema the JSON Schema definition as a string
   */
  public JsonPayloadValidator(String jsonSchema) {
    JSONObject rawSchema = new JSONObject(new JSONTokener(jsonSchema));
    this.schema = SchemaLoader.load(rawSchema);
  }

  /**
   * Ensure the provided payload is valid JSON by attempting to parse it with Jackson.
   *
   * @param json the input payload
   * @throws JsonProcessingException if the payload is not strict JSON
   */
  static void isStrictJson(String json) throws JsonProcessingException {
    SingletonMapper.getMapper(SchemaType.JSON).readTree(json);
  }

  /**
   * Validate the given payload string against the configured schema.
   *
   * @param payload the payload to validate
   * @return {@link ValidationResult} – ValidationResult.success() if valid,
   *     ValidationResult.failure(...) otherwise
   */
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

  /**
   * Convert Everit ValidationException into ValidationResult containing ValidationError instances.
   *
   * @param ex the ValidationException thrown by the schema validator
   * @return a failed ValidationResult with detailed errors
   */
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
