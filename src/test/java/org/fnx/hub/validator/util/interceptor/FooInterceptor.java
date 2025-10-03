package org.fnx.hub.validator.util.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.fnx.hub.proxy.MethodInterceptor;

public record FooInterceptor() implements MethodInterceptor {

  @Override
  public Object invoke(
      Object target,
      Method method,
      Map<Class<? extends Annotation>, List<Annotation>> annotations,
      Object[] args)
      throws Throwable {
    return invoke(target, method, args);
  }

  @Override
  public Object invoke(Object target, Method method, Object[] args) throws Throwable {
    return method.invoke(target, args);
  }
}
