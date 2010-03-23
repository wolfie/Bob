package com.github.wolfie.bob.exception;

public class NoBuildDirectoryFoundException extends BobRuntimeException {
  private static final long serialVersionUID = 7567683483528790738L;
  
  public NoBuildDirectoryFoundException() {
    super("No build directory was found");
  }
}
