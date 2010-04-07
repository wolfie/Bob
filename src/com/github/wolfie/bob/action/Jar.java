package com.github.wolfie.bob.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import com.github.wolfie.bob.DefaultValues;
import com.github.wolfie.bob.Log;
import com.github.wolfie.bob.Util;
import com.github.wolfie.bob.exception.InternalConsistencyException;
import com.github.wolfie.bob.exception.NoManifestFileFoundException;
import com.github.wolfie.bob.exception.ProcessingException;

/**
 * Compile and package the project into a JAR file
 * 
 * @author Henrik Paul
 */
public class Jar implements Action {
  
  private Compilation fromCompilation;
  private String fromPath;
  private String toPath;
  private String manifestPath;
  private boolean sourcesFromChain;
  private String sourcesFromPath;
  
  // public static class Manifest {
  //    
  // /**
  // * The value of this attribute specifies the relative URLs of the extensions
  // * or libraries that this application or extension needs. URLs are separated
  // * by one or more spaces. The application or extension class loader uses the
  // * value of this attribute to construct its internal search path.
  // */
  // public static final String CLASS_PATH = "Class-Path";
  //    
  // /**
  // * The value of this attribute defines the relative path of the main
  // * application class which the launcher will load at startup time. The value
  // * must <em>not</em> have the <tt>.class</tt> extension appended to the
  // * class name.
  // */
  // public static final String MAIN_CLASS = "Main-Class";
  //    
  // /**
  // * The value is a string that defines the title of the extension
  // * implementation.
  // */
  // public static final String IMPLEMENTATION_TITLE = "Implementation-Title";
  //    
  // /**
  // * The value is a string that defines the version of the extension
  // * implementation.
  // */
  // public static final String IMPLEMENTATION_VERSION =
  // "Implementation-Version";
  //    
  // /**
  // * The value is a string that defines the organization that maintains the
  // * extension implementation.
  // */
  // public static final String IMPLEMENTATION_VENDOR = "Implementation-Vendor";
  //    
  // /**
  // * The value is a string id that uniquely defines the organization that
  // * maintains the extension implementation.
  // */
  // public static final String IMPLEMENTATION_VENDOR_ID =
  // "Implementation-Vendor-Id";
  //    
  // /**
  // * This attribute defines the URL from which the extension implementation
  // * can be downloaded from.
  // */
  // public static final String IMPLEMENTATION_URL = "Implementation-Url";
  //    
  // @SuppressWarnings("unused")
  // private static final String CREATED_BY = "Created-By";
  // @SuppressWarnings("unused")
  // private static final String MANIFEST_VERSION = "Manifest-Version";
  //    
  // public Manifest put(final String key, final String value) {
  // // TODO Auto-generated method stub
  // return this;
  // }
  // }
  
  @Override
  public void process() {
    setDefaults();
    
    final File classesDir = getClassesDirectory();
    
    try {
      final File destination = getDestination();
      
      final FileOutputStream fileOutputStream = new FileOutputStream(
          destination);
      JarOutputStream jarOutputStream;
      
      try {
        final File manifestFile = getManifestFile();
        final Manifest manifest = new Manifest(
            new FileInputStream(manifestFile));
        jarOutputStream = new JarOutputStream(fileOutputStream, manifest);
      } catch (final NoManifestFileFoundException e) {
        jarOutputStream = new JarOutputStream(fileOutputStream);
      }
      
      Log.fine("Adding files from " + classesDir.getPath());
      final Collection<File> classFiles = Util.getFilesRecursively(classesDir,
          Util.JAVA_CLASS_FILE);
      for (final File classFile : Util.normalizeFilePaths(classesDir,
          classFiles)) {
        add(classesDir, classFile, jarOutputStream);
      }
      jarOutputStream.close();
      
      Log.info("Wrote " + destination.getAbsolutePath());
    } catch (final Exception e) {
      throw new ProcessingException(e);
    }
  }
  
  private File getManifestFile() throws NoManifestFileFoundException {
    final File manifestFile = new File(manifestPath);
    if (manifestFile.exists() && manifestFile.canRead()) {
      return manifestFile;
    } else {
      Log.info("Could not include manifest from file "
          + manifestFile.getAbsolutePath());
      throw new NoManifestFileFoundException();
    }
  }
  
