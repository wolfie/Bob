package com.github.wolfie.bob.action;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.github.wolfie.bob.Log;
import com.github.wolfie.bob.Util;
import com.github.wolfie.bob.exception.CompilationFailedException;

public class Compilation implements Action {
  
  private static class BobDiagnosticListener implements
      DiagnosticListener<JavaFileObject> {
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics = new ArrayList<Diagnostic<? extends JavaFileObject>>();
    
    @Override
    public void report(final Diagnostic<? extends JavaFileObject> diagnostic) {
      diagnostics.add(diagnostic);
    }
    
    public boolean hasProblems() {
      return !diagnostics.isEmpty();
    }
    
    public List<Diagnostic<? extends JavaFileObject>> getProblems() {
      return Collections.unmodifiableList(diagnostics);
    }
  }
  
  private static final FileFilter JAR_FILTER = new FileFilter() {
    @Override
    public boolean accept(final File file) {
      return file.isFile() && file.canRead()
          && file.getName().toLowerCase().endsWith(".jar");
    }
  };
  
  private static final String DEFAULT_SOURCE_PATH = "src";
  private static final String DEFAULT_DESTINATION_PATH = "artifacts";
  private static final String DEFAULT_JARS_PATH = "lib";
  
  private String sourcePath;
  private String destinationPath;
  private final Set<String> jarPaths = new HashSet<String>();
  
  /** The cached result for the destination */
  private File destinationDir = null;
  
  /** The cached result for where the sources are */
  private File sourceDir = null;
  
  private boolean disableDebug = false;
  
  @Override
  public void process() {
    final File sourceDir = getSourceDirectory();
    
    Log.fine("Finding source files from " + sourceDir.getAbsolutePath());
    
    final Collection<File> javaFiles = Util.getFilesRecursively(sourceDir,
          Util.JAVA_SOURCE_FILE);
    
    for (final File file : javaFiles) {
      Log.finer("Found file " + file.getPath());
    }
    
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final StandardJavaFileManager fileManager = compiler
          .getStandardFileManager(null, null, null);
    
    final File destination = getDestinationDirectory();
    
    try {
      fileManager.setLocation(StandardLocation.CLASS_PATH, getClassPath());
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections
            .singleton(destination));
    } catch (final IOException e) {
      throw new CompilationFailedException(e);
    }
    
    final Iterable<? extends JavaFileObject> javaFileObjects = fileManager
          .getJavaFileObjectsFromFiles(javaFiles);
    
    final BobDiagnosticListener diagnosticListener = new BobDiagnosticListener();
    compiler.getTask(null, fileManager, diagnosticListener, getCompilerOptions(), null,
          javaFileObjects).call();
    
    if (diagnosticListener.hasProblems()) {
      final StringBuilder builder = new StringBuilder();
      for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnosticListener
            .getProblems()) {
        builder.append(diagnostic.getMessage(null));
      }
      throw new CompilationFailedException(builder.toString());
    }
  }
  
  private Iterable<String> getCompilerOptions() {
    if (disableDebug) {
      return Collections.emptySet();
    } else {
      return Arrays.asList("-g");
    }
  }
  
  File getDestinationDirectory() {
    if (destinationDir == null) {
      destinationDir = new File(
          destinationPath != null ? destinationPath
              : DEFAULT_DESTINATION_PATH);
      
      if (destinationDir.exists()) {
        Util.delete(destinationDir);
      }
      Util.createDir(destinationDir);
    }
    return destinationDir;
  }
  
  /**
   * <p>
   * Get the directory for the sources to compile.
   * </p>
   * 
   * <p>
   * If the path was given via {@link #sourcesFrom(String)}, that will be used
   * as the path. Otherwise, {@link #DEFAULT_SOURCE_PATH} (
   * {@value #DEFAULT_SOURCE_PATH}) will be used.
   * </p>
   * 
   * @return The alleged source directory. Not guaranteed to contain any
   *         compilable files, however.
   * @throws CompilationFailedException
   *           if the evaulated source path was either not a directory, or was
   *           not readable.
   */
  File getSourceDirectory() {
    if (sourceDir == null) {
      final File sourceDir = new File(sourcePath != null ? sourcePath
          : DEFAULT_SOURCE_PATH);
      
      if (sourceDir.isDirectory() && sourceDir.canRead()) {
        this.sourceDir = sourceDir;
      } else {
        throw new CompilationFailedException("source directory "
            + sourceDir.getAbsolutePath()
            + " was not a directory, or was not readable");
      }
    }
    
    return sourceDir;
  }
  
  private Iterable<? extends File> getClassPath() {
    final List<File> classpath = new ArrayList<File>();
    
    jarPaths.add(DEFAULT_JARS_PATH);
    
    for (final String jarPath : jarPaths) {
      final File jarDir = new File(jarPath);
      Log.fine("Finding jars at " + jarDir.getAbsolutePath());
      
      if (jarDir.isDirectory()) {
        for (final File jar : jarDir.listFiles(JAR_FILTER)) {
          Log.finer("Found " + jar.getAbsolutePath());
          classpath.add(jar);
        }
      }
    }
    
    return classpath;
  }
  
  public Compilation sourcesFrom(final String sourcePath) {
    this.sourcePath = sourcePath;
    return this;
  }
  
  public Compilation to(final String destinationPath) {
    this.destinationPath = destinationPath;
    return this;
  }
  
  public Compilation useJarsAt(final String jarsAt) {
    Util.checkNulls(jarsAt);
    jarPaths.add(jarsAt);
    return this;
  }
  
  public Compilation disableDebug() {
    disableDebug = true;
    return this;
  }
}
