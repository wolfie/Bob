package com.github.wolfie.bob.action;

import java.io.File;

/**
 * This class allows access to selected package-private data of classes in the
 * package <tt>com.github.wolfie.bob.action</tt>.
 * <p/>
 * The intention of this class is to keep the API minimal (thus clean and
 * simple) from the user, while allowing developers (Bob developers included) to
 * access safe data in a public way.
 * <p/>
 * For example, this class is a way for optional Actions to query information
 * from core Actions.
 */
public class _revealer {
  private _revealer() {
  }
  
  /** @see Compilation#getDestinationDir() */
  public static File getDestinationDir(final Compilation compilation) {
    return compilation.getDestinationDir();
  }
}
