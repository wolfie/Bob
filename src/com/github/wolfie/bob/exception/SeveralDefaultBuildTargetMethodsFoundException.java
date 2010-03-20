package com.github.wolfie.bob.exception;

public class SeveralDefaultBuildTargetMethodsFoundException extends
    BobCheckedException {
  
  private static final long serialVersionUID = 819241842601637688L;
  
  public SeveralDefaultBuildTargetMethodsFoundException(
      final Class<?> buildClass) {
    super(String.format("Several default build target methods found in class "
        + buildClass.getName()));
  }
}
