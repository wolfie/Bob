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
import com.github.wolfie.bob.exception.NoSourcesToIncludeException;
import com.github.wolfie.bob.exception.ProcessingException;
import com.sun.tools.internal.ws.processor.ProcessorException;

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
      
      Log.fine("Adding classfiles from " + classesDir.getAbsolutePath());
      final Collection<File> classFiles = Util.getFilesRecursively(classesDir,
          Util.JAVA_CLASS_FILE);
      for (final File classFile : Util.normalizeFilePaths(classesDir,
          classFiles)) {
        add(classesDir, classFile, jarOutputStream);
      }
      
      try {
        final File sourcesDir = getSourcesDirectory();
        Log.fine("Adding sources from " + sourcesDir.getAbsolutePath());
        final Collection<File> sourceFiles = Util.getFilesRecursively(
            sourcesDir, Util.JAVA_SOURCE_FILE);
        for (final File sourceFile : Util.normalizeFilePaths(sourcesDir,
            sourceFiles)) {
          add(sourcesDir, sourceFile, jarOutputStream);
        }
      } catch (final NoSourcesToIncludeException e) {
        // okay, fine, no sources then.
      }
      
      jarOutputStream.close();
      
      Log.info("Wrote " + destination.getAbsolutePath());
    } catch (final Exception e) {
      throw new ProcessingException(e);
    }
  }
  
  private File getSourcesDirectory() throws NoSourcesToIncludeException {
    if (sourcesFromPath != null) {
      return new File(sourcesFromPath);
    } else if (sourcesFromChain) {
      if (fromCompilation != null) {
        return fromCompilation.getSourceDirectory();
      } else {
        Log.severe("Although requested, no sources will be included, since " +
            "there are no available chains to take sources from.");
        throw new NoSourcesToIncludeException();
      }
    } else {
      throw new NoSourcesToIncludeException();
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
      final File destination = new File(toPath);
      
      if (destination.exists()) {
        Util.delete(destination);
      }
      
      final File parentFile = destination.getParentFile();
      if (!parentFile.exists()) {
        Util.createDir(parentFile);
      }
      
      return destination;
      
    } else {
      throw new InternalConsistencyException("No destination path defined");
    }
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
    try {
      compilation.to(Util.getTemporaryDirectory().getAbsolutePath());
      compilation.process();
      
      final File directory = compilation.getDestinationDirectory();
      return Util.checkedDirectory(directory);
    } catch (final IOException e) {
      throw new ProcessorException("Could not create a temporary directory.", e);
    }
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
  
  public Jar withManifestFrom(final String path) {
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
