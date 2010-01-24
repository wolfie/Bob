package com.github.wolfie.bob.result;

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.github.wolfie.bob.exception.BobRuntimeException;

public class Jar extends Artifact {
  private final Manifest manifest = new Manifest();
  
  private static final String DEFAULT_SOURCE_PATH = "src";
  
  private File sourceDir = null;
  private boolean includeSources = false;
  
  private String manifestFile;
  
  public Jar() {
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
  }
  
  public void setSourceDir(final File sourceDir) {
    if (sourceDir.isDirectory()) {
      this.sourceDir = sourceDir;
    } else {
      throw new BobRuntimeException("Source directory must be a directory. '"
          + sourceDir + "' doesn't seem to be.");
    }
  }
  
  public File getSourceDir() {
    return sourceDir;
  }
  
  public void process() {
    if (sourceDir == null) {
      setSourceDir(new File(DEFAULT_SOURCE_PATH));
    }
    
    new Exception().fillInStackTrace().printStackTrace();
  }
  
  public void setIncludeSources(final boolean includeSources) {
    this.includeSources = includeSources;
  }
  
  public boolean isIncludeSoruces() {
    return includeSources;
  }
  
  public void setManifestFile(final String manifestFile) {
    this.manifestFile = manifestFile;
  }
}
