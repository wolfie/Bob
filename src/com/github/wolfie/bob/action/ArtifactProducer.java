package com.github.wolfie.bob.action;

/**
 * An artifact producer is an {@link Action} that, after a successful
 * processing, produces an artifact on the file system as a result.
 * 
 * @author Henrik Paul
 * @since alpha3
 */
public interface ArtifactProducer extends Action {
  
  public interface FileProducer extends ArtifactProducer {
  }
  
  public interface PathProducer extends ArtifactProducer {
  }
  
  /**
   * Create the artifact to the given <tt>path</tt>.
   * 
   * @return The {@link ArtifactProducer} itself.
   */
  ArtifactProducer to(String path);
}
