package org.fnx.hub.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache used to resolve and store annotations for methods of a specific target class. Uses a {@link
 * WeakHashMap} keyed by Class to avoid classloader-related memory leaks.
 */
public enum SingletonCache {
  INSTANCE(Collections.synchronizedMap(new WeakHashMap<>()));

  private final Map<
          Class<?>, Map<MethodSignature, Map<Class<? extends Annotation>, List<Annotation>>>>
      cache;

  SingletonCache(
      Map<Class<?>, Map<MethodSignature, Map<Class<? extends Annotation>, List<Annotation>>>>
          cache) {
    this.cache = cache;
  }

  /**
   * Resolve annotations applying precedence: interface method, concrete implementation method,
   * class-level annotations.
   *
   * @param interfaceMethod the method declared in the interface
   * @param targetClass target class where implementation might be declared
   * @return a map grouping annotations by their annotation type
   */
  private static Map<Class<? extends Annotation>, List<Annotation>> resolveAnnotations(
      Method interfaceMethod, Class<?> targetClass) {
    Map<Class<? extends Annotation>, List<Annotation>> result = new HashMap<>();
    collectAnnotations(interfaceMethod.getAnnotations(), result);

    Method concrete = findConcreteMethod(interfaceMethod, targetClass);
    if (concrete != null) collectAnnotations(concrete.getAnnotations(), result);

    collectAnnotations(Objects.requireNonNull(targetClass).getAnnotations(), result);
    return result;
  }

  /**
   * Collect annotations into the provided result map grouping by annotation type.
   *
   * @param annotations an array of annotations to collect
   * @param result the map that accumulates annotations
   */
  private static void collectAnnotations(
      Annotation[] annotations, Map<Class<? extends Annotation>, List<Annotation>> result) {
    for (Annotation ann : annotations) {
      result.computeIfAbsent(ann.annotationType(), k -> new ArrayList<>()).add(ann);
    }
  }

  /**
   * Find a concrete method in the target class hierarchy matching the interface method signature.
   *
   * @param interfaceMethod the interface method to match
   * @param targetClass the class where to search for an implementation
   * @return the concrete Method if found, otherwise null
   */
  private static Method findConcreteMethod(Method interfaceMethod, Class<?> targetClass) {
    Class<?> current = targetClass;
    while (current != null) {
      try {
        return current.getDeclaredMethod(
            interfaceMethod.getName(), interfaceMethod.getParameterTypes());
      } catch (NoSuchMethodException e) {
        current = current.getSuperclass();
      }
    }
    return null;
  }

  /**
   * Return cached annotations for a given target class and method, computing them if absent.
   *
   * @param targetClass the class whose method annotations are requested
   * @param method the method to inspect
   * @return a map from annotation type to list of annotations
   */
  public Map<Class<? extends Annotation>, List<Annotation>> getOrCompute(
      Class<?> targetClass, Method method) {
    return cache
        .computeIfAbsent(targetClass, k -> new ConcurrentHashMap<>())
        .computeIfAbsent(new MethodSignature(method), k -> resolveAnnotations(method, targetClass));
  }

  /** Lightweight method signature used as cache key. */
  private record MethodSignature(String name, List<Class<?>> parameterTypes) {
    public MethodSignature(Method method) {
      this(method.getName(), Arrays.asList(method.getParameterTypes()));
    }
  }
}
