package com.github.wolfie.bob.result;

import java.util.ArrayList;
import java.util.List;

import com.github.wolfie.bob.Log;

public class Actions implements Action {
  
  private final List<Action> actions = new ArrayList<Action>();
  
  public void addAction(final Action action) {
    actions.add(action);
  }
  
  public static Actions from(final Action... actions) {
    final Actions compiledActions = new Actions();
    for (final Action action : actions) {
      compiledActions.addAction(action);
    }
    return compiledActions;
  }
  
  @Override
  public void process() {
    for (final Action action : actions) {
      Log.info("Processing " + action.getClass().getSimpleName());
      action.process();
    }
  }
}
