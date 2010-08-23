package com.github.wolfie.bob.action;

import com.github.wolfie.bob.internals.action._War;

/**
 * <p>
 * Package the project into a WAR file.
 * </p>
 * 
 * <h1>Assumptions</h1>
 * <ul>
 * <li>Web content must include the <tt>META-INF</tt> and <tt>WEB-INF</tt>
 * directories, in addition to all other web resources.</li>
 * <li>All JARs used during compilation will be added to the WAR's libraries.</li>
 * </ul>
 * 
 * <h1>Conventions</h1>
 * 
 * <ul>
 * <li>The classes are compiled from a default instance of {@link Compilation}</li>
 * <li>The compilation will search <tt>WebContent/WEB-INF/lib</tt> for JARs.</li>
 * <li>The resulting file is <tt>artifacts/build.war</tt></li>
 * <li>Sources are not included</li>
 * <li>All web content is taken from <tt>WebContent</tt></li>
 * <li>Manifest file is taken from <tt>WebContent/META-INF/MANIFEST.MF</tt>, if
 * exists</li>
 * <li><tt>web.xml</tt> is taken from <tt>WebContent/WEB-INF/web.xml</tt>, if
 * exists</li>
 * </ul>
 * 
 * @author Henrik Paul
 */
public class War extends Jar implements Action,
    ArtifactProducer.FileProducer {
  
  private final _War internal = new _War();
  
  /**
   * Explicitly define which file to use as the <tt>web.xml</tt> file.
   * 
   * @param path
   *          A file path.
   * @return <code>this</code>
   */
  public War withWebXmlFrom(final String path) {
    internal.withWebXmlFrom(path);
    return this;
  }
  
  /**
   * Define the path where the web content will be found at.
   * 
   * @param path
   *          A directory path.
   * @return <code>this</code>
   */
  public War withWebContentFrom(final String path) {
    internal.withWebContentFrom(path);
    return this;
  }
}
