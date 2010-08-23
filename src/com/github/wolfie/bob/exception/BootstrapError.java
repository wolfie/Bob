package com.github.wolfie.bob.exception;

public class BootstrapError extends Error {
  private static final long serialVersionUID = 4028361969617154737L;
  
  public BootstrapError(final Throwable cause) {
    super(cause);
  }
  
  public BootstrapError(final String msg, final Throwable cause) {
    super(msg, cause);
  }
  
  public BootstrapError(final String msg) {
    super(msg);
  }
}
