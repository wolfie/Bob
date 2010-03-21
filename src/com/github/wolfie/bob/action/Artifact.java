package com.github.wolfie.bob.action;

import java.io.File;

/**
 * An {@link Action} that will create one file or more (or an entire directory
 * structure) onto the computer's filesystem.
 * 
 * @author wolfie
 * 
 */
public interface Artifact extends Action {
  /**
   * Sets the destination directory in which the artifact will be placed.
   * 
   * @param directory
   *          The pre-existing destination directory.
   * @throws IllegalArgumentException
   *           if <tt>directory</tt> is not an existing, readable, directory.
   */
  void setDestination(File directory);
  
  /**
   * <p>
   * Sets the destination directory in which the artifact will be placed.
   * </p>
   * 
   * <p>
   * If the destination directory doesn't exist, the full path be lazily created
   * on-demand.
   * </p>
   * 
   * @param directory
   *          The destination directory.
   */
  void setDestinationForceCreate(File directory);
}
