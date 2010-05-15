package com.github.wolfie.bob.exception;


public class NoBuildFileFoundException extends BobRuntimeException {
  private static final long serialVersionUID = -7854744946118025041L;
  
  public NoBuildFileFoundException(final String triedFilePath) {
    super(triedFilePath + " was not a valid, readable, build file.");
  }
}
