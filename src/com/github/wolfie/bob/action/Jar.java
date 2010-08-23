package com.github.wolfie.bob.action;

import com.github.wolfie.bob.internals.action._Jar;
import com.github.wolfie.bob.util.Util;

/**
 * <p>
 * Package the project into a JAR file.
 * </p>
 * 
 * <h1>Assumptions</h1>
 * <p>
 * <i>There are no assumptions.</i>
 * </p>
 * 
 * <h1>Conventions</h1>
 * 
 * <ul>
 * <li>The classes are compiled from a default instance of {@link Compilation}</li>
 * <li>The resulting file is <tt>artifacts/build.jar</tt></li>
 * <li>Sources are not included</li>
 * <li>Manifest file is taken from <tt>META-INF/MANIFEST.MF</tt>, if exists</li>
 * </ul>
 * 
 * @author Henrik Paul
 */
public class Jar implements Action, ArtifactProducer.FileProducer {
  
  private final _Jar internal = new _Jar();
  
  /**
   * Get classes to package from a {@link Compilation}
   * 
   * @param compilation
   * @return <code>this</code>
   * @throws IllegalStateException
   *           if any <tt>from()</tt> method was previously called
   */
  public Jar from(final Compilation compilation) throws IllegalStateException {
    internal.from(Util.getInternal(compilation));
    return this;
  }
  
  /**
   * Get classes to package from the filesystem
   * 
   * @param path
   *          The path at which the classes will be found.
   * @return <code>this</code>
   * @throws IllegalStateException
   *           if any <tt>from()</tt> method was previously called
   */
  public Jar from(final String path) throws IllegalStateException {
    internal.from(path);
    return this;
  }
  
  /**
   * The path of the resulting jar file
   * 
   * @param path
   *          a file path
   * @return <code>this</code>
   */
  @Override
  public Jar to(final String path) {
    internal.to(path);
    return this;
  }
  
  /**
   * Use a manifest file from an explicit path.
   * 
   * @param path
   *          the file path to the manifest file.
   * @return <code>this</code>
   */
  public Jar withManifestFrom(final String path) {
    internal.withManifestFrom(path);
    return this;
  }
  
  /**
   * Include sources to the resulting jar from a chained supplier.
   * 
   * @return <code>this</code>
   * @throws IllegalStateException
   *           if {@link #withSourcesFrom(String)} was called previously.
   * @see #from(Compilation)
   */
  public Jar withSources() throws IllegalStateException {
    internal.withSources();
    return this;
  }
  
  /**
   * Include sources to the resulting jar from the filesystem.
   * 
   * @param path
   *          The path on the filesystem where the source folder for the Jar
   *          package are found.
   * @return <code>this</code>
   * @throws IllegalStateException
   *           if {@link #withSources()} was called previously.
   */
  public Jar withSourcesFrom(final String path) throws IllegalStateException {
    internal.withSourcesFrom(path);
    return this;
  }
}
