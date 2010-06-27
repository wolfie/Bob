package com.github.wolfie.bob.action.optional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.github.wolfie.bob.Defaults;
import com.github.wolfie.bob.Util;
import com.github.wolfie.bob.action.Action;
import com.github.wolfie.bob.action.Compilation;
import com.github.wolfie.bob.exception.ProcessingException;

public class JUnitTestRun implements Action {
  
  private static final String JUNITCORE_CLASSNAME = "org.junit.runner.JUnitCore";
  
  private final LinkedHashSet<Class<?>> classesToTest = new LinkedHashSet<Class<?>>();
  private Compilation testsFromCompilation = null;
  private Compilation targetsFrom;
  
  public JUnitTestRun testsFrom(final Compilation compilation) {
    testsFromCompilation = compilation;
    return this;
  }
  
  public JUnitTestRun targetsFrom(final Compilation compilation) {
    targetsFrom = compilation;
    return this;
  }
  
  public JUnitTestRun run(final Class<?>... classes) {
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
    
    testsFromCompilation.process();
    
    int exitCode = 0;
    try {
      exitCode = new JavaLauncher(JUnitTestRunner.class)
          .ensureClassCanBeLoaded(JUNITCORE_CLASSNAME)
          .addAppArg(File.createTempFile("bobJUnitResults", ".properties")
                  .getAbsolutePath())
          .addAppArgs(getClassNamesToTest())
          .run();
    } catch (final Exception e) {
      e.printStackTrace();
      throw new ProcessingException(e);
    }
    
    if (exitCode != 0) {
      throw new ProcessingException("thread didn't exit properly");
    }
  }
  
  private Collection<String> getClassNamesToTest() {
    final List<String> names = new ArrayList<String>();
    for (final Class<?> clazz : classesToTest) {
      names.add(clazz.getName());
    }
    return names;
  }
  
  private void setDefaults() {
    if (targetsFrom == null) {
      targetsFrom = new Compilation();
    }
    
    if (testsFromCompilation == null) {
      try {
        testsFromCompilation = new Compilation()
            .use(targetsFrom)
            .useJarsAt("dist/lib/")
            .from(Defaults.DEFAULT_TEST_SRC_PATH)
            .to(Util.getTemporaryDirectory().getAbsolutePath());
      } catch (final IOException e) {
        throw new ProcessingException(e);
      }
    }
  }
}
