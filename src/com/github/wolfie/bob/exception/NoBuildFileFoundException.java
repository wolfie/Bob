package com.github.wolfie.bob.exception;

import java.io.File;

public class NoBuildFileFoundException extends BobRuntimeException {
  private static final long serialVersionUID = -7854744946118025041L;
  
  public NoBuildFileFoundException(final File buildDirectory) {
    super("No build file found under " + buildDirectory);
  }
}
