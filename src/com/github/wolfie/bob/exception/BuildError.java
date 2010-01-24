package com.github.wolfie.bob.exception;

@SuppressWarnings("serial")
public class BuildError extends Error {
  public BuildError() {
    super();
  }
  
  public BuildError(final String message, final Throwable cause) {
    super(message, cause);
  }
  
  public BuildError(final String message) {
    super(message);
  }
  
  public BuildError(final Throwable cause) {
    super(cause);
  }
}
