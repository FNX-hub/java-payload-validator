package org.fnx.hub.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.fnx.hub.mapper.SingletonMapper;
import org.fnx.hub.proxy.MethodInterceptor;
import org.fnx.hub.validator.annotation.ValidatePayload;
import org.fnx.hub.validator.annotation.ValidatorResolver;
import org.fnx.hub.validator.exception.InvalidPayloadException;
import org.fnx.hub.validator.impl.SchemaType;
import org.fnx.hub.validator.model.ValidationResult;

/**
 * Interceptor that applies payload validation before delegating to the next {@link
 * MethodInterceptor}.
 *
 * <p>The interceptor looks for the {@link ValidatePayload} annotation among resolved annotations,
 * extracts schema information, serializes the targeted method arguments and runs the resolved
 * {@link PayloadValidator}. If validation fails an {@link InvalidPayloadException} is thrown.
 *
 * @param next the next {@link MethodInterceptor} in the chain to invoke when validation passes
 */
public record ValidatorInterceptor(MethodInterceptor next) implements MethodInterceptor {

  /**
   * Intercept a method invocation, perform payload validation if a {@link ValidatePayload}
   * annotation is present, and delegate to the next interceptor.
   *
   * <p>If validation fails this method throws {@link InvalidPayloadException}; otherwise it returns
   * the value from the delegated invocation.
   *
   * @param target the target object being invoked
   * @param method the method being invoked
   * @param annotations resolved annotations grouped by annotation type for the invoked method
   * @param args the arguments passed to the method invocation
   * @return the result of the delegated invocation when validation succeeds
   * @throws Throwable if the delegated invocation throws or validation processing fails
   */
  @Override
  public Object invoke(
      Object target,
      Method method,
      Map<Class<? extends Annotation>, List<Annotation>> annotations,
      Object[] args)
      throws Throwable {
    List<Annotation> validatePayloads = annotations.get(ValidatePayload.class);
    if (Objects.nonNull(validatePayloads) && !validatePayloads.isEmpty()) {
      ValidatePayload annotation = (ValidatePayload) validatePayloads.getLast();
      SchemaType schemaType = annotation.type();
      PayloadValidator validator = ValidatorResolver.resolve(schemaType, annotation.schema());
      int[] argPositions = annotation.args();

      Object[] argsToValidate = new Object[argPositions.length];

      for (int position : argPositions) {
        argsToValidate[position] = args[position];
      }

      String payload = SingletonMapper.serialize(schemaType, argsToValidate);
      ValidationResult result = validator.validate(payload);

      if (!result.valid()) {
        throw new InvalidPayloadException(result.errors());
      }
    }
    return next.invoke(target, method, args);
  }
}
