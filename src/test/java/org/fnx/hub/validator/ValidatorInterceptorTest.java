package org.fnx.hub.validator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;
import org.fnx.hub.proxy.MethodInterceptor;
import org.fnx.hub.proxy.ProxyFactory;
import org.fnx.hub.validator.annotation.ValidatorResolver;
import org.fnx.hub.validator.exception.InvalidPayloadException;
import org.fnx.hub.validator.impl.SchemaType;
import org.fnx.hub.validator.model.ValidationError;
import org.fnx.hub.validator.model.ValidationResult;
import org.fnx.hub.validator.util.SchemaService;
import org.fnx.hub.validator.util.interceptor.FooInterceptor;
import org.fnx.hub.validator.util.interceptor.RecordingNextInterceptor;
import org.fnx.hub.validator.util.proxable.ByteBuddyService;
import org.fnx.hub.validator.util.proxable.JdkJsonSchemaService;
import org.fnx.hub.validator.util.proxable.JdkXsdSchemaService;
import org.fnx.hub.validator.util.proxable.JdkYmlSchemaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

class ValidatorInterceptorTest {

  static Stream<Arguments> jdkProxiesSource() {
    return Stream.of(
        Arguments.of(SchemaType.JSON, new JdkJsonSchemaService()),
        Arguments.of(SchemaType.XSD, new JdkXsdSchemaService()),
        Arguments.of(SchemaType.YML, new JdkYmlSchemaService()));
  }

  static Stream<Arguments> annotatedMethodArguments() {
    return Stream.of(
        Arguments.of(
            SchemaType.JSON,
            new JdkJsonSchemaService(),
            "doWork",
            new Class<?>[] {String.class},
            new Object[] {"annotated"}),
        Arguments.of(
            SchemaType.XSD,
            new JdkXsdSchemaService(),
            "doWork",
            new Class<?>[] {String.class},
            new Object[] {"annotated"}),
        Arguments.of(
            SchemaType.YML,
            new JdkYmlSchemaService(),
            "doWork",
            new Class<?>[] {String.class},
            new Object[] {"annotated"}));
  }

  @ParameterizedTest
  @MethodSource("jdkProxiesSource")
  void jdkProxy_appliesInterceptorValidation(SchemaType schemaType, SchemaService real) {

    // build a proxy that uses ValidatorInterceptor then calls the real implementation in "next"
    ValidatorInterceptor interceptor = new ValidatorInterceptor(new FooInterceptor());

    SchemaService proxied = ProxyFactory.createProxy(real, interceptor);

    // mock validation to be valid
    try (MockedStatic<?> vr = mockStatic(ValidatorResolver.class)) {

      PayloadValidator pv = mock(PayloadValidator.class);
      ValidationResult ok = mock(ValidationResult.class);
      when(ok.valid()).thenReturn(true);
      when(pv.validate(anyString())).thenReturn(ok);

      vr.when(() -> ValidatorResolver.resolve(any(SchemaType.class), anyString())).thenReturn(pv);

      String annotated = proxied.doWork("annotated");
      String plain = proxied.plain("not_annotated");

      assertEquals("WORK:annotated", annotated);
      assertEquals("PLAIN:not_annotated", plain);
      verify(pv, times(1)).validate(anyString());
      vr.verify(() -> ValidatorResolver.resolve(eq(schemaType), anyString()), times(1));
    }
  }

