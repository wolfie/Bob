package com.github.wolfie.bob.exception;

public class UnrecognizedArgumentException extends BobRuntimeException {
  private static final long serialVersionUID = 4239683616261713767L;
  private final String arg;
  
  public UnrecognizedArgumentException(final String arg) {
    super("Unrecognized argument: " + arg);
    this.arg = arg;
  }
  
  public String getArgument() {
    return arg;
  }
}
