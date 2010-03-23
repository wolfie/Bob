package com.github.wolfie.bob.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
import com.github.wolfie.bob.exception.DestinationNotSetException;

public class Compile extends AbstractArtifact implements HasSourceDirectory {
  
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
  
  private File sourceDirectory = new File("src");
  private File cachedDestination = null;
  
  @Override
  public void process() {
    final File sourceDir = getSourceDirectory();
    
    if (sourceDir.isDirectory()) {
      Log.fine("Finding source files from " + sourceDir.getPath());
      
      final Collection<File> javaFiles = Util.getFilesRecursively(sourceDir,
          Util.JAVA_SOURCE_FILE);
      
      for (final File file : javaFiles) {
        Log.finer("Found file " + file.getPath());
      }
      
      final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      final StandardJavaFileManager fileManager = compiler
          .getStandardFileManager(null, null, null);
      
      final File destination = getDestination();
      
      try {
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections
            .singleton(destination));
      } catch (final IOException e) {
        throw new CompilationFailedException(e);
      }
      
      final Iterable<? extends JavaFileObject> javaFileObjects = fileManager
          .getJavaFileObjectsFromFiles(javaFiles);
      
      final BobDiagnosticListener diagnosticListener = new BobDiagnosticListener();
      compiler.getTask(null, fileManager, diagnosticListener, null, null,
          javaFileObjects).call();
      
      if (diagnosticListener.hasProblems()) {
        final StringBuilder builder = new StringBuilder();
        for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnosticListener
            .getProblems()) {
          builder.append(diagnostic.getMessage(null));
        }
        throw new CompilationFailedException(builder.toString());
      }
      
    } else {
      throw new IllegalArgumentException("Source directory, \""
          + sourceDir.getAbsolutePath() + "\", is not a directory.");
    }
  }
  
  @Override
  public Compile setSourceDirectory(final File sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
    return this;
  }
  
  @Override
  public File getSourceDirectory() {
    return sourceDirectory;
  }
  
  /**
   * {@inheritDoc}
   * 
   * <p>
   * <em>Note:</em> once this method has been called, neither
   * {@link #setDestination(File)} nor {@link #setDestinationForceCreate(File)}
   * may be called.
   * </p>
   */
  @Override
  public File getDestination() {
    if (cachedDestination == null) {
      File destination = null;
      try {
        destination = super.getDestination();
        if (!destination.isDirectory()) {
          throw new IllegalArgumentException("Destination directory, \""
              + destination + "\", is not a directory.");
        }
      } catch (final DestinationNotSetException e) {
        try {
          destination = Util.getTemporaryDirectory();
        } catch (final IOException e1) {
          throw new CompilationFailedException(
              "Could not create temporary directory.", e1);
        }
      }
      cachedDestination = destination;
    }
    
    if (cachedDestination != null) {
      return cachedDestination;
    } else {
      throw new NullPointerException(
          "Internal integrity error: destination should never become null, but now it is.");
    }
  }
  
  /**
   * {@inheritDoc}
   * 
   * @throws IllegalStateException
   *           if {@link #getDestination()} is called prior.
   */
  @Override
  public Compile setDestination(final File directory) {
    if (cachedDestination == null) {
      super.setDestination(directory);
    } else {
      throw new IllegalStateException("Cannot set the destination "
          + "after it has been retrieved.");
    }
    
    return this;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @throws IllegalStateException
   *           if {@link #getDestination()} is called prior.
   */
  @Override
  public Compile setDestinationForceCreate(final File directory) {
    if (cachedDestination == null) {
      super.setDestinationForceCreate(directory);
    } else {
      throw new IllegalStateException("Cannot set the destination "
          + "after it has been retrieved.");
    }
    
    return this;
  }
}
