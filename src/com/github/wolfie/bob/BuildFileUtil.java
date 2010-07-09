package com.github.wolfie.bob;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.github.wolfie.bob.exception.BobRuntimeException;

public class BuildFileUtil {
  private static final String DEFAULT_TARGET_SYMBOL = "(default)";
  
  private static final class TargetInfo {
    private final String name;
    private final boolean defaultTarget;
    
    public static final Comparator<TargetInfo> BY_NAME = new Comparator<BuildFileUtil.TargetInfo>() {
      @Override
      public int compare(final TargetInfo o1, final TargetInfo o2) {
        return o1.name.compareTo(o2.name);
      }
    };
    
    public TargetInfo(final String name, final boolean defaultTarget) {
      this.name = name;
      this.defaultTarget = defaultTarget;
    }
    
    public String getName() {
      return name;
    }
    
    public boolean isDefaultTarget() {
      return defaultTarget;
    }
    
    @Override
    public String toString() {
      return name;
    }
    
    public static void print(final Collection<TargetInfo> targetInfo,
        final PrintStream stream) {
      final List<TargetInfo> myTargetInfo = new ArrayList<TargetInfo>(
          targetInfo);
      
      TargetInfo defaultName = null;
      TargetInfo defaultTarget = null;
      
      for (final TargetInfo aTargetInfo : targetInfo) {
        if (aTargetInfo.isDefaultTarget()) {
          if (defaultTarget != null) {
            throw new IllegalStateException(
                "Noticed at least two defaultTarget-annotated targets: "
                    + defaultTarget + " and " + aTargetInfo);
          } else {
            defaultTarget = aTargetInfo;
          }
        } else if (aTargetInfo.getName().equals(
            Defaults.DEFAULT_BUILD_METHOD_NAME)) {
          if (defaultName != null) {
            throw new IllegalStateException(
                "Noticed at least two methods named "
                    + Defaults.DEFAULT_BUILD_METHOD_NAME);
          } else {
            defaultName = aTargetInfo;
          }
        }
        
        myTargetInfo.add(aTargetInfo);
      }
      
      Collections.sort(myTargetInfo, TargetInfo.BY_NAME);
      
      if (defaultTarget != null) {
        stream.println(defaultTarget + " " + DEFAULT_TARGET_SYMBOL);
        myTargetInfo.remove(defaultTarget);
      } else if (defaultName != null) {
        stream.println(defaultName + " " + DEFAULT_TARGET_SYMBOL);
        myTargetInfo.remove(defaultName);
      }
      
      for (final TargetInfo aTargetInfo : myTargetInfo) {
        stream.println(aTargetInfo);
      }
    }
  }
  
  private static final String DESCRIBE_PROJECT_METHOD_NAME = "describeProject";
  
  private BuildFileUtil() {
  }
  
  private static File getDescriptionMethodClassFile(final File buildFile)
      throws IOException {
    final File sourceFile = getDescriptionMethodSourceFile(buildFile);
    return compile(sourceFile);
  }
  
  private static File compile(final File sourceFile) {
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    
    final DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
    
    final StandardJavaFileManager fileManager = compiler
        .getStandardFileManager(diagnosticCollector, null, null);
    final Iterable<? extends JavaFileObject> javaFileObjects = fileManager
        .getJavaFileObjects(sourceFile);
    
    compiler.getTask(null, fileManager, diagnosticCollector, null, null,
        javaFileObjects).call();
    
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticCollector
        .getDiagnostics();
    if (diagnostics.isEmpty()) {
      final String classFileName = sourceFile.getAbsolutePath().replaceAll(
          ".java$", ".class");
      final File classFile = new File(classFileName);
      if (classFile.exists()) {
        return classFile;
      } else {
        System.err.println("class file not found: " + classFileName);
        return null;
      }
    }
    
    for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
      System.err.println(diagnostic.toString());
    }
    
