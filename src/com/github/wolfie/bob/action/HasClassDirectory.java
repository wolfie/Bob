package com.github.wolfie.bob.action;

import java.io.File;

public interface HasClassDirectory {
  void setClassDirectory(File classDirectory);
  
  File getClassDirectory();
}
