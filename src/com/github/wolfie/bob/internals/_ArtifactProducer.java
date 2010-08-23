package com.github.wolfie.bob.internals;

import java.io.File;

import com.github.wolfie.bob.action.ArtifactProducer;

/**
 * An artifact producer is an {@link Action} that, after a successful
 * processing, produces an artifact on the file system as a result.
 * 
 * @author Henrik Paul
 * @since alpha3
 */
public interface _ArtifactProducer extends ArtifactProducer, _Action {
  
  public interface _FileProducer extends _ArtifactProducer {
  }
  
  public interface _PathProducer extends _ArtifactProducer {
  }
  
  /**
   * Gets the resulting artifact file. If the associated _Action has not yet
   * been processed, it will be processed first.
   */
  File get();
}
