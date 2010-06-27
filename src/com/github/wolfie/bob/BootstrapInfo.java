package com.github.wolfie.bob;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public class BootstrapInfo implements Serializable {
  
  private static final long serialVersionUID = -6340121775075760399L;
  
  private final CompilationCache cache;
  
  private final Collection<File> classpath;
  private transient Collection<File> unmodifiableClasspath = null;
  
  public BootstrapInfo(final CompilationCache cache,
      final Collection<File> classpath) {
    
    this.cache = cache;
    this.classpath = classpath;
    
    if (!(classpath instanceof Serializable)) {
      throw new BootstrapError("classpath method must be Serializable");
    }
  }
  
  public CompilationCache getCache() {
    return cache;
  }
  
  public Collection<File> getClasspath() {
    if (unmodifiableClasspath == null) {
      unmodifiableClasspath = Collections.unmodifiableCollection(classpath);
    }
    return unmodifiableClasspath;
  }
}
