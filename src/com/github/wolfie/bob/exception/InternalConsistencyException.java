package com.github.wolfie.bob.exception;

public class InternalConsistencyException extends BobRuntimeException {
  private static final long serialVersionUID = -3046280489091621071L;
  
  public InternalConsistencyException(final String string) {
    super(string);
  }
  
}
