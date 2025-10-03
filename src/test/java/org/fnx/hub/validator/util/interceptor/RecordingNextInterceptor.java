package org.fnx.hub.validator.util.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.fnx.hub.proxy.MethodInterceptor;

public class RecordingNextInterceptor implements MethodInterceptor {

  boolean invoked = false;

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
    invoked = true;
    return "NEXT_OK";
  }
}
