package org.fnx.hub.validator.proxy;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.stream.Stream;
import org.fnx.hub.proxy.ProxyFactory;
import org.fnx.hub.validator.util.interceptor.FooInterceptor;
import org.fnx.hub.validator.util.proxable.*;
import org.fnx.hub.validator.util.unproxable.EnumClass;
import org.fnx.hub.validator.util.unproxable.FinalClass;
import org.fnx.hub.validator.util.unproxable.RecordClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ProxyFactoryTest {

  static Stream<Arguments> createJdkProxy() {
    return Stream.of(
        Arguments.of(new JdkJsonSchemaService()), Arguments.of(new FooInterfaceImpl()));
  }

  static Stream<Arguments> createByteBuddyProxy() {
    return Stream.of(
        Arguments.of(new ByteBuddyService()),
        Arguments.of(new NoDefaultConstructorClass("Test")),
        Arguments.of(new TestConcreteClass()));
  }

  static Stream<Arguments> createProxy_withFinalClasses() {
    return Stream.of(
        Arguments.of(EnumClass.INSTANCE),
        Arguments.of(new RecordClass("Test")),
        Arguments.of(new FinalClass("Test")));
  }

  @ParameterizedTest
  @MethodSource("createJdkProxy")
  void createProxy_withInterface_returnsJdkProxy(Object target) {
    Object proxy = ProxyFactory.createProxy(target, new FooInterceptor());

    assertNotNull(proxy);
    assertTrue(Proxy.isProxyClass(proxy.getClass()));
  }

  @ParameterizedTest
  @MethodSource("createByteBuddyProxy")
  void createProxy_withConcreteClass_returnsByteBuddyProxy(Object target) {
    var proxy = ProxyFactory.createProxy(target, new FooInterceptor());

    assertNotNull(proxy);
    assertNotEquals(target.getClass(), proxy.getClass());
    assertTrue(target.getClass().isAssignableFrom(proxy.getClass()));
  }

  @Test
  void createProxy_withConcreteClass_checkMethodCall() {
    var target = new NoDefaultConstructorClass("Test");
    var proxy = ProxyFactory.createProxy(target, new FooInterceptor());

    assertNotNull(proxy);
    assertNotEquals(target.getClass(), proxy.getClass());
    assertEquals(target.name(), proxy.name());
  }

  @ParameterizedTest
  @MethodSource("createProxy_withFinalClasses")
  void createProxy_withConcreteFinalClass_returnsSameInstance(Object target) {
    var nonProxy = ProxyFactory.createProxy(target, new FooInterceptor());

    assertNotNull(nonProxy);
    assertSame(target, nonProxy);
  }
}
