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

import com.github.wolfie.bob.Log;
import com.github.wolfie.bob.Util;
import com.github.wolfie.bob.exception.ProcessingException;

/**
 * Compile and package the project into a JAR file
 * 
 * @author Henrik Paul
 */
public class Jar extends AbstractArtifact implements Artifact,
    HasSourceDirectory, HasClassDirectory {
  
  private File sourceDir = new File("src");
  private boolean sourceDirSetByUser = false;
  
  private File classesDir = null;
  private boolean classesDirSetByUser = false;
  
  private File manifestFile = new File("META-INF/MANIFEST.MF");
  
  public Jar(final String targetFileName) {
    this(new File(targetFileName));
  }
  
  public Jar(final File targetFile) {
    setDestinationForceCreate(targetFile);
  }
  
  /**
   * Define a manifest file for the Jar.
   * 
   * @param manifest
   *          the {@link File} representing the manifest file.
   * @return <code>this</code>
   */
  public Jar setManifestFile(final File manifest) {
    Util.checkNulls(manifest);
    manifestFile = manifest;
    return this;
  }
  
  /**
   * A conveniency method, equals to calling
   * 
   * <pre>
   * <code>{@link #setManifestFile(File) setManifestFile}(new File("META-INF/MANIFEST.MF"));</code>
   * </pre>
   */
  public Jar useDefaultManifestFile() {
    setManifestFile(new File("META-INF/MANIFEST.MF"));
    return this;
  }
  
  public File getSourceDirectory() {
    return sourceDir;
  }
  
  public Jar setSourceDirectory(final File sourceDir) {
    Util.checkNulls(sourceDir);
    
    if (!classesDirSetByUser) {
      this.sourceDir = sourceDir;
      sourceDirSetByUser = true;
    } else {
      throw new IllegalStateException("classes directory was already defined, "
          + "and trying to define a source directory. "
          + "Only one may be defined.");
    }
    
    return this;
  }
  
  public File getManifestFile() {
    return manifestFile;
  }
  
  public File getClassesDir() {
    return classesDir;
  }
  
  public Jar setClassDirectory(final File classDirectory) {
    if (!sourceDirSetByUser) {
      classesDir = classDirectory;
      classesDirSetByUser = true;
    } else {
      throw new IllegalStateException("source directory was already defined, "
          + "and trying to define a classes directory. "
          + "Only one may be defined.");
    }
    
    return this;
  }
  
  public File getClassDirectory() {
    return classesDir;
  }
  
  public void process() {
    File classesDir;
    if (!classesDirSetByUser) {
      final Compile compileAction = new Compile();
      compileAction.setSourceDirectory(getSourceDirectory());
      compileAction.process();
      classesDir = compileAction.getDestination();
    } else {
      classesDir = this.classesDir;
    }
    
    try {
      final File destination = getDestination();
      
      final FileOutputStream fileOutputStream = new FileOutputStream(
          destination);
      final JarOutputStream jarOutputStream;
      if (manifestFile == null || !manifestFile.isFile()
          || !manifestFile.canRead()) {
        logWhyNoManifestFile();
        jarOutputStream = new JarOutputStream(fileOutputStream);
      } else {
        Log.fine("Using manifest file: " + manifestFile.getPath());
        final Manifest manifest = new Manifest(
            new FileInputStream(manifestFile));
        jarOutputStream = new JarOutputStream(fileOutputStream, manifest);
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
  
  private void logWhyNoManifestFile() {
    Log.finer("Not using a manifest file.");
    
    if (manifestFile != null) {
      if (!manifestFile.exists()) {
        Log.finer(manifestFile.getPath() + " does not exist");
      } else if (!manifestFile.canRead()) {
        Log.finer(manifestFile.getPath() + " could not be read");
      } else if (!manifestFile.isFile()) {
        Log.finer(manifestFile.getPath() + " is not a file");
      }
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
}
