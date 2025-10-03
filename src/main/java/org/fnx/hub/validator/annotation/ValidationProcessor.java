package org.fnx.hub.validator.annotation;

import com.google.auto.service.AutoService;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Annotation processor that validates correct usage of {@link ValidatePayload} at compile time. It
 * emits an error if the annotation is applied to {@code static}, {@code final} or {@code private}
 * methods.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("org.fnx.hub.validator.annotation.ValidatePayload")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ValidationProcessor extends AbstractProcessor {
  /**
   * Process annotated elements in the current round.
   *
   * @param annotations the set of supported annotations
   * @param roundEnv environment for information about the current and prior round
   * @return {@code true} if annotations are claimed by this processor
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(ValidatePayload.class)) {
      if (element.getKind() == ElementKind.METHOD) {
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.STATIC)
            || modifiers.contains(Modifier.FINAL)
            || modifiers.contains(Modifier.PRIVATE)) {
          processingEnv
              .getMessager()
              .printMessage(
                  Diagnostic.Kind.ERROR,
                  "@ValidatePayload cannot be applied to static, final, or private methods",
                  element);
        }
      }
    }
    return true;
  }
}
