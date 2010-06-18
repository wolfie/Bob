package com.github.wolfie.bob.action;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import com.github.wolfie.bob.Defaults;
import com.github.wolfie.bob.Log;
import com.github.wolfie.bob.Util;

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
 * @since 1.0.0
 */
public class War extends Jar {
  
  private static final String WEB_XML_PATH = "WEB-INF/web.xml";
  
  private String webContentPath = null;
  private String webXmlPath = null;
  
  @Override
  protected void setDefaults() {
    if (super.manifestPath == null) {
      super.manifestPath = Defaults.WAR_MANIFEST_PATH;
    }
    
    if (super.toPath == null) {
      super.toPath = Defaults.WAR_PATH;
    }
    
    if (webContentPath == null) {
      webContentPath = Defaults.WEB_CONTENT_PATH;
    }
    
    // WARs have classes and sources here, not in the root
    super.archiveClassSourceDestination = "WEB-INF/classes/";
    
    super.setDefaults();
    
    if (super.fromCompilation != null) {
      super.fromCompilation.useJarsAt(Defaults.WAR_LIBS_PATH);
    }
  }
  
  @Override
  protected void subClassProcessHook(final Map<String, File> entryMap) {
    
    Log.fine("Finding web content files from " + webContentPath);
    final File webContentDirectory = new File(webContentPath);
    for (final File webFile : getWebContentFiles()) {
      
      final String entryName = Util.relativeFileName(webContentDirectory,
          webFile);
      
      if (webXmlPath != null && entryName.equals(WEB_XML_PATH)) {
        continue;
      }
      
      Log.finer(entryName + " <- " + webFile.getAbsolutePath());
      entryMap.put(entryName, webFile);
    }
    
    if (webXmlPath != null) {
      final File webXmlFile = new File(webXmlPath);
      if (webXmlFile.isFile()) {
        Log.fine("Using " + webXmlFile.getAbsolutePath() + " as web.xml");
        entryMap.put(WEB_XML_PATH, webXmlFile);
      } else {
        Log.warning(webXmlPath + " is not an existing file.");
      }
    }
    
    super.subClassProcessHook(entryMap);
  }
  
  private Collection<File> getWebContentFiles() {
    Log.fine("Finding web archive files");
    return Util.getFilesRecursively(new File(webContentPath));
  }
  
  /**
   * Explicitly define which file to use as the <tt>web.xml</tt> file.
   * 
   * @param path
   *          A file path.
   * @return <code>this</code>
   */
  public War withWebXmlFrom(final String path) {
    Util.checkNulls(path);
    webXmlPath = path;
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
    Util.checkNulls(path);
    webContentPath = path;
    return this;
  }
}
