package org.fnx.hub.proxy.exception;

/**
 * Runtime exception thrown when a proxy cannot be instantiated.
 *
 * <p>This exception indicates a failure during the creation or instantiation of a proxy class, for
 * example when using ByteBuddy, Objenesis or reflection-based instantiation fails.
 */
public class ProxyInstantiationException extends RuntimeException {

  /**
   * Constructs a new {@code ProxyInstantiationException} with the specified detail message.
   *
   * @param message the detail message explaining the instantiation failure
   */
  public ProxyInstantiationException(String message) {
    super(message);
  }
}
