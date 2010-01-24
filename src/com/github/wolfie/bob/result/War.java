package com.github.wolfie.bob.result;

import java.util.HashSet;
import java.util.Set;

public class War extends Jar {
  
  private final Set<String> libraries = new HashSet<String>();
  private String webXML = null;
  
  @Override
  public void process() {
  }
  
  public void addLibrary(final String library) {
    libraries.add(library);
  }
  
  public void setWebXML(final String webXML) {
    this.webXML = webXML;
  }
}
