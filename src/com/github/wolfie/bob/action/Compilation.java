package com.github.wolfie.bob.action;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.github.wolfie.bob.CompilationCache;
import com.github.wolfie.bob.CompilationCache.NotCompiledException;
import com.github.wolfie.bob.Defaults;
import com.github.wolfie.bob.Util;
import com.github.wolfie.bob.exception.ProcessingError;

public class Compilation implements Action {
  
  private String srcPath = null;
  private String toPath = null;
  private boolean disableDebug = false;
  
  @Override
  public void process() {
    setDefaults();
    
    final File destination = getDestinationDir();
    
    if (!disableDebug) {
      try {
        final Set<File> classFiles = CompilationCache.get()
            .getClassFiles(srcPath);
        final File baseDir = CompilationCache.get().getBaseDirForClasses();
        Util.copy(classFiles, baseDir, destination);
      } catch (final NotCompiledException e) {
        throw new ProcessingError(e);
      } catch (final IOException e) {
        throw new ProcessingError(e);
      }
    } else {
      // TODO recompile without debug
      throw new UnsupportedOperationException(
          "Recompilation for disabling debug isn't supported yet");
    }
  }
  
  /**
   * Define the source path to compile.
   * <p/>
   * If the source path isn't included in the build's
   * {@link com.github.wolfie.bob.ProjectDescription}, the build will fail in a
   * {@link com.github.wolfie.bob.CompilationCache.NotCompiledException}.
   */
  public Compilation from(final String sourcePath) {
    srcPath = sourcePath;
    return this;
  }
  
  private File getDestinationDir() {
    final File destination = new File(toPath);
    
    if (!destination.exists()) {
      final boolean success = destination.mkdirs();
      if (!success) {
        throw new ProcessingError(toPath + " could not be created");
      }
    }
    
    if (destination.isDirectory()
        && destination.canWrite()) {
      return destination;
    } else {
      throw new ProcessingError(toPath
          + " is not a writeable, existing directory");
    }
  }
  
  public Compilation disableDebug() {
    disableDebug = true;
    return this;
  }
  
  public Compilation to(final String path) {
    toPath = path;
    return this;
  }
  
  private void setDefaults() {
    if (srcPath == null) {
      srcPath = Defaults.SOURCE_PATH;
    }
    
    if (toPath == null) {
      toPath = Defaults.ARTIFACTS_PATH;
    }
  }
  
  File getSourceDirectory() {
    return Util.checkedDirectory(new File(srcPath));
  }
}
