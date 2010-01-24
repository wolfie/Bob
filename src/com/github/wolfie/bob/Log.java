package com.github.wolfie.bob;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public class Log {
  private static final Logger logger = Logger.getLogger(Bob.class.getPackage()
      .toString());
  private static final Handler handler = new ConsoleHandler();
  
  static {
    logger.addHandler(handler);
    logger.setLevel(Level.ALL);
    handler.setLevel(Level.ALL);
  }
  
  public static void log(final LogRecord record) {
    logger.log(record);
  }
  
  public static void log(final Level level, final String msg) {
    logger.log(level, msg);
  }
  
  public static void log(final Level level, final String msg,
      final Object param1) {
    logger.log(level, msg, param1);
  }
  
  public static void log(final Level level, final String msg,
      final Object[] params) {
    logger.log(level, msg, params);
  }
  
  public static void log(final Level level, final String msg,
      final Throwable thrown) {
    logger.log(level, msg, thrown);
  }
  
  public static void logp(final Level level, final String sourceClass,
      final String sourceMethod, final String msg) {
    logger.logp(level, sourceClass, sourceMethod, msg);
  }
  
  public static void logp(final Level level, final String sourceClass,
      final String sourceMethod, final String msg, final Object param1) {
    logger.logp(level, sourceClass, sourceMethod, msg, param1);
  }
  
  public static void logp(final Level level, final String sourceClass,
      final String sourceMethod, final String msg, final Object[] params) {
    logger.logp(level, sourceClass, sourceMethod, msg, params);
  }
  
  public static void logp(final Level level, final String sourceClass,
      final String sourceMethod, final String msg, final Throwable thrown) {
    logger.logp(level, sourceClass, sourceMethod, msg, thrown);
  }
  
  public static void logrb(final Level level, final String sourceClass,
      final String sourceMethod, final String bundleName, final String msg) {
    logger.logrb(level, sourceClass, sourceMethod, bundleName, msg);
  }
  
  public static void logrb(final Level level, final String sourceClass,
      final String sourceMethod, final String bundleName, final String msg,
      final Object param1) {
    logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, param1);
  }
  
  public static void logrb(final Level level, final String sourceClass,
      final String sourceMethod, final String bundleName, final String msg,
      final Object[] params) {
    logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, params);
  }
  
  public static void logrb(final Level level, final String sourceClass,
      final String sourceMethod, final String bundleName, final String msg,
      final Throwable thrown) {
    logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, thrown);
  }
  
  public static void severe(final String msg) {
    logger.severe(msg);
  }
  
  public static void warning(final String msg) {
    logger.warning(msg);
  }
  
  public static void info(final String msg) {
    logger.info(msg);
  }
  
  public static void config(final String msg) {
    logger.config(msg);
  }
  
  public static void fine(final String msg) {
    logger.fine(msg);
  }
  
  public static void finer(final String msg) {
    logger.finer(msg);
  }
  
  public static void finest(final String msg) {
    logger.finest(msg);
  }
  
  public static void setLevel(final Level newLevel) throws SecurityException {
    logger.setLevel(newLevel);
  }
  
  public static Level getLevel() {
    return logger.getLevel();
  }
}
