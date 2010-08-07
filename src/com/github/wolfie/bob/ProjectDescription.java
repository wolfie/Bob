package com.github.wolfie.bob;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProjectDescription {
  
  private boolean finalized = false;
  
  private final LinkedHashSet<String> sourcePaths = new LinkedHashSet<String>();
  private final Set<String> sourcePathsOptional = new HashSet<String>();
  private final Set<String> jarPaths = new HashSet<String>();
  private final Set<String> jarPathsOptional = new HashSet<String>();
  
  private final Set<String> jarFiles = new HashSet<String>();
  
  /**
   * <p>
   * Get a freely modifiable instance of {@link ProjectDescription} that
   * conforms to Bob's default project layout.
   * </p>
   * 
   * <p>
   * Default values:
   * <dl>
   * <dt>Source path:</dt>
   * <dd>src</dd>
   * <dd>optional: test</dd>
   * <dt>JAR path:</dt>
   * <dd>optional: lib</dd>
   * </dl>
   * </p>
   */
  public static ProjectDescription getDefault() {
    return new ProjectDescription()
        .sourcePath(Defaults.SOURCE_PATH)
        .sourcePathOptional(Defaults.DEFAULT_TEST_SRC_PATH)
        .jarPathOptional(Defaults.LIBRARY_PATH);
  }
  
  /**
   * <p>
   * Define a source path, relative to the project's root directory.
   * </p>
   * 
   * <p>
   * If the given path does not exist, or is not readable, the build will halt.
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
  
  private ProjectDescription sourcePathOptional(final String sourcePath) {
    sourcePath(sourcePath);
    sourcePathsOptional.add(sourcePath);
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
  
  private ProjectDescription jarPathOptional(final String jarPath) {
    jarPath(jarPath);
    jarPathsOptional.add(jarPath);
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
    return Collections.unmodifiableSet(sourcePaths);
  }
  
  boolean isSourcePathOptional(final String sourcePath) {
    return sourcePathsOptional.contains(sourcePath);
  }
  
  /**
   * Get all
   */
  Set<String> getJarPaths() {
    return Collections.unmodifiableSet(jarPaths);
  }
  
  boolean isJarPathOptional(final String jarPath) {
    return jarPathsOptional.contains(jarPath);
  }
  
  Set<String> getJarFiles() {
    return Collections.unmodifiableSet(jarFiles);
  }
}
