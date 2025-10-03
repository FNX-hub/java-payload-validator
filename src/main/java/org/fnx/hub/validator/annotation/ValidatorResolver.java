package org.fnx.hub.validator.annotation;

import org.fnx.hub.validator.PayloadValidator;
import org.fnx.hub.validator.impl.JsonPayloadValidator;
import org.fnx.hub.validator.impl.SchemaType;

/** Resolves a concrete {@link PayloadValidator} implementation based on the provided SchemaType. */
public interface ValidatorResolver {

  /**
   * Create a {@link PayloadValidator} suitable for the given schema type and schema content.
   *
   * @param schemaType the type of the schema (JSON, XSD, YAML)
   * @param schema the schema definition as string
   * @return a {@link PayloadValidator} instance for the requested type
   */
  static PayloadValidator resolve(SchemaType schemaType, String schema) {
    return switch (schemaType) {
      case JSON, YML, XSD -> new JsonPayloadValidator(schema);
    };
  }
}
