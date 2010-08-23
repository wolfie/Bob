package com.github.wolfie.bob.action;

import com.github.wolfie.bob.action.ArtifactProducer.FileProducer;
import com.github.wolfie.bob.internals.action._Zip;
import com.github.wolfie.bob.util.Util;

public class Zip implements Action, FileProducer {
  
  private final _Zip internal = new _Zip();
  
  public Zip add(final String path, final String entryName) {
    internal.add(path, entryName);
    return this;
  }
  
  public Zip add(final FileProducer action,
      final String entryName) {
    internal.add(Util.getInternal(action), entryName);
    return this;
  }
  
  public Zip add(final PathProducer action, final String entryPath) {
    internal.add(Util.getInternal(action), entryPath);
    return this;
  }
  
  @Override
  public Zip to(final String path) {
    internal.to(path);
    return this;
  }
}
