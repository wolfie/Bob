package com.github.wolfie.bob;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.wolfie.bob.action.Action;
import com.github.wolfie.bob.annotation.Target;
import com.github.wolfie.bob.exception.BobRuntimeException;
import com.github.wolfie.bob.exception.BuildTargetException;
import com.github.wolfie.bob.exception.NotAReadableDirectoryException;

public class Util {
  public interface FilePredicate {
    boolean accept(File file);
  }
  
  public static FilePredicate HIDE_DOT_FILES = new FilePredicate() {
    @Override
    public boolean accept(final File file) {
      return !file.getName().startsWith(".");
    }
  };
  
  public static final FilePredicate JAVA_SOURCE_FILE = new FilePredicate() {
    @Override
    public boolean accept(final File file) {
      return file.getName().endsWith(".java");
    }
  };
  
  public static final FilePredicate JAVA_CLASS_FILE = new FilePredicate() {
    @Override
    public boolean accept(final File file) {
      return file.getName().endsWith(".class");
    }
  };
  
  public static String implode(final String glue, final Object... bits) {
    checkNulls(glue, bits);
    
    final StringBuilder builder = new StringBuilder();
    for (final Object bit : bits) {
      builder.append(bit.toString()).append(glue);
    }
    
    if (bits.length > 0) {
      // remove the last bit
      builder.delete(builder.length() - glue.length(), builder.length());
    }
    
    return builder.toString();
  }
  
  public static String implode(final String glue, final Collection<?> bits) {
    return implode(glue, bits.toArray());
  }
  
  public static File getTemporaryDirectory() throws IOException {
    final File tempDir = File.createTempFile("bob", null);
    
    // Delete autocreated temporary file, and try to recreate it as a
    // directory instead.
    if (!tempDir.delete() || !tempDir.mkdir()) {
      throw new IOException("could not create temporary directory "
          + tempDir.getAbsolutePath());
    }
    
    return tempDir;
  }
  
  /**
   * Verifies that no argument is <code>null</code>
   * 
   * @param args
   *          the arguments to check
   * @throws NullPointerException
   *           if any element in <tt>args</tt> is <code>null</code>.
   */
  public static void checkNulls(final Object... args) {
    for (int i = 0; i < args.length; i++) {
      if (args[i] == null) {
        throw new NullPointerException("Argument with index " + i + " was null");
      }
    }
  }
  
  /**
   * Gets all files underneath a directory matching a given predicate
   * 
   * @param baseDir
   *          The base directory in which to find the files.
   * @param predicate
   *          The predicate to fulfill.
   * @return All {@link File}s that match <tt>predicate</tt> and are in, or
   *         under, <tt>baseDir</tt>.
   */
  public static Collection<File> getFilesRecursively(final File baseDir,
      final FilePredicate predicate) {
    final Set<File> files = new HashSet<File>();
    
    for (final File file : baseDir.listFiles()) {
      if (file.isDirectory()) {
        files.addAll(getFilesRecursively(file, predicate));
      } else if (predicate == null || predicate.accept(file)) {
        
        if (HIDE_DOT_FILES.accept(file)) {
          files.add(file);
        }
      }
    }
    
    return files;
  }
  
  public static Collection<File> getFilesRecursively(final File baseDir) {
    return getFilesRecursively(baseDir, null);
  }
  
  // public static Collection<File> normalizeFilePaths(final File basePath,
  // final Collection<File> files) {
  // final List<File> result = new ArrayList<File>(files.size());
  // final String basePathName = basePath.getPath();
  //
  // for (final File file : files) {
  // if (file.getPath().startsWith(basePathName)) {
  // String filename = file.getPath().replace(basePathName, "");
  // if (filename.startsWith("/")) {
  // filename = filename.substring(1);
  // }
  // result.add(new File(basePath, filename));
  // } else {
  // result.add(file);
  // }
  // }
  //
  // return result;
  // }
  
  public static String relativeFileName(final File baseDir, final File file) {
    final String basePath = baseDir.getAbsolutePath();
    final String filePath = file.getAbsolutePath();
    
    if (baseDir.isDirectory() && file.isFile() && filePath.startsWith(basePath)) {
      // +1 to rid of starting '/'
      return filePath.substring(basePath.length() + 1);
    } else {
      return filePath.substring(1);
    }
  }
  
  /**
   * Verifies that the argument is a readable directory.
   * 
   * @param directory
   *          The {@link File} to check for.
   * @return <tt>directory</tt>
   * @throws NotAReadableDirectoryException
   *           if <tt>directory</tt> is not a directory or it is not readable.
   */
  public static File checkedDirectory(final File directory) {
    if (directory.isDirectory() && directory.canRead()) {
      return directory;
    } else {
      throw new NotAReadableDirectoryException(directory);
    }
  }
  
