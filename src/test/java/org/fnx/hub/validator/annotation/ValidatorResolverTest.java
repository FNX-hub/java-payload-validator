package org.fnx.hub.validator.annotation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.fnx.hub.validator.PayloadValidator;
import org.fnx.hub.validator.impl.JsonPayloadValidator;
import org.fnx.hub.validator.impl.SchemaType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValidatorResolverTest {

  static Stream<Arguments> types() {
    return Stream.of(Arguments.of(SchemaType.JSON, "{}", JsonPayloadValidator.class));
  }

  @ParameterizedTest
  @MethodSource("types")
  void should_resolve_validator(SchemaType schemaType, String schema, Class<?> expectedClass) {
    PayloadValidator validator = ValidatorResolver.resolve(schemaType, schema);
    assertNotNull(validator);
    assertEquals(expectedClass, validator.getClass());
  }
}
