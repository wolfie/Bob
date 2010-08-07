package com.github.wolfie.bob;

abstract public class BobBuild {
  
  private final ProjectDescription paths;
  
  public BobBuild() {
    paths = describeProject();
    paths.setFinalized();
  }
  
  /**
   * <p>
   * Describe your project's strucutre with a {@link ProjectDescription}.
   * </p>
   * 
   * <p>
   * If the user project doesn't override this method, the default project
   * description will be used.
   * </p>
   * 
   * 
   * <p>
   * <em>Implementation note:</em> This method will be invoked in the
   * superclass' constructor, so non-static fields are guaranteed to be
   * uninitialized. References to uninitialized non-static fields lead most
   * probably into {@link NullPointerException NullPointerExceptions}.
   * </p>
   * 
   * @see ProjectDescription#getDefault()
   */
  protected ProjectDescription describeProject() {
    return ProjectDescription.getDefault();
  }
}
