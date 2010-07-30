package com.github.wolfie.bob.exception;

/**
 * An exception was thrown while trying to process an
 * {@link com.github.wolfie.bob.action.Action Action}
 * 
 * @author Henrik Paul
 */
public class ProcessingError extends Error {
  private static final long serialVersionUID = 3823064523285817093L;
  
  public ProcessingError(final Throwable e) {
    super(e);
  }
  
  public ProcessingError(final String msg) {
    super(msg);
  }
  
  public ProcessingError(final String message, final Throwable cause) {
    super(message, cause);
  }
}
