package org.fnx.hub.validator.annotation;

import java.lang.annotation.*;
import org.fnx.hub.validator.impl.SchemaType;

/**
 * Annotation used to indicate that a method parameter or payload must be validated against a
 * schema. The annotation targets methods and is inherited by subclasses.
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidatePayload {

  /**
   * The schema content or reference used for validation.
   *
   * @return the schema as a {@link String}
   */
  String schema();

  /**
   * The schema type that determines the validator to use (JSON, XSD, YAML).
   *
   * @return the {@link SchemaType} to apply
   */
  SchemaType type() default SchemaType.JSON;

  /**
   * Indices of method arguments that will be validated (by default the first argument).
   *
   * @return array of argument positions
   */
  int[] args() default {0};
}
