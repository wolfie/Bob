package com.github.wolfie.bob;

abstract public class BobBuild {
  
  private final ProjectDescription paths;
  
  public BobBuild() {
    paths = describeProject();
    paths.setFinalized();
  }
  
  /**
   * <p>
   * <tt>TODO: write javadoc</tt>
   * </p>
   * 
   * <p>
   * <em>Implementation note:</em> This method will be invoked in the
   * superclass' constructor, so fields are guaranteed to be uninitialized.
   * References to uninitialized fields lead most probably into
   * {@link NullPointerException NullPointerExceptions}.
   * </p>
   * 
   * @return
   */
  abstract protected ProjectDescription describeProject();
}
