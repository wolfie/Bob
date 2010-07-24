package com.github.wolfie.bob.action.optional;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.taskdefs.PumpStreamHandler;

import com.github.wolfie.bob.Util;
import com.github.wolfie.bob.exception.BobRuntimeException;

public class JavaLauncher {
  
  private final static String CLASSPATH_SEPARATOR = System
      .getProperty("path.separator");
  
  private final Class<?> classToRun;
  private final Set<String> userClassPaths = new HashSet<String>();
  private final Set<String> userClassPathsForced = new HashSet<String>();
  private final Set<String> classesToLoad = new HashSet<String>();
  
  /**
   * userJarPath &rarr; classloader containing it.
   * 
   * @deprecated don't use this field directly. Use
   *             {@link #getUserClassLoaderCache()} instead.
   */
  @Deprecated
  private final Map<String, ClassLoader> userClassLoaderCache = new HashMap<String, ClassLoader>();
  private boolean userClassLoaderCacheIsInitialized = false;
  
  /**
   * bobJarPath &rarr; classloader containing it.
   * 
   * @deprecated don't use this field directly. Use
   *             {@link #getBobClassLoaderCache()} instead.
   */
  @Deprecated
  private final Map<String, ClassLoader> bobClassLoaderCache = new HashMap<String, ClassLoader>();
  private boolean bobClassLoaderCacheIsInitialized = false;
  
  private final List<String> givenAppArgs = new ArrayList<String>();
  private final List<String> givenJvmArgs = new ArrayList<String>();
  
  public JavaLauncher(final Class<?> classToRun) {
    Util.checkNulls(classToRun);
    this.classToRun = classToRun;
    
    ensureClassCanBeLoaded(classToRun.getName());
  }
  
  /**
   * Defines a user-given path to a classpath that should (but is not guaranteed
   * to) exist.
   * <p/>
   * A classpath entry might be either a directory containing classes, or a jar
   * file.
   * <p/>
   * This method can be called many times to add the amount of classpath entries
   * provided by the user.
   * 
   * @return <code>this</code>
   */
  public JavaLauncher userProvidedClassPath(final String classPath) {
    Util.checkNulls(classPath);
    userClassPaths.add(classPath);
    return this;
  }
  
  /**
   * Add classpath entries that will be put as-is into the launched JVM's
   * classpath, without any checks being made.
   * <p/>
   * Without good reason, you should use {@link #userProvidedClassPath(String)}
   * instead.
   */
  public JavaLauncher userProvidedForcedClassPath(final String classPath) {
    Util.checkNulls(classPath);
    userClassPathsForced.add(classPath);
    return this;
  }
  
  /**
   * Ensure that, before the java code is run, the given class can be loaded.
   * <p/>
   * This method can be called many times to add many classes that must be
   * loadable. These checks are made in the {@link #run()} method.
   * 
   * @return <code>this</code>
   */
  public JavaLauncher ensureClassCanBeLoaded(
      final String fullyQualifiedClassName) {
    Util.checkNulls(fullyQualifiedClassName);
    classesToLoad.add(fullyQualifiedClassName);
    return this;
  }
  
  public int run() throws IOException {
    final String[] cmd = getCommands();
    
    for (final String string : cmd) {
      System.out.println(string);
    }
    
    final Process process = Runtime.getRuntime().exec(cmd, null, null);
    final PumpStreamHandler streamHandler = setupPumpStreamHandler(process);
    
    try {
      process.waitFor();
    } catch (final InterruptedException e) {
      process.destroy();
    }
    
    streamHandler.stop();
    
    return process.exitValue();
  }
  
  private PumpStreamHandler setupPumpStreamHandler(final Process process) {
    final PumpStreamHandler streamHandler = new PumpStreamHandler();
    streamHandler.setProcessErrorStream(process.getErrorStream());
    streamHandler.setProcessOutputStream(process.getInputStream());
    streamHandler.setProcessInputStream(process.getOutputStream());
    streamHandler.start();
    return streamHandler;
  }
  
  private String[] getCommands() {
    final String jreCommand = getJreCommand();
    final String classPathArgument = getClassPathArgument();
    final String executableClassName = classToRun.getName();
    
    final List<String> cmdList = new ArrayList<String>();
    cmdList.add(jreCommand);
    
    if (!classPathArgument.isEmpty()) {
      cmdList.add("-classpath");
      cmdList.add(classPathArgument);
    }
    
    cmdList.addAll(givenJvmArgs);
    cmdList.add(executableClassName);
    cmdList.addAll(givenAppArgs);
    
    final String[] cmd = cmdList.toArray(new String[cmdList.size()]);
    return cmd;
  }
  
  private String getJreCommand() {
    // rely for it being in the path
    
    String command = "java";
    
    if (Util.systemIsWindows()) {
      command += ".exe";
    }
    
    return command;
  }
  
  private String getClassPathArgument() {
    
    final Set<String> classPath = new HashSet<String>();
    classPath.addAll(getJarsToAddToClasspath());
    classPath.addAll(userClassPathsForced);
    
    return Util.implode(CLASSPATH_SEPARATOR, classPath);
  }
  
