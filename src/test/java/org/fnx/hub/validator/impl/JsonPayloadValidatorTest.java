package org.fnx.hub.validator.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonParseException;
import java.util.List;
import java.util.stream.Stream;
import org.fnx.hub.validator.ValidationError;
import org.fnx.hub.validator.ValidationResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JsonPayloadValidatorTest {

  private static String readResource(String path) {
    try (var is = JsonPayloadValidatorTest.class.getResourceAsStream(path)) {
      if (is == null) throw new IllegalArgumentException("Resource not found: " + path);
      return new String(is.readAllBytes());
    } catch (Exception e) {
      throw new RuntimeException("Failed to read resource: " + path, e);
    }
  }

  static Stream<Arguments> validJsonPayload() {
    return Stream.of(
        Arguments.of(readResource("/payloads/valid/empty.json")),
        Arguments.of(readResource("/payloads/valid/example.json")),
        Arguments.of(readResource("/payloads/valid/array_empty_object.json")),
        Arguments.of(readResource("/payloads/valid/empty_object.json")),
        Arguments.of(readResource("/payloads/valid/empty_array.json")));
  }

  static Stream<Arguments> invalidJsonPayload() {
    return Stream.of(
        Arguments.of(readResource("/payloads/invalid/invalid_head.json")),
        Arguments.of(readResource("/payloads/invalid/invalid_tail.json")));
  }

  static Stream<Arguments> validPayloadForSchemas() {
    return Stream.of(
        Arguments.of(
            readResource("/schemas/full-schema.json"),
            readResource("/payloads/valid/example.json")),
        Arguments.of(
            readResource("/schemas/pattern-schema.json"),
            readResource("/payloads/valid/example.json")));
  }

  static Stream<Arguments> invalidPayloadForSchemas() {
    return Stream.of(
        Arguments.of(
            readResource("/schemas/restricted-schema.json"),
            readResource("/payloads/valid/example.json")),
        Arguments.of(
            readResource("/schemas/pattern-schema_2.json"),
            readResource("/payloads/valid/example.json")));
  }

  static Stream<Arguments> invalidJsonPayloadForSchemas() {
    return Stream.of(
        Arguments.of(
            readResource("/schemas/full-schema.json"),
            readResource("/payloads/invalid/invalid_head.json")),
        Arguments.of(
            readResource("/schemas/full-schema.json"),
            readResource("/payloads/invalid/invalid_tail.json")));
  }

  @ParameterizedTest
  @MethodSource("validJsonPayload")
  void testIsStrictJsonWithValidJson(String json) {
    assertDoesNotThrow(() -> JsonPayloadValidator.isStrictJson(json));
  }

  @ParameterizedTest
  @MethodSource("invalidJsonPayload")
  void testIsStrictJsonWithInvalidJson(String json) {
    assertThrows(JsonParseException.class, () -> JsonPayloadValidator.isStrictJson(json));
  }

  @ParameterizedTest
  @MethodSource("validPayloadForSchemas")
  void testValidateWithValidPayload(String schema, String payload) {
    JsonPayloadValidator validator = new JsonPayloadValidator(schema);
    ValidationResult result = validator.validate(payload);

    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
  }

  @ParameterizedTest
  @MethodSource("invalidPayloadForSchemas")
  void testValidateWithInvalidPayload(String schema, String payload) {
    JsonPayloadValidator validator = new JsonPayloadValidator(schema);
    ValidationResult result = validator.validate(payload);

    assertFalse(result.valid());
    List<ValidationError> errors = result.errors();
    assertFalse(errors.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("invalidJsonPayloadForSchemas")
  void testValidateWithInvalidJson(String schema, String payload) {
    JsonPayloadValidator validator = new JsonPayloadValidator(schema);
    ValidationResult result = validator.validate(payload);

    assertFalse(result.valid());
    List<ValidationError> errors = result.errors();
    assertEquals(1, errors.size());
  }
}
