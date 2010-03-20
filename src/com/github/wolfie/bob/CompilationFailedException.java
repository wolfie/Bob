package com.github.wolfie.bob;

import com.github.wolfie.bob.exception.BobCheckedException;

public class CompilationFailedException extends BobCheckedException {
  private static final long serialVersionUID = -6346445193807399597L;
  
  public CompilationFailedException(final String string) {
    super(string);
  }
  
  public CompilationFailedException(final Throwable e) {
    super(e);
  }
}
