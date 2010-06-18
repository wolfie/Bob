package com.github.wolfie.bob.action.optional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.github.wolfie.bob.Util;

/**
 * A Java executable that runs an amount of JUnit tests.
 * <p/>
 * Arguments: <tt>[result_file_path] [junit_test_class ...]</tt>
 */
public class JUnitTestRunner {
  
  private static File resultsFile;
  
  public static void main(final String[] args) throws Exception {
    resultsFile = getResultsFileFromArgs(args);
    verifyResultsFileExists();
    
    final Class<?>[] classesToTest = getClassesToTestFromArgs(args);
    
    final Properties properties = new Properties();
    final Result results = JUnitCore.runClasses(classesToTest);
    
    properties.setProperty(JUnitTestResultConstants.SUCCESS,
        String.valueOf(results.wasSuccessful()));
    properties.setProperty(JUnitTestResultConstants.FAILED,
        String.valueOf(results.getFailureCount()));
    properties.setProperty(JUnitTestResultConstants.IGNORED,
        String.valueOf(results.getIgnoreCount()));
    properties.setProperty(JUnitTestResultConstants.TOTAL,
        String.valueOf(results.getRunCount()));
    properties.setProperty(JUnitTestResultConstants.RUNTIME,
        String.valueOf(results.getRunTime()));
    
    final List<String> testHeaders = new ArrayList<String>(results
        .getFailureCount());
    final List<Failure> failures = results.getFailures();
    for (final Failure failure : failures) {
      properties.put(failure.getTestHeader(), failure.getTrace());
      testHeaders.add(failure.getTestHeader());
    }
    
    properties.setProperty(JUnitTestResultConstants.FAILEDTESTS,
        Util.implode(",", testHeaders));
    
    System.out.println(resultsFile.getAbsolutePath());
    final FileWriter writer = new FileWriter(resultsFile);
    try {
      properties.store(writer, null);
    } finally {
      writer.close();
    }
    
    System.out.println(properties);
    
    if (!results.wasSuccessful()) {
      System.exit(1);
    }
  }
  
  private static File getResultsFileFromArgs(final String[] args) {
    return new File(args[0]);
  }
  
  /**
   * @throws FileNotFoundException
   *           if the {@link #resultsFile} doesn't exist.
   * @throws IOException
   *           if the <tt>resultsFile</tt> cannot be written.
   */
  private static void verifyResultsFileExists() throws IOException {
    if (!resultsFile.exists()) {
      throw new FileNotFoundException(resultsFile.getAbsolutePath()
          + " does not exist.");
    } else if (!resultsFile.canRead()) {
      throw new IOException(resultsFile.getAbsolutePath()
          + " cannot be written.");
    }
  }
  
  private static Class<?>[] getClassesToTestFromArgs(final String[] args)
      throws ClassNotFoundException {
    final List<Class<?>> classesToTest = new ArrayList<Class<?>>();
    for (final String arg : args) {
      classesToTest.add(Class.forName(arg));
    }
    
    return classesToTest.toArray(new Class<?>[classesToTest.size()]);
  }
}
