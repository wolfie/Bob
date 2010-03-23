package com.github.wolfie.bob.action;

import java.io.File;

public interface HasClassDirectory {
  HasClassDirectory setClassDirectory(File classDirectory);
  
  File getClassDirectory();
}
