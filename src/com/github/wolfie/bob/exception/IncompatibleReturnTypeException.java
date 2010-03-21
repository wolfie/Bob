package com.github.wolfie.bob.exception;

import java.lang.reflect.Method;

import com.github.wolfie.bob.action.Action;

public class IncompatibleReturnTypeException extends BobRuntimeException {
  private static final long serialVersionUID = -7268263437206804036L;
  
  public IncompatibleReturnTypeException(final Method method) {
    super(String.format("Method %s.%s() has a return type %s, "
        + "but it is incompatible with %s", method.getDeclaringClass()
        .getName(), method.getName(), method.getReturnType().getName(),
        Action.class.getName()));
  }
}
