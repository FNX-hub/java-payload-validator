package org.fnx.hub.proxy;

import org.objenesis.ObjenesisStd;

/** Singleton enum holding an {@link ObjenesisStd} instance for unsafe object instantiation. */
enum SingletonObjenesis {
  /**
   * Single enum instance containing the {@link ObjenesisStd} used to instantiate objects without
   * invoking constructors.
   */
  INSTANCE(new ObjenesisStd());

  /** The ObjenesisStd instance used for proxy instantiation when constructors cannot be invoked. */
  final ObjenesisStd objenesisStd;

  /**
   * Create the enum instance with the provided {@link ObjenesisStd}.
   *
   * @param objenesisStd the {@link ObjenesisStd} instance to hold
   */
  SingletonObjenesis(ObjenesisStd objenesisStd) {
    this.objenesisStd = objenesisStd;
  }
}
