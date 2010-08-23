package com.github.wolfie.bob.internals.action.optional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.github.wolfie.bob.Defaults;
import com.github.wolfie.bob.exception.ProcessingError;
import com.github.wolfie.bob.internals._Action;
import com.github.wolfie.bob.internals.action._Compilation;
import com.github.wolfie.bob.util.Util;

public class _JUnitTestRun implements _Action {
  
  private static final String JUNITCORE_CLASSNAME = "org.junit.runner.JUnitCore";
  
  private final LinkedHashSet<Class<?>> classesToTest = new LinkedHashSet<Class<?>>();
  private _Compilation tests = null;
  private _Compilation targets = null;
  
  private boolean isProcessed;
  
  public _JUnitTestRun testsFrom(final _Compilation compilation) {
    tests = compilation;
    return this;
  }
  
  public _JUnitTestRun targetsFrom(final _Compilation compilation) {
    targets = compilation;
    return this;
  }
  
  public _JUnitTestRun run(final Class<?>... classes) {
    for (final Class<?> clazz : classes) {
      classesToTest.add(clazz);
    }
    return this;
  }
  
  @Override
  public void process() {
    setDefaults();
    
    if (classesToTest.isEmpty()) {
      System.err.println("No tests marked to run");
      return;
    }
    
    tests.process();
    
    int exitCode = 0;
    try {
      final String testsClassPath = tests.get().getAbsolutePath();
      
      exitCode = new JavaLauncher(JUnitTestRunner.class)
          .ensureClassCanBeLoaded(JUNITCORE_CLASSNAME)
          .userProvidedForcedClassPath(
              testsClassPath)
          .addAppArg(File.createTempFile("bobJUnitResults", ".properties")
                  .getAbsolutePath())
          .addAppArgs(getClassNamesToTest())
          .run();
    } catch (final Exception e) {
      e.printStackTrace();
      throw new ProcessingError(e);
    }
    
    if (exitCode != 0) {
      throw new ProcessingError("thread didn't exit properly");
    }
    
    isProcessed = true;
  }
  
  private Collection<String> getClassNamesToTest() {
    final List<String> names = new ArrayList<String>();
    for (final Class<?> clazz : classesToTest) {
      names.add(clazz.getName());
    }
    return names;
  }
  
  private void setDefaults() {
    if (targets == null) {
      targets = new _Compilation();
    }
    
    if (tests == null) {
      try {
        tests = new _Compilation()
            .from(Defaults.DEFAULT_TEST_SRC_PATH)
            .to(Util.getTemporaryDirectory().getAbsolutePath());
      } catch (final IOException e) {
        throw new ProcessingError(e);
      }
    }
  }
  
  @Override
  public boolean isProcessed() {
    return isProcessed;
  }
}
