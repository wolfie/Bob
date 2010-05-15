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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.github.wolfie.bob.action.Action;
import com.github.wolfie.bob.annotation.Target;
import com.github.wolfie.bob.exception.BuildTargetException;
import com.github.wolfie.bob.exception.CompilationFailedException;
import com.github.wolfie.bob.exception.IncompatibleReturnTypeException;
import com.github.wolfie.bob.exception.NoBuildFileFoundException;
import com.github.wolfie.bob.exception.NoDefaultBuildTargetMethodFoundException;
import com.github.wolfie.bob.exception.SeveralDefaultBuildTargetMethodsFoundException;
import com.github.wolfie.bob.exception.UnexpectedArgumentAmountException;
import com.github.wolfie.bob.exception.UnrecognizedArgumentException;

/**
 * Non-XML Builder
 * 
 * @author Henrik Paul
 * @since 1.0.0
 */
public class Bob {
  public static final int VERSION_MAJOR = 0;
  public static final int VERSION_MINOR = 0;
  public static final int VERSION_MAINTENANCE = 0;
  public static final String VERSION_BUILD = "pre-alpha";
  public static final String VERSION;
  
  private static boolean showHelp = false;
  private static boolean skipBuilding = false;
  private static boolean success = false;
  private static boolean listTargets = false;
  
  /**
   * A path to the desired build file. Guaranteed to have a non-
   * <code>null</code> value
   */
  private static String buildfile;
  
  /**
   * The name of the desired build method. Can be <code>null</code>, which means
   * a suitable default target needs to be found manually.
   */
  private static String buildtarget = null;
  
  static {
    if (VERSION_BUILD != null && !VERSION_BUILD.isEmpty()) {
      VERSION = String.format("%s.%s.%s-%s", VERSION_MAJOR, VERSION_MINOR,
          VERSION_MAINTENANCE, VERSION_BUILD);
    } else {
      VERSION = String.format("%s.%s.%s", VERSION_MAJOR, VERSION_MINOR,
          VERSION_MAINTENANCE);
    }
  }
  
  public static final void main(final String[] args) {
    try {
      handleArgs(args);
      
      if (!skipBuilding) {
        run();
      }
      
      if (showHelp) {
        showHelp();
      }
      
      if (listTargets) {
        listTargets();
      }
      
    } catch (final NoBuildFileFoundException e) {
      Log.severe(e.getMessage());
      Log.severe("Are you sure you're in the right directory?");
    } catch (final UnrecognizedArgumentException e) {
      Log.severe(e.getMessage());
    } catch (final UnexpectedArgumentAmountException e) {
      Log.severe(e.getMessage());
    } catch (final Exception e) {
      // catch-all last resort
      Log.severe(e.getMessage());
    }
    
    if (!skipBuilding) {
      if (success) {
        Log.info("Build successful");
      } else {
        Log.severe("Build FAILED!");
      }
    } else {
      Log.fine("Didn't build anything");
    }
  }
  
