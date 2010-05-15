package com.github.wolfie.bob.exception;

import java.lang.reflect.Method;

import com.github.wolfie.bob.annotation.Target;

public class TargetIsNotAnnotatedException extends BobRuntimeException {
  private static final long serialVersionUID = 4395187907851521202L;
  
  public TargetIsNotAnnotatedException(final Method method) {
    super("A method " + method.getName()
        + " was found, but it was not annotated with " + Target.class.getName());
  }
}