  /**
   * 
   * @param directory
   */
  public static void createDir(final File directory) {
    if (!directory.isDirectory()) {
      Log.finer("Creating directory " + directory.getAbsolutePath());
      final boolean success = directory.mkdir();
      
      if (!success) {
        throw new BobRuntimeException("Could not create directory "
            + directory.getAbsolutePath());
      }
    } else {
      if (directory.isFile()) {
        throw new BobRuntimeException("Could not create directory "
            + directory.getAbsolutePath()
            + " since it is an existing file.");
      } else {
        Log.info("Not creating " + directory.getAbsolutePath()
            + " since it already exists.");
      }
    }
  }
  
  /**
   * Delete a {@link File}. If it is a normal file, it will simply be deleted.
   * If it is a directory, it, and everything underneath, will recursively be
   * removed.
   * 
   * @param file
   *          The file to remove.
   */
  public static void delete(final File file) {
    if (file.exists()) {
      
      if (file.isDirectory()) {
        for (final File fileEntry : file.listFiles()) {
          delete(fileEntry);
        }
      }
      
      Log.finer("Deleting " + file.getAbsolutePath());
      final boolean success = file.delete();
      if (!success) {
        throw new BobRuntimeException("Could not delete "
            + file.getAbsolutePath());
      }
    } else {
      Log.info("Not removing " + file.getAbsolutePath()
          + " since it does not exist.");
    }
  }
  
  /**
   * Puts a value with a key into a map if the key is previously unset.
   * 
   * @param key
   * @param value
   * @param map
   * @return <code>true</code> if the item was successfully put, otherwise
   *         <code>false</code>
   */
  public static <K, V extends Object> boolean putIfNotExists(final K key,
      final V value, final Map<K, V> map) {
    if (!map.containsKey(key)) {
      map.put(key, value);
      return true;
    } else {
      return false;
    }
  }
  
  public static String wordWrap(final String string) {
    return wordWrap(string, 70);
  }
  
  public static String wordWrap(final String string, final int lineLength) {
    final String linePrefix;
    
    final Pattern pattern = Pattern.compile("(\\s*).*");
    final Matcher matcher = pattern.matcher(string);
    if (matcher.matches() && matcher.groupCount() == 1) {
      linePrefix = matcher.group(1);
    } else {
      linePrefix = "";
    }
    
    // highly unoptimized
    
    final List<String> lines = new ArrayList<String>();
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(linePrefix);
    
    for (final String word : string.trim().split("\\s")) {
      stringBuilder.append(word);
      
      if (stringBuilder.length() + word.length() > lineLength) {
        lines.add(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append(linePrefix);
      }

      else {
        stringBuilder.append(" ");
      }
      
    }
    
    final String lastString = stringBuilder.toString().trim();
    if (!lastString.isEmpty()) {
      lines.add(linePrefix + lastString);
    }
    
    return implode("\n", lines);
  }
  
  public static boolean isAnyOf(final String needle, final String... haystack) {
    
    checkNulls(needle, haystack);
    
    for (final String option : haystack) {
      if (option.equals(needle)) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * <p>
   * Verifies that a given method fulfills all the requirements of a valid build
   * target
   * </p>
   * 
   * <p>
   * The method must
   * </p>
   * <ul>
   * <li>be public</li>
   * <li>be annotated with {@link com.github.wolfie.bob.annotation.Target}</li>
   * <li>not accept any parameters</li>
   * <li>return {@link com.github.wolfie.bob.action.Action} or its subclass</li>
   * </ul>
   * 
   * @param method
   *          The method to check
   * @return <tt>method</tt>
   * @throws BuildTargetException
   *           if any of the requirements are not met, with a description of the
   *           problem in the message.
   */
  public static Method verifyBuildTargetMethod(final Method method) {
    if (!Modifier.isPublic(method.getModifiers())) {
      throw new BuildTargetException("Method " + method + " is not public.");
    } else if (!method.isAnnotationPresent(Target.class)) {
      throw new BuildTargetException("Method " + method
          + " is not annotated with @" + Target.class.getName() + ".");
    } else if (method.getParameterTypes().length != 0) {
      throw new BuildTargetException("Method " + method
          + " accepts parameters, which it may not do.");
    } else if (!Action.class.isAssignableFrom(method.getReturnType())) {
      throw new BuildTargetException("Method " + method
          + " does not return an instance of " + Action.class.getName()
          + " or one of its subclasses.");
    }
    
    return method;
  }
  
  public static boolean systemIsWindows() {
    return System.getProperty("os.name").startsWith("Windows");
  }
}
