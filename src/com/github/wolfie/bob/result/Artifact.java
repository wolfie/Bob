package com.github.wolfie.bob.result;

public abstract class Artifact implements Action {
  public static final String DEFAULT_ARTIFACT_PATH = "artifacts";
  
  private String artifactPath = null;
  
  public void setArtifactPath(final String artifactPath) {
    this.artifactPath = artifactPath;
  }
  
  public String getArtifactPath() {
    return artifactPath;
  }
}
