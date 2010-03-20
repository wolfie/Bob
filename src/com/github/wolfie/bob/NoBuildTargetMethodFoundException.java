package com.github.wolfie.bob;

import com.github.wolfie.bob.exception.BobCheckedException;

public class NoBuildTargetMethodFoundException extends BobCheckedException {
  private static final long serialVersionUID = 5421998199551065594L;
  
  public NoBuildTargetMethodFoundException(final Class<?> buildClass) {
    super("No build method found in class " + buildClass.getName());
  }
}
