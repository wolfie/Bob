package com.github.wolfie.bob;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.github.wolfie.bob.annotation.Target;
import com.github.wolfie.bob.exception.NoBuildDirectoryFoundException;
import com.github.wolfie.bob.exception.NoBuildFileFoundException;
import com.github.wolfie.bob.exception.SeveralDefaultBuildTargetMethodsFoundException;

/**
 * Non-XML Builder
 * 
 * @author Henrik Paul
 * @since 1.0.0
 */
public class Bob {
  private static final String DEFAULT_BUILD_SRC_DIR = "build";
  private static final String DEFAULT_BUILD_SRC_FILE = "Default.java";
  private static final String DEFAULT_BUILD_METHOD_NAME = "build";
  
  private final String sourceDirectory = DEFAULT_BUILD_SRC_DIR;
  private final String sourceFile = DEFAULT_BUILD_SRC_FILE;
  
  public static final void main(final String[] args) {
    new Bob().run(args);
  }
  
  private final void run(final String[] args) {
    handleArgs(args);
    
    try {
      final File buildFile = getBuildFile(getBuildDirectory());
      final File buildClassFile = compile(buildFile);
      
      ClassLoader.getSystemClassLoader();
      final URLClassLoader urlClassLoader = new URLClassLoader(
          new URL[] { buildClassFile.toURI().toURL() });
      final Class<?> buildClass = urlClassLoader.loadClass(getBuildClassName());
      
      final Method buildMethod = getBuildMethod(buildClass);
      
      try {
        buildMethod.invoke(null);
      } catch (final NullPointerException e) {
        // as per javadoc, the method was not static.
        
        try {
          final Object buildObject = buildClass.newInstance();
          buildMethod.invoke(buildObject);
        } catch (final InstantiationException e2) {
          System.err.println(buildMethod + " is an instance method, but the"
              + " class doesn't have a public default constructor.");
          e2.printStackTrace();
        } catch (final IllegalAccessException e2) {
          System.err.println(buildMethod + " is an instance method, but the"
              + " class doesn't have a public default constructor.");
          e2.printStackTrace();
        }
      } catch (final IllegalArgumentException e) {
        System.err.println(buildMethod.getName() + " was annotated with @"
            + Target.class.getName() + ", but the method requires arguments, "
            + "which it may not do.");
        e.printStackTrace();
      } catch (final IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (final NoBuildDirectoryFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final NoBuildFileFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final CompilationFailedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final NoBuildTargetMethodFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final InvocationTargetException e) {
      System.err.println("The build method threw unhandled exceptions. "
          + "See below for more details.");
      e.printStackTrace();
    } catch (final SeveralDefaultBuildTargetMethodsFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  
  /**
   * <p>
   * Get the build method to call in the build class file.
   * </p>
   * 
   * <p>
   * If nothing else is specified, the chosen method will be, in the following
   * priority:
   * </p>
   * 
   * <ol>
   * <li>A method annotated as "
   * <code>@{@link Target}(defaultTarget = true)</code>"</li>
   * <li>The method <tt>build()</tt>, if appropriately annotated with
   * {@link Target}.
   * </ol>
   * 
   * @param buildClass
   *          The {@link Class} in which to look for said methods
   * @return The build {@link Method}.
   * @throws NoBuildTargetMethodFoundException
   *           If no suitable build method is found in <tt>buildClass</tt>.
   * @throws SeveralDefaultBuildTargetMethodsFoundException
   *           If <tt>buildClass</tt> has more than one method annotated as a
   *           default target.
   */
  private static Method getBuildMethod(final Class<?> buildClass)
      throws NoBuildTargetMethodFoundException,
      SeveralDefaultBuildTargetMethodsFoundException {
    
    // TODO: make command-line target invocation possible.
    
    Method defaultAnnotatedMethod = null;
    Method defaultNameMethod = null;
    
    for (final Method method : buildClass.getMethods()) {
      
      final Target targetAnnotation = method.getAnnotation(Target.class);
      if (targetAnnotation != null) {
        if (targetAnnotation.defaultTarget()) {
          
          if (defaultAnnotatedMethod == null) {
            defaultAnnotatedMethod = method;
          } else {
            throw new SeveralDefaultBuildTargetMethodsFoundException(buildClass);
          }
          
        } else if (method.getName().equals(DEFAULT_BUILD_METHOD_NAME)) {
          defaultNameMethod = method;
        }
      }
    }
    
    if (defaultAnnotatedMethod != null) {
      return defaultAnnotatedMethod;
    } else if (defaultNameMethod != null) {
      return defaultNameMethod;
    } else {
      throw new NoBuildTargetMethodFoundException(buildClass);
    }
  }
  
  private static String getBuildClassName() {
    // FIXME: take arguments into account.
    return "Default";
  }
  
  private static File compile(final File buildFile)
      throws CompilationFailedException {
    
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    
    try {
      final File tempDir = File.createTempFile("bob", null);
      tempDir.delete();
      if (!tempDir.mkdir()) {
        throw new CompilationFailedException(
            "could not create temporary directory " + tempDir.getAbsolutePath());
      }
      
      final DiagnosticCollector<JavaFileObject> diagnosticListener = new DiagnosticCollector<JavaFileObject>();
      final StandardJavaFileManager fileManager = compiler
          .getStandardFileManager(diagnosticListener, null, null);
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections
          .singleton(tempDir));
      
      final Iterable<? extends JavaFileObject> javaFiles = fileManager
          .getJavaFileObjects(buildFile);
      
      compiler.getTask(null, fileManager, diagnosticListener, null, null,
          javaFiles).call();
      
      final List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticListener
          .getDiagnostics();
      if (diagnostics.isEmpty()) {
        return tempDir;
      } else {
        final StringBuilder causeBuilder = new StringBuilder(
            "The following halted compilation:\n");
        
        for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
          causeBuilder.append(String.format("%s! %s%n", diagnostic.getKind(),
              diagnostic.getMessage(null)));
        }
        
        throw new CompilationFailedException(causeBuilder.toString());
      }
    } catch (final IOException e) {
      throw new CompilationFailedException(e);
    }
    
  }
  
  /**
   * <p>
   * Search for the source file for the build class in a directory.
   * </p>
   * 
   * @param buildDirectory
   *          A {@link File} object representing the directory in which a build
   *          file should be found at.
   * @return The build source file as a {@link File}
   * @throws NoBuildFileFoundException
   *           if no build file was found directly under <tt>buildDirectory</tt>
   *           , or <tt>buildDirectory</tt> is not a directory.
   */
  private File getBuildFile(final File buildDirectory)
      throws NoBuildFileFoundException {
    if (buildDirectory != null && buildDirectory.isDirectory()) {
      final List<File> files = Arrays.asList(buildDirectory.listFiles());
      for (final File file : files) {
        if (file.getName().equals(sourceFile)) {
          return file;
        }
      }
    }
    
    throw new NoBuildFileFoundException(buildDirectory);
  }
  
  /**
   * Search for the directory in which the build file should be located.
   * 
   * @return The directory in which the build file should be as a {@link File}.
   * @throws NoBuildDirectoryFoundException
   *           When no build directory was found.
   */
  private File getBuildDirectory() throws NoBuildDirectoryFoundException {
    final File buildSrcDir = new File(sourceDirectory);
    if (buildSrcDir.isDirectory()) {
      return buildSrcDir;
    }
    
    throw new NoBuildDirectoryFoundException();
  }
  
  private static void handleArgs(final String[] args) {
    for (final String arg : args) {
      System.out.println(arg);
    }
  }
}
