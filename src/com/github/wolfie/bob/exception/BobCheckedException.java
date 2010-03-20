package com.github.wolfie.bob.exception;

public class BobCheckedException extends Exception {
  private static final long serialVersionUID = 3133462660067215252L;
  
  public BobCheckedException() {
    super();
  }
  
  public BobCheckedException(final String message, final Throwable cause) {
    super(message, cause);
  }
  
  public BobCheckedException(final String message) {
    super(message);
  }
  
  public BobCheckedException(final Throwable cause) {
    super(cause);
  }
}
