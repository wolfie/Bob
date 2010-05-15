package com.github.wolfie.bob.exception;

public class BuildTargetException extends BobRuntimeException {
  private static final long serialVersionUID = 3740703967800281514L;
  
  public BuildTargetException(final String string, final Throwable e) {
    super(string, e);
  }
  
  public BuildTargetException(final String message) {
    super(message);
  }
}
