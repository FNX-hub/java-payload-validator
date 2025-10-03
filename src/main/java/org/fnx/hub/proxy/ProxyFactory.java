package org.fnx.hub.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fnx.hub.proxy.exception.ProxyInstantiationException;

/**
 * Factory utilities to create validation proxies for target objects.
 *
 * <p>This interface provides static helper methods to create either JDK dynamic proxies (when the
 * target implements interfaces) or ByteBuddy-based subclass proxies for concrete classes. The
 * created proxies delegate method calls to ValidatorProxyInterceptor to perform parameter and
 * return-value validation logic.
 */
public interface ProxyFactory {

  /**
   * Creates a proxy for the given target object. If the target implements interfaces, a JDK dynamic
   * proxy is used. Otherwise, a {@link ByteBuddy} proxy is created for concrete classes. If proxy
   * creation fails, the original target is returned.
   *
   * @param target the target object to proxy
   * @param <T> the type of the target object
   * @return a proxy instance or the original target if proxy creation fails
   */
  @SuppressWarnings("unchecked")
  static <T> T createProxy(T target, MethodInterceptor interceptor) {
    Class<? extends T> targetClass = (Class<? extends T>) target.getClass();

    if (targetClass.getInterfaces().length > 0) {
      return (T) createJdkProxy(target, targetClass, interceptor);
    }
    try {
      return (T) createByteBuddyProxy(target, targetClass, interceptor);
    } catch (Exception e) {
      Log log = LogFactory.getLog(ProxyFactory.class);
      log.warn("Proxy creation failed", e);
      return target;
    }
  }

  /**
   * Creates a JDK dynamic proxy for the given target object. The proxy will implement all
   * interfaces of the target class.
   *
   * @param target the target object
   * @param targetClass the class of the target object
   * @param <T> the type of the target object
   * @return a JDK proxy instance
   */
  private static <T> Object createJdkProxy(
      T target, Class<? extends T> targetClass, MethodInterceptor interceptor) {
    return Proxy.newProxyInstance(
        targetClass.getClassLoader(),
        targetClass.getInterfaces(),
        ProxyAdapter.jdk(target, interceptor));
  }

  /**
   * Creates a ByteBuddy proxy for the given target object. Throws {@link
   * ProxyInstantiationException} if the target class is final.
   *
   * @param target the target object
   * @param targetClass the class of the target object
   * @param <T> the type of the target object
   * @return a ByteBuddy proxy instance
   * @throws ProxyInstantiationException if the target class is final
   */
  private static <T> Object createByteBuddyProxy(
      T target, Class<? extends T> targetClass, MethodInterceptor interceptor) {
    if (Modifier.isFinal(targetClass.getModifiers())) {
      throw new ProxyInstantiationException(
          "Cannot create proxy for final class: %s".formatted(targetClass.getName()));
    }
    return getProxyInstance(target, targetClass, ProxyFactory::useDefaultConstructor, interceptor);
  }

  /**
   * Instantiates a proxy instance using the provided instantiator function. The proxy is created as
   * a subclass of the target class and intercepts all public methods.
   *
   * @param target the target object
   * @param targetClass the class of the target object
   * @param instantiator function to instantiate the proxy
   * @param <T> the type of the target object
   * @return a proxy instance
   */
  private static <T> T getProxyInstance(
      T target,
      Class<? extends T> targetClass,
      Function<Class<? extends T>, T> instantiator,
      MethodInterceptor interceptor) {
    try (var loaded =
        new ByteBuddy()
            // create a subclass of the target class
            .subclass(targetClass)
            // intercept all public methods to add validation logic
            .method(ElementMatchers.isPublic())
            .intercept(MethodDelegation.to(ProxyAdapter.byteBuddy(target, interceptor)))
            .make()) {

      Class<? extends T> proxyClass = loaded.load(targetClass.getClassLoader()).getLoaded();

      return instantiator.apply(proxyClass);
    }
  }

  /**
   * Instantiates a proxy using its default constructor. If instantiation fails, falls back to
   * Objenesis for unsafe instantiation.
   *
   * @param proxy the proxy class
   * @param <T> the type of the proxy
   * @return a proxy instance
   */
  private static <T> T useDefaultConstructor(Class<? extends T> proxy) {
    try {
      return proxy.getDeclaredConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      return SingletonObjenesis.INSTANCE.objenesisStd.newInstance(proxy);
    }
  }
}
