package com.github.wolfie.bob.exception;


public class CompilationFailedException extends BobRuntimeException {
  private static final long serialVersionUID = -6346445193807399597L;
  
  public CompilationFailedException(final String string) {
    super(string);
  }
  
  public CompilationFailedException(final Throwable e) {
    super(e);
  }
  
  public CompilationFailedException(final String msg, final Throwable cause) {
    super(msg, cause);
  }
}
