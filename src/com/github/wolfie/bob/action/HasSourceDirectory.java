package com.github.wolfie.bob.action;

import java.io.File;

public interface HasSourceDirectory {
  void setSourceDirectory(File sourceDirectory);
  
  File getSourceDirectory();
}
