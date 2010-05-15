package com.github.wolfie.bob.exception;

public class UnexpectedArgumentAmountException extends BobRuntimeException {
  private static final long serialVersionUID = 7478857951796286199L;
  
  public UnexpectedArgumentAmountException(final int argumentsTooMany,
      final int maxArgumentsAfterOptions) {
    super("There should be only " + maxArgumentsAfterOptions
        + " arguments after the options. "
        + (argumentsTooMany + maxArgumentsAfterOptions)
        + " were given.");
  }
}
