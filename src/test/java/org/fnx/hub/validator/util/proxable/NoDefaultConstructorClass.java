package org.fnx.hub.validator.util.proxable;

public class NoDefaultConstructorClass {
  private final String name;

  public NoDefaultConstructorClass(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }
}
