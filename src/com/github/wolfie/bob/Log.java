package com.github.wolfie.bob;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

public class Log {
  
  public enum LogLevel {
    /**
     * Something went horribly wrong, and probably will prevent a successful
     * execution.
     */
    SEVERE,
    
    /**
     * Something didn't work out as they should, but the show can still go on
     */
    WARNING,
    
    /**
     * General good-to-know information for the user. Should be kept to an
     * absolute minimum.
     */
    INFO,
    
    /**
     * Information for the common user about what is going on at each stage of
     * the execution. Understanding these messages should not require any
     * knowledge of the inner workings of the application.
     */
    VERBOSE,
    
    /**
     * Stuff for developers' interests. Information about the inner workings of
     * the application, which would most probably be mumbo-jumbo for the common
     * user. For debug purposes, in short.
     */
    DEBUG;
    
    private boolean lt(final LogLevel level) {
      return ordinal() < level.ordinal();
    }
  };
  
  public static class MultiLog {
    private final Map<String, LogLevel> logs = new HashMap<String, LogLevel>();
    
    public MultiLog(final String msg, final LogLevel level) {
      or(msg, level);
    }
    
    public MultiLog or(final String msg, final LogLevel level) {
      logs.put(msg, level);
      return this;
    }
    
    private void log(final Log log) {
      Entry<String, LogLevel> highestLog = null;
      
      for (final Entry<String, LogLevel> multiLog : logs.entrySet()) {
        final LogLevel level = multiLog.getValue();
        
        if (highestLog == null || level.lt(highestLog.getValue())) {
          highestLog = multiLog;
        }
      }
      
      if (highestLog != null) {
        log.log(highestLog.getKey(), highestLog.getValue());
      }
    }
  }
  
  private static final int MAX_LOCATION_LENGTH = 8;
  
  // lazy initialization
  private static Log singleton = null;
  
  private final Stack<String> logStack = new Stack<String>();
  
  private LogLevel logLevel = LogLevel.INFO;
  
  private boolean logStackHasChangedSinceLastOutput = false;
  
  private final PrintStream out = System.out;
  private final PrintStream err = System.err;
  
  private Log() {
  }
  
  public synchronized Log enter(final String location) {
    if (location.length() > MAX_LOCATION_LENGTH) {
      throw new IllegalArgumentException("Location may not be longer than "
          + MAX_LOCATION_LENGTH + " characters long: " + location);
    }
    
    logStack.push(location);
    logStackHasChangedSinceLastOutput = true;
    return this;
  }
  
  public synchronized Log exit() {
    logStack.pop();
    logStackHasChangedSinceLastOutput = true;
    return this;
  }
  
  public synchronized Log setLogLevel(final LogLevel level) {
    logLevel = level;
    return this;
  }
  
  private synchronized boolean isLoggingAt(final LogLevel level) {
    return level.ordinal() <= logLevel.ordinal();
  }
  
  public synchronized Log log(final String msg, final LogLevel level) {
    if (isLoggingAt(level)) {
      
      // if (logStackHasChangedSinceLastOutput && !logStack.isEmpty()) {
      // getPrintStream(level).println("\n" + logStack.peek());
      // }
      // logStackHasChangedSinceLastOutput = false;
      
      final String alignedStack = Util.rightAlign(logStack.peek(),
          MAX_LOCATION_LENGTH);
      getPrintStream(level).format("[%s] %s\n", alignedStack, msg);
    }
    return this;
  }
  
  private synchronized PrintStream getPrintStream(final LogLevel level) {
    final PrintStream usedStream;
    if (level.compareTo(LogLevel.WARNING) >= 0) {
      usedStream = err;
    } else {
      usedStream = out;
    }
    return usedStream;
  }
  
  public synchronized Log log(final MultiLog altLogs) {
    altLogs.log(this);
    return this;
  }
  
  public static Log get() {
    if (singleton == null) {
      singleton = new Log();
    }
    return singleton;
  }
  
  /**
   * 
   * @param formattedString
   *          A string where "%s" is the placeholder for the file path.
   * @param file
   * @return
   */
  public synchronized Log logFile(final String formattedString, final File file) {
    final String shortLog = String.format(formattedString, file.getPath());
    final String longLog = String.format(formattedString,
        file.getAbsolutePath());
    log(new MultiLog(shortLog, LogLevel.VERBOSE)
        .or(longLog, LogLevel.DEBUG));
    return this;
  }
}
