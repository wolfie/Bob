package com.github.wolfie.bob.exception;

import java.io.File;

public class NotADirectoryOrCouldNotReadException extends Exception {
  private static final long serialVersionUID = -6685364875302800650L;
  
  public NotADirectoryOrCouldNotReadException(final File baseDir) {
    super(baseDir.getAbsolutePath());
  }
}
