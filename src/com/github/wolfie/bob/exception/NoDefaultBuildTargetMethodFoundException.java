package com.github.wolfie.bob.exception;

public class NoDefaultBuildTargetMethodFoundException extends BobRuntimeException {
  private static final long serialVersionUID = 5421998199551065594L;
  
  public NoDefaultBuildTargetMethodFoundException(final Class<?> buildClass) {
    super("No build method found in class " + buildClass.getName());
  }
}
