package com.github.wolfie.bob.action;

import com.github.wolfie.bob.internals.action._Compilation;

public class Compilation implements Action, ArtifactProducer.PathProducer {
  
  final _Compilation internal = new _Compilation();
  
  /**
   * Define the source path to compile.
   * <p/>
   * If the source path isn't included in the build's
   * {@link com.github.wolfie.bob.ProjectDescription}, the build will fail in a
   * {@link com.github.wolfie.bob.CompilationCache.NotCompiledException}.
   */
  public Compilation from(final String sourcePath) {
    internal.from(sourcePath);
    return this;
  }
  
  public Compilation disableDebug() {
    internal.disableDebug();
    return this;
  }
  
  @Override
  public Compilation to(final String path) {
    internal.to(path);
    return this;
  }
}
