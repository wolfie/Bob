package com.github.wolfie.bob.action;

import java.io.File;

public interface HasSourceDirectory {
  HasSourceDirectory setSourceDirectory(File sourceDirectory);
  
  File getSourceDirectory();
}
