package com.github.wolfie.bob;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public final class CompilationCache implements Serializable {
  
  public final static class Builder {
    
    private final CompilationCache cache;
    private boolean isBuilt = false;
    
    public Builder() {
      cache = new CompilationCache();
    }
    
    /**
     * 
     * @param srcPath
     * @param sourceFiles
     * @param classFiles
     * @return
     * @throws IllegalStateException
     *           if {@link #build()} has been called.
     */
    public Builder add(final String srcPath, final Iterable<File> sourceFiles,
        final Iterable<URI> classFiles) {
      if (!isBuilt) {
        cache.sourceFiles.put(srcPath, sourceFiles);
        cache.classFiles.put(srcPath, classFiles);
        return this;
      } else {
        throw new IllegalStateException("The "
            + CompilationCache.class.getSimpleName()
            + " was already built. Cannot add to it anymore.");
      }
    }
    
    public CompilationCache build() {
      isBuilt = true;
      return cache;
    }
  }
  
  private static final long serialVersionUID = -8990384639233236252L;
  
  private final Map<String, Iterable<File>> sourceFiles = new HashMap<String, Iterable<File>>();
  private final Map<String, Iterable<URI>> classFiles = new HashMap<String, Iterable<URI>>();
  
  private CompilationCache() {
  }
  
}