  @Test
  void byteBuddyProxy_appliesInterceptorValidation() {
    // create real instance
    ByteBuddyService real = new ByteBuddyService();

    // build a proxy that uses ValidatorInterceptor then calls the real implementation in "next"
    ValidatorInterceptor interceptor = new ValidatorInterceptor(new FooInterceptor());

    ByteBuddyService proxied = ProxyFactory.createProxy(real, interceptor);

    // mock validation to be valid
    try (MockedStatic<?> vr = mockStatic(ValidatorResolver.class)) {

      PayloadValidator pv = mock(PayloadValidator.class);
      ValidationResult ok = mock(ValidationResult.class);
      when(ok.valid()).thenReturn(true);
      when(pv.validate(anyString())).thenReturn(ok);

      vr.when(() -> ValidatorResolver.resolve(any(SchemaType.class), anyString())).thenReturn(pv);

      String annotated = proxied.doWork("annotated");
      String plain = proxied.plain("not_annotated");

      assertEquals("WORK:annotated", annotated);
      assertEquals("PLAIN:not_annotated", plain);
      verify(pv, times(1)).validate(anyString());
      vr.verify(() -> ValidatorResolver.resolve(eq(SchemaType.JSON), anyString()), times(1));
    }
  }

  @Test
  void whenMethodNotAnnotated_shouldCallNextAndSkipValidation() throws Throwable {
    MethodInterceptor next = new RecordingNextInterceptor();
    ValidatorInterceptor interceptor = new ValidatorInterceptor(next);
    Method method = JdkJsonSchemaService.class.getMethod("plain", String.class);
    Object result =
        interceptor.invoke(new JdkJsonSchemaService(), method, new Object[] {"not_annotated"});
    assertEquals("NEXT_OK", result);
    assertNotNull(next.getClass()); // minimal check; next returned sentinel
  }

  @ParameterizedTest
  @MethodSource("annotatedMethodArguments")
  void whenAnnotatedAndValid_shouldCallNext(
      SchemaType schemaType,
      Object instance,
      String methodName,
      Class<?>[] arguments,
      Object[] payloads)
      throws Throwable {
    // mock static ValidatorResolver and SingletonMapper
    try (MockedStatic<?> vr = mockStatic(ValidatorResolver.class)) {

      // prepare mocks
      PayloadValidator pv = mock(PayloadValidator.class);
      ValidationResult ok = mock(ValidationResult.class);
      when(ok.valid()).thenReturn(true);
      when(pv.validate(anyString())).thenReturn(ok);

      // static resolve and serialize
      vr.when(() -> ValidatorResolver.resolve(eq(schemaType), anyString())).thenReturn(pv);

      // invoke
      Method method = instance.getClass().getMethod(methodName, arguments);
      ValidatorInterceptor interceptor = new ValidatorInterceptor(new RecordingNextInterceptor());
      Object result = interceptor.invoke(instance, method, payloads);

      assertEquals("NEXT_OK", result);
      verify(pv, times(1)).validate(anyString());
    }
  }

  @ParameterizedTest
  @MethodSource("annotatedMethodArguments")
  void whenAnnotatedAndInvalid_shouldThrowInvalidPayloadException(
      SchemaType schemaType,
      Object instance,
      String methodName,
      Class<?>[] arguments,
      Object[] payloads)
      throws Throwable {
    try (MockedStatic<?> vr = mockStatic(ValidatorResolver.class)) {

      // prepare mocks
      PayloadValidator pv = mock(PayloadValidator.class);
      ValidationResult fail = mock(ValidationResult.class);
      when(fail.valid()).thenReturn(false);
      when(fail.errors())
          .thenReturn(
              List.of(new ValidationError("err1", "loc1"), new ValidationError("err2", null)));
      when(pv.validate(anyString())).thenReturn(fail);

      // static resolve and serialize
      vr.when(() -> ValidatorResolver.resolve(eq(schemaType), anyString())).thenReturn(pv);

      // invoke
      Method method = instance.getClass().getMethod(methodName, arguments);
      ValidatorInterceptor interceptor = new ValidatorInterceptor(new RecordingNextInterceptor());
      InvalidPayloadException ex =
          assertThrows(
              InvalidPayloadException.class, () -> interceptor.invoke(instance, method, payloads));
      assertTrue(ex.getMessage().contains("err1"));
      assertTrue(ex.getMessage().contains("loc1"));
      assertTrue(ex.getMessage().contains("err2"));
      verify(pv, times(1)).validate(anyString());
    }
  }
}