  /**
   * Setting the defaults just before starting to process the action itself.
   */
  private void setDefaults() {
    if (fromCompilation == null && fromPath == null) {
      fromCompilation = new Compilation();
    }
    
    if (manifestPath == null) {
      manifestPath = DefaultValues.MANIFEST_PATH;
    }
    
    // no sources added by default
    
    if (toPath == null) {
      toPath = DefaultValues.JAR_PATH;
    }
  }
  
  private File getDestination() {
    if (toPath != null) {
      return Util.checkedDirectory(clearDirectory(new File(toPath)));
    } else {
      throw new InternalConsistencyException("No destination path defined");
    }
  }
  
  private static File clearDirectory(final File destination) {
    if (destination.exists()) {
      Util.delete(destination);
    }
    
    Util.createDir(destination);
    return destination;
  }
  
  /**
   * Resolve the directory in which the classes will be found.
   * 
   * @return a {@link File} representing the directory containing the classfiles
   * @throws NotAReadableDirectoryException
   *           if the resulting directory is not, in fact, a readable directory
   *           at all.
   */
  private File getClassesDirectory() {
    if (fromCompilation != null) {
      return getClassesDirectoryFromCompilation(fromCompilation);
    } else if (fromPath != null) {
      return getClassesDirectoryFromPath(fromPath);
    } else {
      throw new InternalConsistencyException("No class source defined");
    }
  }
  
  /**
   * @param path
   * @return
   * @throws NotAReadableDirectoryException
   *           if the resulting directory is not, in fact, a readable directory
   *           at all.
   */
  private static File getClassesDirectoryFromPath(final String path) {
    return Util.checkedDirectory(new File(path));
  }
  
  /**
   * @param compilation
   * @return
   * @throws NotAReadableDirectoryException
   *           if the resulting directory is not, in fact, a readable directory
   *           at all.
   */
  private static File getClassesDirectoryFromCompilation(
      final Compilation compilation) {
    compilation.process();
    
    final File directory = compilation.getDestinationDirectory();
    return Util.checkedDirectory(directory);
  }
  
  /**
   * See <a href="http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file"
   * >Stack Overflow</a> for explanation on method.
   * 
   * @param source
   *          The {@link File} to add to the Jar.
   * @param classFile
   * @param target
   *          The Jar's {@link JarOutputStream}.
   * @throws IOException
   */
  private static void add(final File baseDir, final File source,
      final JarOutputStream target) throws IOException {
    final String baseDirPath = baseDir.getPath();
    
    // strip the base dir from the entry name.
    String name = source.getPath().replace(baseDirPath, "");
    // ZIPs require forward slashes.
    name = name.replace("\\", "/").substring(1);
    
    Log.finer("Adding " + name);
    
    final JarEntry entry = new JarEntry(name);
    entry.setTime(source.lastModified());
    target.putNextEntry(entry);
    
    final BufferedInputStream in = new BufferedInputStream(new FileInputStream(
        source));
    
    try {
      final byte[] buffer = new byte[1024];
      while (true) {
        final int count = in.read(buffer);
        if (count == -1) {
          break;
        }
        target.write(buffer, 0, count);
      }
      target.closeEntry();
    } finally {
      in.close();
    }
  }
  
  /**
   * Get classes to package from a {@link Compilation}
   * 
   * @param compilation
   * @return <code>this</code>
   * @throws IllegalStateException
   *           if any <tt>from()</tt> method was previously called
   */
  public Jar from(final Compilation compilation) {
    if (fromPath == null && fromCompilation == null) {
      fromCompilation = compilation;
      return this;
    } else {
      throw new IllegalStateException("from was already set, cannot reset");
    }
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
  public Jar from(final String path) {
    if (fromPath == null && fromCompilation == null) {
      fromPath = path;
      return this;
    } else {
      throw new IllegalStateException("from was already set, cannot reset");
    }
  }
  
  public Jar to(final String path) {
    toPath = path;
    return this;
  }
  
  public Jar withManifest(final String path) {
    manifestPath = path;
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
  public Jar withSources() {
    if (sourcesFromPath == null) {
      sourcesFromChain = true;
      return this;
    } else {
      throw new IllegalStateException("Sources were already being taken from " +
          "a path. Can't take sources from two places at once");
    }
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
  public Jar withSourcesFrom(final String path) {
    if (!sourcesFromChain) {
      sourcesFromPath = path;
      return this;
    } else {
      throw new IllegalStateException("Sources were already being taken " +
          "from the chain. Can't take sources from two places at once");
    }
  }
}
