package org.fnx.hub.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

/**
 * Adapter utilities to bridge different proxy implementations to a common {@link MethodInterceptor}
 * API. Provides factory methods to create JDK dynamic proxy adapters and ByteBuddy adapters.
 */
interface ProxyAdapter {

  /**
   * Create a JDK-based proxy adapter.
   *
   * @param target the target object to be proxied
   * @param interceptor the interceptor that will handle method invocations
   * @return a {@link JdkProxyAdapter} instance
   */
  static JdkProxyAdapter jdk(Object target, MethodInterceptor interceptor) {
    return new JdkProxyAdapter(target, interceptor);
  }

  /**
   * Create a ByteBuddy-based proxy adapter.
   *
   * @param target the target object to be proxied
   * @param interceptor the interceptor that will handle method invocations
   * @return a {@link ByteBuddyProxyAdapter} instance
   */
  static ByteBuddyProxyAdapter byteBuddy(Object target, MethodInterceptor interceptor) {
    return new ByteBuddyProxyAdapter(target, interceptor);
  }

  /**
   * JDK {@link InvocationHandler} adapter that resolves annotations from cache and delegates to the
   * interceptor.
   *
   * @param target the proxied target object
   * @param interceptor the method interceptor
   */
  record JdkProxyAdapter(Object target, MethodInterceptor interceptor)
      implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Map<Class<? extends Annotation>, List<Annotation>> annotations =
          SingletonCache.INSTANCE.getOrCompute(target.getClass(), method);
      return interceptor.invoke(target, method, annotations, args);
    }
  }

  /** ByteBuddy-compatible adapter that delegates calls to the interceptor. */
  record ByteBuddyProxyAdapter(Object target, MethodInterceptor interceptor) {
    @RuntimeType
    public Object invoke(@Origin Method method, @AllArguments Object[] args) throws Throwable {
      return interceptor.invoke(target, method, args);
    }
  }
}