  private Set<String> getJarsToAddToClasspath() {
    final Set<String> jarsToAddToClasspath = new HashSet<String>();
    final Set<String> classesUnableToLoad = new HashSet<String>();
    
    for (final String classNameToLoad : classesToLoad) {
      try {
        final String jarPath = getJarContainingClass(classNameToLoad);
        jarsToAddToClasspath.add(jarPath);
      } catch (final ClassNotFoundException e2) {
        // the jar couldn't be found in any of the jar files.
        classesUnableToLoad.add(classNameToLoad);
      }
    }
    
    if (classesUnableToLoad.isEmpty()) {
      return jarsToAddToClasspath;
    } else {
      throw new BobRuntimeException(
          "The following class(es) couldn't be loaded: "
              + Util.implode(", ", classesUnableToLoad) + "\n"
              + "Searched the following files: "
              + Util.implode(", ", userClassPaths));
    }
  }
  
  private String getJarContainingClass(final String classNameToLoad)
      throws ClassNotFoundException {
    
    String jarPath = findClassFromJars(classNameToLoad,
        getUserClassLoaderCache());
    if (jarPath == null) {
      jarPath = findClassFromJars(classNameToLoad, getBobClassLoaderCache());
    }
    
    if (jarPath != null) {
      return jarPath;
    } else {
      throw new ClassNotFoundException(classNameToLoad + " could not be loaded");
    }
  }
  
  private String findClassFromJars(final String classNameToLoad,
      final Map<String, ClassLoader> cache) {
    
    System.out.println("Searching for " + classNameToLoad);
    for (final Map.Entry<String, ClassLoader> entry : cache.entrySet()) {
      try {
        final ClassLoader classLoader = entry.getValue();
        
        System.out.println("Trying " + entry.getKey());
        
        classLoader.loadClass(classNameToLoad);
        
        System.out.println("Found it!");
        // it was loaded, since no exception was thrown. Return the jar name.
        return entry.getKey();
        
      } catch (final ClassNotFoundException e) {
        System.out.println("Nope...");
        // ignore, just try the next classloader.
      }
    }
    return null;
  }
  
  /**
   * Returns the {@link #userClassLoaderCache}. If it isn't initialized before,
   * it will be before returned.
   */
  private Map<String, ClassLoader> getUserClassLoaderCache() {
    if (!userClassLoaderCacheIsInitialized) {
      userClassLoaderCacheIsInitialized = true;
      initializeClassLoaderCache(userClassLoaderCache, userClassPaths);
    }
    
    return userClassLoaderCache;
  }
  
  private Map<String, ClassLoader> getBobClassLoaderCache() {
    if (!bobClassLoaderCacheIsInitialized) {
      bobClassLoaderCacheIsInitialized = true;
      initializeClassLoaderCache(bobClassLoaderCache, getBobLibJarPaths());
    }
    
    return bobClassLoaderCache;
  }
  
  private Iterable<String> getBobLibJarPaths() {
    
    final String bobLibPath = System.getenv("BOB_LIB");
    if (bobLibPath != null && !bobLibPath.isEmpty()) {
      
      final File file = new File(bobLibPath);
      if (file.isDirectory()) {
        final File[] jarFiles = file.listFiles(new FileFilter() {
          @Override
          public boolean accept(final File pathname) {
            return pathname.isFile()
                && pathname.getName().toLowerCase().endsWith(".jar");
          }
        });
        
        final Set<String> jarPaths = new HashSet<String>();
        for (final File jarFile : jarFiles) {
          jarPaths.add(jarFile.getAbsolutePath());
        }
        return jarPaths;
        
      } else {
        System.err.println("The BOB_LIB path, " + file.getAbsolutePath()
            + ", is not an existing directory!");
      }
    } else {
      System.err.println("BOB_LIB environment variable is unset!");
    }
    
    return Collections.emptySet();
  }
  
  private void initializeClassLoaderCache(
      final Map<String, ClassLoader> cacheMap, final Iterable<String> jarPaths) {
    for (final String jarPath : jarPaths) {
      
      final File jarFile = new File(jarPath);
      if (jarFile.exists()) {
        
        try {
          final URL jarUrl = jarFile.toURI().toURL();
          final URL[] jarUriAsArray = new URL[] { jarUrl };
          cacheMap.put(jarPath, new URLClassLoader(jarUriAsArray, null));
        } catch (final MalformedURLException e) {
          throw new BobRuntimeException("This shouldn't happen", e);
        }
        
      } else {
        System.err.println("File \"" + jarPath + "\" doesn't exist");
      }
    }
  }
  
  public JavaLauncher addAppArg(final String arg) {
    givenAppArgs.add(arg);
    return this;
  }
  
  public JavaLauncher addAppArgs(final Collection<String> args) {
    givenAppArgs.addAll(args);
    return this;
  }
  
  public JavaLauncher addJvmArg(final String arg) {
    givenJvmArgs.add(arg);
    return this;
  }
  
  public JavaLauncher addJvmArgs(final Collection<String> args) {
    givenJvmArgs.addAll(args);
    return this;
  }
  
  @Override
  public String toString() {
    return Util.implode(" ", (Object[]) getCommands());
  }
  
  public void addAppArgs(final String[] rawArgs) {
    addAppArgs(Arrays.asList(rawArgs));
  }
}
