package com.github.wolfie.bob.action;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import com.github.wolfie.bob.DefaultValues;
import com.github.wolfie.bob.Log;
import com.github.wolfie.bob.Util;

public class War extends Jar {
  
  private String webContentPath = null;
  
  @Override
  protected void setDefaults() {
    if (super.manifestPath == null) {
      super.manifestPath = DefaultValues.WAR_MANIFEST_PATH;
    }
    
    if (super.toPath == null) {
      super.toPath = DefaultValues.WAR_PATH;
    }
    
    if (webContentPath == null) {
      webContentPath = DefaultValues.WEB_CONTENT_PATH;
    }
    
    // WARs have classes and sources here, not in the root
    super.archiveClassSourceDestination = "WEB-INF/classes/";
    
    super.setDefaults();
    
    if (super.fromCompilation != null) {
      super.fromCompilation.useJarsAt(DefaultValues.WAR_LIBS_PATH);
    }
  }
  
  @Override
  protected void subClassProcessHook(final Map<String, File> entryMap) {
    
    Log.fine("Finding web content files");
    final File webContentDirectory = new File(webContentPath);
    for (final File webFile : getWebContentFiles()) {
      final String entryName = Util.relativeFileName(webContentDirectory,
          webFile);
      Log.finer(entryName + " <- " + webFile.getAbsolutePath());
      entryMap.put(entryName, webFile);
    }
    
    super.subClassProcessHook(entryMap);
  }
  
  private Collection<File> getWebContentFiles() {
    Log.fine("Finding web archive files");
    return Util.getFilesRecursively(new File(webContentPath));
  }
}