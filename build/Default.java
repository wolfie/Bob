import com.github.wolfie.bob.action.Action;
import com.github.wolfie.bob.action.Compilation;
import com.github.wolfie.bob.action.Jar;
import com.github.wolfie.bob.annotation.Target;

public class Default {
  
  /**
   * This is the simplest form of Compilation. It will compile the default
   * source folder (<tt>src/</tt>) and put it under the default artifacts folder
   * (<tt>artifacts</tt>). If the default library path (<tt>lib/</tt>) contains
   * any <tt>.jar</tt>-files, they will be added to the classpath.
   */
  @Target
  public Action simpleCompilation() {
    return new Compilation();
  }
  
  /**
   * <p>
   * When this target is called on its own, the result of this target is
   * identical to what comes out of {@link #simpleCompilation()}.
   * </p>
   * 
   * <p>
   * However, if this would be used as a source to a, say, Jar action, this
   * would also result into an artifact, because the target path is explicitly
   * defined. A simple compilation would, when chained, would result only in
   * temporary files.
   * </p>
   */
  @Target
  public Action explicitCompilation() {
    
    return new Compilation()
        .sourcesFrom("src")
        .useJarsAt("lib")
        .to("artifacts");
  }
  
  /**
   * <p>
   * This is the simplest form of Jar. It will compile the project according to
   * {@link #simpleCompilation()}, and package use the produced class-files. No
   * individual class files will be produced as artifacts.
   * </p>
   * 
   * <p>
   * If a file <tt>META-INF/MANIFEST.MF</tt> exists, it will be used as the
   * jar's manifest file.
   * </p>
   * 
   * 
   * <p>
   * The default location for the resulting jar is <tt>artifacts/build.jar</tt>.
   * </p>
   */
  @Target
  public Action simpleJar() {
    return new Jar();
  }
  
  /**
   * Effectively identical to {@link #simpleJar()}
   */
  @Target
  public Action explicitJar() {
    return new Jar()
        .from(new Compilation())
        .withManifestFrom("META-INF/MANIFEST.MF")
        .to("artifacts/build.jar");
  }
  
  // /**
  // * You can also define the Jar's manifest file within the build target. In
  // * this case, the <tt>META-INF/MANIFEST.MF</tt> will be ignored. This will
  // not
  // * be supported in version 1.0.0.
  // */
  // @Target
  // public Action jarWithInlineManifest() {
  //    
  // // com.github.wolfie.action.Jar.Manifest
  // final Manifest manifest = new Manifest()
  // .put(Manifest.CLASS_PATH, "foo/ bar/")
  // .put(Manifest.MAIN_CLASS, "foo.bar.Baz")
  // .put(Manifest.IMPLEMENTATION_TITLE, "Baz Application")
  // .put(Manifest.IMPLEMENTATION_VERSION, "1.0.0")
  // .put(Manifest.IMPLEMENTATION_VENDOR, "Acme Ltd")
  // .put(Manifest.IMPLEMENTATION_VENDOR_ID, "com.acme")
  // .put(Manifest.IMPLEMENTATION_URL, "http://example.com/Baz")
  // .put("Vaadin-Package-Version", "1")
  // .put("Vaadin-License-Title", "Apache License 2.0");
  //    
  // return new Jar()
  // .withManifest(manifest);
  // }
  
  /**
   * If you want to include sources with your classes, you can use
   * {@link Jar#withSources()} if you used the default jar, or your classes are
   * taken from a chain with a {@link Compilation}.
   * 
   * @return
   */
  @Target
  public Action jarWithSources() {
    return new Jar()
        .withSources();
  }
  
  /**
   * If, however, you define a class path, you must explicitly define a sources
   * path, too.
   * 
   * @return
   */
  public Action jarWithExplicitSources() {
    return new Jar()
        .from("bin")
        .withSourcesFrom("src");
  }
  
  /**
   * A build target required to Bob into its executable jar.
   */
  @Target(defaultTarget = true)
  public Action bobJar() {
    return new Jar()
        .withSources()
        .to("artifacts/bob.jar");
  }
}
