package com.github.wolfie.bob.result;

import java.io.File;

import com.github.wolfie.bob.Build;
import com.github.wolfie.bob.Log;
import com.github.wolfie.bob.exception.BuildError;

public class Clear implements Action {
  
  private final File directory;
  
  public Clear(final File directory) {
    this.directory = directory;
  }
  
  @Override
  public void process() {
    final File targetDirectory = directory != null ? directory : Build
        .getDefaultBuildDirectory();
    Log.info("Clearing " + targetDirectory.getName());
    
    if (targetDirectory.exists()) {
      if (!targetDirectory.isDirectory()) {
        throw new BuildError("Target \"" + targetDirectory
            + "\" was found, but is a file, not a directory.");
      } else {
        deleteDirectoriesRecursively(targetDirectory);
      }
    } else {
      Log.fine("Nothing to clear, so nothing was done.");
    }
  }
  
  private static void deleteDirectoriesRecursively(final File directory) {
    for (final File file : directory.listFiles()) {
      // depth first
      if (file.isDirectory()) {
        deleteDirectoriesRecursively(file);
      } else {
        file.delete();
        Log.info("Deleting file " + file.getName());
      }
    }
    Log.info("Deleting directory " + directory.getName());
    directory.delete();
  }
}
