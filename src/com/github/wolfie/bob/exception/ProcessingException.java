package com.github.wolfie.bob.exception;

import com.github.wolfie.bob.action.Action;

/**
 * An exception was thrown while trying to process an {@link Action}
 * 
 * @author Henrik Paul
 */
public class ProcessingException extends BobRuntimeException {
  private static final long serialVersionUID = 3823064523285817093L;
  
  public ProcessingException(final Throwable e) {
    super(e);
  }
  
  public ProcessingException(final String msg) {
    super(msg);
  }
  
  public ProcessingException(final String message, final Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }
}