  private static final void run() {
    try {
      final File buildFile = getBuildFile();
      final File buildClassFile = compile(buildFile);
      
      final URLClassLoader urlClassLoader = new URLClassLoader(
          new URL[] { buildClassFile.toURI().toURL() });
      final Class<?> buildClass = urlClassLoader.loadClass(getBuildClassName());
      
      final Method buildMethod = getBuildMethod(buildClass);
      
      Action result = null;
      try {
        result = (Action) buildMethod.invoke(null);
        execute(result, buildClass, buildMethod);
      } catch (final NullPointerException e) {
        // as per javadoc, the method was not static.
        
        try {
          final Object buildObject = buildClass.newInstance();
          result = (Action) buildMethod.invoke(buildObject);
          execute(result, buildClass, buildMethod);
          
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
    } catch (final MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final InvocationTargetException e) {
      System.err.println("The build method threw unhandled exceptions. "
          + "See below for more details.");
      e.printStackTrace();
    }
  }
  
  private static void execute(final Action result, final Class<?> buildClass,
      final Method buildMethod) {
    if (result != null) {
      result.process();
      success = true;
    } else {
      throw new NullPointerException(String.format("%s.%s() returned null.",
          buildClass.getName(), buildMethod.getName()));
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
   * @throws NoDefaultBuildTargetMethodFoundException
   *           If no suitable build method is found in <tt>buildClass</tt>.
   * @throws SeveralDefaultBuildTargetMethodsFoundException
   *           If <tt>buildClass</tt> has more than one method annotated as a
   *           default target.
   * @throws IncompatibleReturnTypeException
   *           If any defined build target method in <tt>buildClass</tt> returns
   *           something other than an implementation of {@link Action}.
   */
  private static Method getBuildMethod(final Class<?> buildClass)
      throws NoDefaultBuildTargetMethodFoundException,
      SeveralDefaultBuildTargetMethodsFoundException {
    
    final Method method;
    if (buildtarget != null) {
      method = getBuildTarget(buildClass, buildtarget);
    } else {
      method = getDefaultBuildTarget(buildClass);
    }
    
    return Util.verifyBuildTargetMethod(method);
  }
  
  private static Method getBuildTarget(final Class<?> buildClass,
      final String buildtarget) {
    try {
      return buildClass.getMethod(buildtarget);
    } catch (final SecurityException e) {
      throw new BuildTargetException("The JVM didn't allow access to target "
          + buildtarget + ".", e);
    } catch (final NoSuchMethodException e) {
      throw new BuildTargetException("No target by the name " + buildtarget
          + " was found", e);
    }
  }
  
  private static Method getDefaultBuildTarget(final Class<?> buildClass) {
    Method buildMethod = null;
    
    for (final Method method : buildClass.getMethods()) {
      
      final Target targetAnnotation = method.getAnnotation(Target.class);
      if (targetAnnotation != null) {
        if (targetAnnotation.defaultTarget()) {
          
          if (buildMethod == null) {
            buildMethod = method;
          } else {
            throw new SeveralDefaultBuildTargetMethodsFoundException(buildClass);
          }
        }
        
      }
    }
    
    if (buildMethod != null) {
      return buildMethod;
    } else {
      throw new NoDefaultBuildTargetMethodFoundException(buildClass);
    }
  }
  
  private static String getBuildClassName() {
    return buildfile.substring(buildfile.lastIndexOf(File.separator) + 1,
        buildfile.lastIndexOf("."));
  }
  
  private static File compile(final File buildFile)
      throws CompilationFailedException {
    
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    
    try {
      final File tempDir = Util.getTemporaryDirectory();
      
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
  
  private static File getBuildFile() throws NoBuildFileFoundException {
    final File file = new File(buildfile);
    if (file.canRead()) {
      return file;
    } else {
      throw new NoBuildFileFoundException(buildfile);
    }
  }
  
  private static void handleArgs(final String[] args) {
    final Queue<String> argQueue = new LinkedList<String>(Arrays.asList(args));
    
    while (!argQueue.isEmpty()) {
      final String arg;
      if (argQueue.peek().startsWith("-")) {
        arg = argQueue.remove();
      } else {
        break;
      }
      
      // LOGGING
      
      if (Util.isAnyOf(arg, "-v", "--verbose")) {
        Log.setLevel(Level.FINE);
        Log.fine("Verbose logging on");
      }

      else if (Util.isAnyOf(arg, "-vv", "--very-verbose")) {
        Log.setLevel(Level.FINER);
        Log.finer("Very verbose logging on");
      }

      else if (Util.isAnyOf(arg, "-s", "--silent")) {
        Log.setLevel(Level.WARNING);
      }

      else if (Util.isAnyOf(arg, "-ss", "--very-silent")) {
        Log.setLevel(Level.SEVERE);
      }

      else if (Util.isAnyOf(arg, "-l", "--list-targets")) {
        skipBuilding = true;
        listTargets = true;
      }

      // END LOGGING
      
      else if (Util.isAnyOf(arg, "-h", "--help")) {
        showHelp = true;
        skipBuilding = true;
      }

      else {
        showHelp = true;
        skipBuilding = true;
        throw new UnrecognizedArgumentException(arg);
      }
    }
    
    if (!argQueue.isEmpty()) {
      buildfile = argQueue.remove();
      Log.fine("Using " + buildfile + " as the buildfile");
    } else {
      buildfile = DefaultValues.DEFAULT_BUILD_SRC_PATH;
      Log.finer("Using the default " + buildfile + " as the buildfile");
    }
    
    if (!argQueue.isEmpty()) {
      buildtarget = argQueue.remove();
      Log.fine("Using \"" + buildtarget + "\" as the build target");
    }
    // else {} is handled at #getBuildMethod()
    
    if (!argQueue.isEmpty()) {
      showHelp = true;
      skipBuilding = true;
      throw new UnexpectedArgumentAmountException(argQueue.size(), 2);
    }
  }
  
  private static void showHelp() {
    System.out.println("Usage: bob [<options>] [<buildfile>] [<buildtarget>]");
    System.out.println();
    System.out.println("Options:");
    System.out.println(" -v, --verbose          show additional build info");
    System.out.println(" -vv, --more-verbose    show even more information");
    System.out.println(" -s, --silent           suppress build info");
    System.out.println(" -ss, --more-silent     suppress almost all info");
    System.out.println(" -h, --help             show this help");
    System.out.println();
    System.out.println(" -l, --list-targets     ");
    System.out.println(Util.wordWrap("        list targets in a buildfile. " +
                    "Any defined buildtarget will be ignored."));
    System.out.println();
    System.out.println("Buildfile:");
    System.out.println(Util.wordWrap("  The buildfile is where all "
        + "the build targets are located at. If this parameter "
        + "is omitted, a default value of \""
        + DefaultValues.DEFAULT_BUILD_SRC_DIR + File.separator
        + DefaultValues.DEFAULT_BUILD_SRC_FILE + "\" will " + "be used."));
    System.out.println();
    System.out.println("Buildtarget:");
    System.out.println(Util.wordWrap("  The buildtarget " + "is the method "
        + "within buildfile that will be invoked to create whatever "
        + "you wish to be created. If this parameter is "
        + "omitted, a default value of \""
        + DefaultValues.DEFAULT_BUILD_METHOD_NAME + "\" will be used."));
  }
  
  private static void listTargets() throws MalformedURLException,
      ClassNotFoundException {
    final File buildFile = getBuildFile();
    final File buildClassFile = compile(buildFile);
    final URLClassLoader urlClassLoader = new URLClassLoader(
        new URL[] { buildClassFile.toURI().toURL() });
    final Class<?> buildClass = urlClassLoader.loadClass(getBuildClassName());
    
    System.out.println("Valid build targets found in build file "
        + buildFile.getAbsolutePath() + ": \n");
    
    final Method defaultTarget = getDefaultBuildTarget(buildClass);
    
    for (final Method method : buildClass.getMethods()) {
      try {
        Util.verifyBuildTargetMethod(method);
        
        if (defaultTarget.equals(method)) {
          System.out.println(method.getName() + " [default]");
        } else {
          System.out.println(method.getName());
        }
      } catch (final BuildTargetException e) {
        // just ignore...
      }
    }
  }
  
  public static String getVersionString() {
    return "Bob " + VERSION;
  }
}
