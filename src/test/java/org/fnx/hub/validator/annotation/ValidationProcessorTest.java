package org.fnx.hub.validator.annotation;

import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

class ValidationProcessorTest {

  @Test
  void validatePayloadAnnotation_onInvalidMethods_generatesError() {
    JavaFileObject invalidClass =
        JavaFileObjects.forSourceLines(
            "org.fnx.hub.validator.InvalidClass",
            "package org.fnx.hub.validator;",
            "import org.fnx.hub.validator.annotation.ValidatePayload;",
            "public class InvalidClass {",
            "  @ValidatePayload(schema = \"example\")",
            "  private void privateMethod() {}",
            "",
            "  @ValidatePayload(schema = \"example\")",
            "  public static void staticMethod() {}",
            "",
            "  @ValidatePayload(schema = \"example\")",
            "  public final void finalMethod() {}",
            "}");

    Compilation compilation =
        javac().withProcessors(new ValidationProcessor()).compile(invalidClass);

    assertThat(compilation.errors()).hasSize(3);

    compilation
        .errors()
        .forEach(
            error ->
                assertThat(error.getMessage(null))
                    .contains(
                        "@ValidatePayload cannot be applied to static, final, or private methods"));
  }

  @Test
  void validatePayloadAnnotation_onValidMethod_compilesSuccessfully() {
    JavaFileObject validClass =
        JavaFileObjects.forSourceLines(
            "org.fnx.hub.validator.ValidClass",
            "package org.fnx.hub.validator;",
            "import org.fnx.hub.validator.annotation.ValidatePayload;",
            "public class ValidClass {",
            "  @ValidatePayload(schema = \"example\")",
            "  public void validMethod() {}",
            "}");

    Compilation compilation = javac().withProcessors(new ValidationProcessor()).compile(validClass);

    assertThat(compilation.errors()).isEmpty();
    assertEquals(Compilation.Status.SUCCESS, compilation.status());
  }
}
