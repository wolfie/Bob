package com.github.wolfie.bob;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class CompilationCache implements Serializable {
  
  public static final class NotCompiledException extends Exception {
    private static final long serialVersionUID = -2930510932278725435L;
    
    public NotCompiledException(final String srcPath) {
      super("Path " + srcPath + " was not declared as a source directory.");
    }
  }
  
  public final static class Builder {
    
    private final CompilationCache cache;
    private boolean isBuilt = false;
    
    public Builder(final File baseDirForClasses) {
      cache = new CompilationCache(baseDirForClasses);
    }
    
    /**
     * 
     * @param srcPath
     * @param sourceFiles
     * @param set
     * @return
     * @throws IllegalStateException
     *           if {@link #commit()} has been called.
     */
    public Builder add(final String srcPath, final Set<File> sourceFiles,
        final Set<URI> set) {
      if (!isBuilt) {
        cache.sourceFiles.put(srcPath, sourceFiles);
        cache.classFiles.put(srcPath, set);
        return this;
      } else {
        throw new IllegalStateException("The "
            + CompilationCache.class.getSimpleName()
            + " was already built. Cannot add to it anymore.");
      }
    }
    
    public CompilationCache commit() {
      isBuilt = true;
      return cache;
    }
  }
  
  private static final long serialVersionUID = -8990384639233236252L;
  
  private static CompilationCache singleton;
  
  private final Map<String, Set<File>> sourceFiles = new HashMap<String, Set<File>>();
  private final HashMap<String, Set<URI>> classFiles = new HashMap<String, Set<URI>>();
  private final File baseDirForClasses;
  
  private CompilationCache(final File baseDirForClasses) {
    Util.checkNulls(baseDirForClasses);
    if (!baseDirForClasses.isDirectory()) {
      throw new IllegalArgumentException(baseDirForClasses
          + " is not an existing directory.");
    }
    
    if (!baseDirForClasses.canRead()) {
      throw new IllegalArgumentException(baseDirForClasses + " cannot be read");
    }
    
    this.baseDirForClasses = baseDirForClasses;
  }
  
  /**
   * Get the compiled classfiles in the file system that have been precompiled
   * for <tt>srcPath</tt>
   * 
   * @throws NotCompiledException
   *           if the requested <tt>srcPath</tt> hasn't been precompiled.
   */
  public Set<File> getClassFiles(final String srcPath)
      throws NotCompiledException {
    
    if (classFiles.containsKey(srcPath)) {
      return convertToFiles(classFiles.get(srcPath));
    } else {
      throw new NotCompiledException(srcPath);
    }
  }
  
  private static Set<File> convertToFiles(final Set<URI> set) {
    final HashSet<File> files = new HashSet<File>();
    for (final URI uri : set) {
      final File file = new File(uri);
      files.add(file);
    }
    return files;
  }
  
  public File getBaseDirForClasses() {
    return baseDirForClasses;
  }
  
  public static CompilationCache get() {
    if (singleton == null) {
      throw new IllegalStateException("The singleton instance of "
          + CompilationCache.class.getSimpleName() + " is not set");
    }
    return singleton;
  }
  
  static void set(final CompilationCache cache) {
    if (singleton != null) {
      throw new IllegalStateException("Cannot re-set the "
          + CompilationCache.class.getSimpleName() + " singleton instance");
    }
    singleton = cache;
  }
}
