package com.github.wolfie.bob.action.optional;

import com.github.wolfie.bob.action.Action;
import com.github.wolfie.bob.action.Compilation;
import com.github.wolfie.bob.internals.action.optional._JUnitTestRun;
import com.github.wolfie.bob.util.Util;

public class JUnitTestRun implements Action {
  
  private final _JUnitTestRun internal = new _JUnitTestRun();
  
  public JUnitTestRun testsFrom(final Compilation compilation) {
    internal.testsFrom(Util.getInternal(compilation));
    return this;
  }
  
  public JUnitTestRun targetsFrom(final Compilation compilation) {
    internal.targetsFrom(Util.getInternal(compilation));
    return this;
  }
  
  public JUnitTestRun run(final Class<?>... classes) {
    internal.run(classes);
    return this;
  }
}
