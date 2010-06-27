package com.github.wolfie.bob;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProjectDescription {
  private boolean finalized = false;
  private final Set<String> sourcePaths = new LinkedHashSet<String>();
  private final Set<String> jarPaths = new HashSet<String>();
  private final Set<String> jarFiles = new HashSet<String>();
  
  /**
   * <p>
   * <tt>TODO: write javadoc</tt>
   * </p>
   * 
   * <p>
   * This method may be called several times to define several paths.
   * </p>
   * 
   * 
   * @param sourcePath
   * @return
   * @throws IllegalStateException
   *           if method is called oustide of the
   *           {@link BobBuild#describeProject(ProjectDescription)} method
   */
  public ProjectDescription sourcePath(final String sourcePath) {
    checkFinalized();
    Util.checkNulls(sourcePath);
    sourcePaths.add(sourcePath);
    return this;
  }
  
  /**
   * <p>
   * <tt>TODO: write javadoc</tt>
   * </p>
   * 
   * <p>
   * This method may be called several times to define several paths.
   * </p>
   * 
   * 
   * @param jarPath
   * @return
   * @throws IllegalStateException
   *           if method is called oustide of the
   *           {@link BobBuild#describeProject(ProjectDescription)} method
   */
  public ProjectDescription jarPath(final String jarPath) {
    checkFinalized();
    Util.checkNulls(jarPath);
    jarPaths.add(jarPath);
    return this;
  }
  
  private void checkFinalized() {
    if (finalized) {
      throw new IllegalStateException("The description was already finalized");
    }
  }
  
  void setFinalized() {
    finalized = true;
  }
  
  public ProjectDescription jarFile(final String jarFile) {
    jarFiles.add(jarFile);
    return this;
  }
  
  Set<String> getSourcePaths() {
    return sourcePaths;
  }
  
  Set<String> getJarPaths() {
    return jarPaths;
  }
  
  Set<String> getJarFiles() {
    return jarFiles;
  }
}
