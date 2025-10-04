package org.fnx.hub.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * A functional interface representing a method interceptor that can intercept method calls on a
 * proxy object.
 *
 * <p>The {@code invoke} method is called whenever a method on the proxied object is invoked. It
 * receives the target object, the method being called, and the arguments passed to the method. The
 * interceptor can perform additional logic before or after invoking the original method, or it can
 * choose to not invoke the original method at all.
 */
@FunctionalInterface
@SuppressWarnings("java:S112")
public interface MethodInterceptor {
  Object invoke(
      Object target,
      Method method,
      Map<Class<? extends Annotation>, List<Annotation>> annotations,
      Object[] args)
      throws Throwable;

  default Object invoke(Object target, Method method, Object[] args) throws Throwable {
    Map<Class<? extends Annotation>, List<Annotation>> annotations =
        SingletonCache.INSTANCE.getOrCompute(target.getClass(), method);
    return invoke(target, method, annotations, args);
  }
}
