package com.github.wolfie.bob.action.optional;

/**
 * <h1>Result Property File Format</h1>
 * 
 * <pre>
 * success = [boolean: was the test run successful overall]
 * failed = [int: amount of failed tests]
 * ignored = [int: amount of ignored tests]
 * total = [int: amount of total test]
 * runtime = [long: run time in milliseconds]
 * failedtests = [string: comma-separated list of failed test_headers in the order of execution]
 * [test_header] = [string: failure_message]
 * </pre>
 */
public class JUnitTestResultConstants {
  
  static final String SUCCESS = "success";
  static final String FAILED = "failed";
  static final String IGNORED = "ignored";
  static final String TOTAL = "total";
  static final String RUNTIME = "runtime";
  static final String FAILEDTESTS = "failedtests";
  
}
