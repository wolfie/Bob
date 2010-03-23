package com.github.wolfie.bob.action;

import java.io.File;

import com.github.wolfie.bob.Util;
import com.github.wolfie.bob.exception.DestinationNotSetException;

public abstract class AbstractArtifact implements Artifact {
  
  private File destination = null;
  private boolean createPath = false;
  
  @Override
  public AbstractArtifact setDestination(final File destination) {
    setDestination(destination, false);
    return this;
  }
  
  @Override
  public AbstractArtifact setDestinationForceCreate(final File destination) {
    setDestination(destination, true);
    return this;
  }
  
  private AbstractArtifact setDestination(final File destination,
      final boolean createPath) {
    Util.checkNulls(destination);
    this.destination = destination;
    this.createPath = createPath;
    return this;
  }
  
  /**
   * Get the destination to where this {@link Artifact} will be written.
   * 
   * @return The destination as a {@link File}
   * @throws DestinationNotSetException
   *           If the destination isn't explicitly set. This most often means
   *           that the client code should set a default destination.
   */
  protected File getDestination() throws DestinationNotSetException {
    if (destination != null) {
      if (createPath) {
        final File parentFile = destination.getAbsoluteFile().getParentFile();
        parentFile.mkdirs();
      }
      return destination;
    } else {
      throw new DestinationNotSetException();
    }
  }
}
