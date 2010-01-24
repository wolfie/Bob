package com.github.wolfie.bob;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.github.wolfie.bob.annotation.Target;
import com.github.wolfie.bob.exception.BuildError;
import com.github.wolfie.bob.result.Action;

public abstract class Build {
  private static final FilenameFilter SOURCE_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(final File dir, final String name) {
      return name.endsWith(".java");
    }
  };
  
  private static final FileFilter DIRECTORY_FILTER = new FileFilter() {
    @Override
    public boolean accept(final File pathname) {
      return pathname.isDirectory();
    }
  };
  
  private static final DiagnosticListener<? super JavaFileObject> BUILD_DIAGNOSTIC_LISTENER = new DiagnosticListener<JavaFileObject>() {
    @Override
    public void report(final Diagnostic<? extends JavaFileObject> diagnostic) {
      System.out.println(diagnostic.getKind() + ": "
          + diagnostic.getMessage(null));
    }
  };
  
  private static final String DEFAULT_BUILD_CLASS_NAME = "Default";
  private static final String DEFAULT_BUILD_TARGET_NAME = "build";
  
  private static String buildClassName;
  private static String buildTargetName;
  
  private static final String TARGET_DIRECTORY_NAME = "result";
  private static final Class<?> BUILD_TARGET_RETURN_CLASS = Action.class;
  
  private static File tempDir;
  
  public final static void main(final String[] args) throws Exception {
    processArgs(args);
    
    // TODO: this isn't main-material, you silly nerd.
    final Set<File> files = getSourceFiles();
    tempDir = getTargetDirectory();
    compileFiles(files);
    
    final Class<? extends Build> buildClass = getBuildClass();
    final Method buildMethod = getBuildMethod(buildClass);
    final Action action = (Action) buildMethod.invoke(buildClass.newInstance());
    Log.info("Processing " + action.getClass().getSimpleName());
    action.process();
  }
  
  private static Method getBuildMethod(final Class<? extends Build> clazz) {
    final Set<Method> buildMethods = getBuildMethods(clazz);
    
    final String usedBuildTargetName = buildTargetName != null ? buildTargetName
        : DEFAULT_BUILD_TARGET_NAME;
    
    for (final Method method : buildMethods) {
      if (method.getName().equals(usedBuildTargetName)) {
        return method;
      }
    }
    
    throw new BuildError("Build target \"" + usedBuildTargetName
        + "\" not found in class " + clazz.getName());
  }
  
  private static Set<Method> getBuildMethods(final Class<? extends Build> clazz) {
    final Set<Method> methods = new HashSet<Method>();
    
    for (final Method method : clazz.getMethods()) {
      if (method.getAnnotation(Target.class) != null) {
        boolean isSuitableMethod = true;
        
        // Annotated method found. Let's do some sanity checks
        
        final Class<?> returnType = method.getReturnType();
        if (!returnType.equals(BUILD_TARGET_RETURN_CLASS)) {
          Log.warning("Method " + method.getName() + " in class "
              + clazz.getName() + " doesn't return "
              + BUILD_TARGET_RETURN_CLASS.getName() + " but "
              + returnType.getName());
          isSuitableMethod = false;
        }
        
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 0) {
          Log.warning("Method " + method.getName() + " in class "
              + clazz.getName() + " is annotated as a build target, "
              + "but requires arguments. This isn't allowed.");
          isSuitableMethod = false;
        }
        
        if (isSuitableMethod) {
          Log.fine("Found suitable target \"" + method.getName() + "\" in "
              + clazz.getName());
          methods.add(method);
        }
      }

      else if (method.getName().equals(DEFAULT_BUILD_TARGET_NAME)
          && method.getReturnType().equals(BUILD_TARGET_RETURN_CLASS)
          && method.getParameterTypes().length == 0) {
        
        /*
         * Nag about annotations. I think it's better to always to use
         * annotations instead of having exceptions
         */
        Log.warning("Default build target \"" + DEFAULT_BUILD_TARGET_NAME
            + "\" found in class " + clazz.getName()
            + ", but it was not properly annotated. Ignoring.");
      }
    }
    
    return methods;
  }
  
  private static Class<? extends Build> getBuildClass() {
    final String usedBuildClassName = buildClassName != null ? buildClassName
        : DEFAULT_BUILD_CLASS_NAME;
    
    try {
      final Class<?> forName = Class.forName(usedBuildClassName);
      if (Build.class.isAssignableFrom(forName)) {
        // We just checked for this, so it must be okay
        @SuppressWarnings("unchecked")
        final Class<? extends Build> buildClass = (Class<? extends Build>) forName;
        
        Log.fine("Using \"" + buildClass.getName() + "\" as build class");
        return buildClass;
      } else {
        throw new BuildError(usedBuildClassName
            + " is not an implementation of " + Build.class.getName());
      }
    } catch (final ClassNotFoundException e) {
      throw new BuildError(usedBuildClassName + " not found.", e);
    }
  }
  
  private static void processArgs(final String[] args) {
    // TODO
  }
  
  private static File getTargetDirectory() throws IOException {
    final File directory = new File(TARGET_DIRECTORY_NAME);
    if (directory.exists()) {
      if (directory.isDirectory()) {
        
      } else {
        throw new IOException("Cannot create target directory. A "
            + "non-directory file was found at \""
            + directory.getAbsolutePath() + "\" instead");
      }
    }
    
    directory.mkdir();
    return directory;
  }
  
  private static void compileFiles(final Set<File> files) throws IOException {
    if (files != null && !files.isEmpty()) {
      Log.info("Found the following files:\n" + Util.implode("\n", files));
      
      final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      
      final StandardJavaFileManager fileManager = compiler
          .getStandardFileManager(BUILD_DIAGNOSTIC_LISTENER, null, null);
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections
          .singleton(new File("result/")));
      
      final Iterable<? extends JavaFileObject> javaFiles = fileManager
          .getJavaFileObjectsFromFiles(files);
      final DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
      
      compiler.getTask(null, fileManager, diagnosticCollector, null, null,
          javaFiles).call();
      
      System.out.println("\nDiagnostics: ");
      for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnosticCollector
          .getDiagnostics()) {
        System.out.format("%s! %s%n", diagnostic.getKind(), diagnostic
            .getMessage(null));
        
      }
      if (diagnosticCollector.getDiagnostics().isEmpty()) {
        System.out.println("all ok.");
      }
      
      fileManager.close();
    } else {
      System.err.println("No files to compile found.");
    }
  }
  
  private static Set<File> getSourceFiles() {
    final File file = new File("src");
    if (file.isDirectory()) {
      return getSourceFilesRecursively(file);
    } else {
      System.out.println("src not found");
      return new HashSet<File>();
    }
  }
  
  private static Set<File> getSourceFilesRecursively(final File file) {
    if (file.isDirectory()) {
      final Set<File> files = new HashSet<File>();
      files.addAll(Arrays.asList(file.listFiles(SOURCE_FILTER)));
      for (final File dir : file.listFiles(DIRECTORY_FILTER)) {
        files.addAll(getSourceFilesRecursively(dir));
      }
      return files;
    } else {
      System.out.println(file + " is not a directory.");
      return new HashSet<File>();
    }
  }
  
  @Target
  public abstract Action build();
  
  public static File getDefaultBuildDirectory() {
    return new File(TARGET_DIRECTORY_NAME);
  }
}
