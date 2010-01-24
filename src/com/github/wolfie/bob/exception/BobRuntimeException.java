package com.github.wolfie.bob.exception;

@SuppressWarnings("serial")
public class BobRuntimeException extends RuntimeException {
  public BobRuntimeException() {
    super();
  }
  
  public BobRuntimeException(final String message, final Throwable cause) {
    super(message, cause);
  }
  
  public BobRuntimeException(final String message) {
    super(message);
  }
  
  public BobRuntimeException(final Throwable cause) {
    super(cause);
  }
}
