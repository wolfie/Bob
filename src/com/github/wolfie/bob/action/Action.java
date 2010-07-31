package com.github.wolfie.bob.action;

/**
 * The interface to define any kind of action that can be taken by a build
 * target.
 * 
 * @author Henrik Paul
 * @since 1.0.0
 */
public interface Action {
  
  /**
   * Process the action itself.
   */
  void process();
}
