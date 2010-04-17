package com.github.wolfie.bob;

public final class DefaultValues {
  
  public static final String ARTIFACTS_PATH = "artifacts";
  
  // jars
  
  public static final String JAR_MANIFEST_PATH = "META-INF/MANIFEST.MF";
  public static final String JAR_PATH = ARTIFACTS_PATH + "/build.jar";
  
  // wars
  
  public static final String WEB_CONTENT_PATH = "WebContent";
  public static final String WAR_MANIFEST_PATH = WEB_CONTENT_PATH + "/"
      + JAR_MANIFEST_PATH;
  public static final String WAR_PATH = ARTIFACTS_PATH + "/build.war";
  public static final String WAR_LIBS_PATH = WEB_CONTENT_PATH + "/WEB-INF/lib";
  
  private DefaultValues() {
  }
}