    return null;
  }
  
  private static File getDescriptionMethodSourceFile(final File buildFile)
      throws IOException {
    final String methodBodyString = getDescriptionMethod(buildFile);
    
    final File methodSourceFile = File.createTempFile("BobBuildDesc", ".java");
    final String methodClassName = getClassName(methodSourceFile);
    
    final String classString = String.format(
        "import %s;\npublic class %s { %s }",
        ProjectDescription.class.getName(),
        methodClassName,
        methodBodyString);
    
    final FileWriter sourceWriter = new FileWriter(methodSourceFile);
    sourceWriter.write(classString);
    sourceWriter.close();
    return methodSourceFile;
  }
  
  private static String getClassName(final File methodSourceFile) {
    final String absolutePath = methodSourceFile.getAbsolutePath();
    final String parentPath = methodSourceFile.getParent();
    return absolutePath.substring(parentPath.length() + 1, absolutePath
        .lastIndexOf('.'));
  }
  
  private static String getDescriptionMethod(final File file)
      throws IOException {
    
    final String code = Util.getFileAsString(file);
    final String method = getMethodCode(code, DESCRIBE_PROJECT_METHOD_NAME);
    
    return method;
  }
  
  private static String getMethodCode(String code,
      final String methodName, final Class<?>... params) {
    
    code = removeComments(code);
    
    final String modifiers = "(?:(?:private|public|protected|static|final|abstract)\\s+)*";
    final String returnType = "\\p{Alnum}+\\s+";
    final String escapedMethodName = Pattern.quote(methodName);
    final String param = getParamsRegex(params);
    
    final String regex = modifiers + returnType + escapedMethodName
        + "\\(\\s*" + param + "\\)\\s*\\{";
    final Matcher matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(
        code);
    
    if (matcher.find()) {
      final String firstLine = matcher.group(0);
      final String body = getMethodBody(code, matcher.end());
      return firstLine + body;
    } else {
      return null;
    }
  }
  
  private static String removeComments(final String code) {
    char currentQuote = 0;
    final StringBuilder builder = new StringBuilder(code.length());
    
    try {
      for (int i = 0; i < code.length(); i++) {
        final char c = code.charAt(i);
        final char c2;
        
        if (i + 1 < code.length()) {
          c2 = code.charAt(i + 1);
        } else {
          c2 = 0;
        }
        
        if (c == '\'' || c == '"') {
          if (currentQuote == 0) {
            currentQuote = c;
          } else {
            currentQuote = 0;
          }
          builder.append(c);
        } else if (currentQuote == 0 && c == '/' && c2 == '/') {
          while (code.charAt(++i) != '\n') {
            // empty loop
          }
        } else if (currentQuote == 0 && c == '/' && c2 == '*') {
          while (!(code.charAt(++i) == '*' && code.charAt(i + 1) == '/')) {
            // empty loop
          }
          i++;
        } else {
          builder.append(c);
        }
      }
    } catch (final IndexOutOfBoundsException e) {
      e.printStackTrace();
    }
    
    return builder.toString();
  }
  
  private static String getMethodBody(final String code,
      final int methodBodyStart) {
    
    final StringBuilder builder = new StringBuilder();
    
    char currentQuote = 0;
    int braceStackDepth = 0;
    for (int i = methodBodyStart; i < code.length() && braceStackDepth >= 0; i++) {
      final char c = code.charAt(i);
      builder.append(c);
      
      if (currentQuote == 0) {
        if (c == '{') {
          braceStackDepth++;
        } else if (c == '}') {
          braceStackDepth--;
        } else if (c == '\'' || c == '"') {
          currentQuote = c;
        }
      } else {
        if (c == currentQuote) {
          currentQuote = 0;
        }
      }
    }
    
    return builder.toString();
  }
  
  private static String getParamsRegex(final Class<?>[] params) {
    if (params == null || params.length == 0) {
      return "";
    }
    
    final StringBuilder builder = new StringBuilder();
    for (final Class<?> clazz : params) {
      final String fullName = Pattern.quote(clazz.getName());
      final String simpleName = Pattern.quote(clazz.getSimpleName());
      final String regex = String.format("(?:(?:%s|%s)\\s+\\p{Alnum}+\\s+",
          fullName, simpleName);
      builder.append(regex);
    }
    
    return builder.toString();
  }
  
  public static ProjectDescription getProjectDescription(final File buildFile) {
    try {
      final File descClassFile = getDescriptionMethodClassFile(buildFile);
      final ClassLoader descClassLoader = BootClassLoader.get(descClassFile);
      final String descClassName = getClassName(descClassFile);
      final Class<?> descClass = descClassLoader.loadClass(descClassName);
      final Method method = descClass
          .getDeclaredMethod(DESCRIBE_PROJECT_METHOD_NAME);
      method.setAccessible(true);
      final Object instance = descClass.newInstance();
      final ProjectDescription description = (ProjectDescription) method
          .invoke(instance);
      return description;
      
    } catch (final IOException e) {
      throw new BobRuntimeException(e);
    } catch (final ClassNotFoundException e) {
      throw new BobRuntimeException(e);
    } catch (final SecurityException e) {
      throw new BobRuntimeException(e);
    } catch (final NoSuchMethodException e) {
      throw new BobRuntimeException(e);
    } catch (final InstantiationException e) {
      throw new BobRuntimeException(e);
    } catch (final IllegalAccessException e) {
      throw new BobRuntimeException(e);
    } catch (final IllegalArgumentException e) {
      throw new BobRuntimeException(e);
    } catch (final InvocationTargetException e) {
      throw new BobRuntimeException(e);
    }
  }
  
  public static Set<TargetInfo> getTargetNames(final File buildFile)
      throws IOException {
    
    final String code = Util.getFileAsString(buildFile);
    
    final Set<TargetInfo> targets = new HashSet<TargetInfo>();
    return targets;
  }
}
