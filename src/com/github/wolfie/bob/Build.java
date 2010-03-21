package com.github.wolfie.bob;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.github.wolfie.bob.action.Action;
import com.github.wolfie.bob.annotation.Target;
import com.github.wolfie.bob.exception.BuildError;

public abstract class Build {
  private static final String DEFAULT_BUILD_CLASS_NAME = "Default";
  private static final String DEFAULT_BUILD_TARGET_NAME = "build";
  
  private static String buildClassName;
  private static String buildTargetName;
  
  private static final Class<?> BUILD_TARGET_RETURN_CLASS = Action.class;
  
  public static void main(final String[] args) {
    processArgs(args);
    
    try {
      final Class<? extends Build> buildClass = getBuildClass();
      final Method buildMethod = getBuildMethod(buildClass);
      final Action action = (Action) buildMethod.invoke(buildClass
          .newInstance());
      Log.info("Processing " + action.getClass().getSimpleName());
      action.process();
    } catch (final InvocationTargetException e) {
      throw new BuildError("An exception occurred during building", e);
    } catch (final IllegalArgumentException e) {
      throw new BuildError(e);
    } catch (final IllegalAccessException e) {
      throw new BuildError(e);
    } catch (final InstantiationException e) {
      throw new BuildError(e);
    }
  }
  
  private static Method getBuildMethod(final Class<? extends Build> clazz) {
    final Set<Method> buildMethods = getBuildMethods(clazz);
    
    final String usedBuildTargetName = buildTargetName != null ? buildTargetName
        : DEFAULT_BUILD_TARGET_NAME;
    
    for (final Method method : buildMethods) {
      if (method.getName().equals(usedBuildTargetName)) {
        return method;
      }
    }
    
    throw new BuildError("Build target \"" + usedBuildTargetName
        + "\" not found in class " + clazz.getName());
  }
  
  private static Set<Method> getBuildMethods(final Class<? extends Build> clazz) {
    final Set<Method> methods = new HashSet<Method>();
    
    for (final Method method : clazz.getMethods()) {
      if (method.getAnnotation(Target.class) != null) {
        boolean isSuitableMethod = true;
        
        // Annotated method found. Let's do some sanity checks
        
        final Class<?> returnType = method.getReturnType();
        if (!returnType.equals(BUILD_TARGET_RETURN_CLASS)) {
          Log.warning("Method " + method.getName() + " in class "
              + clazz.getName() + " doesn't return "
              + BUILD_TARGET_RETURN_CLASS.getName() + " but "
              + returnType.getName());
          isSuitableMethod = false;
        }
        
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 0) {
          Log.warning("Method " + method.getName() + " in class "
              + clazz.getName() + " is annotated as a build target, "
              + "but requires arguments. This isn't allowed.");
          isSuitableMethod = false;
        }
        
        if (isSuitableMethod) {
          Log.fine("Found suitable target \"" + method.getName() + "\" in "
              + clazz.getName());
          methods.add(method);
        }
      }

      else if (method.getName().equals(DEFAULT_BUILD_TARGET_NAME)
          && method.getReturnType().equals(BUILD_TARGET_RETURN_CLASS)
          && method.getParameterTypes().length == 0) {
        
        /*
         * Nag about annotations. I think it's better to always to use
         * annotations instead of having exceptions
         */
        Log.warning("Default build target \"" + DEFAULT_BUILD_TARGET_NAME
            + "\" found in class " + clazz.getName()
            + ", but it was not properly annotated. Ignoring.");
      }
    }
    
    return methods;
  }
  
  private static Class<? extends Build> getBuildClass() {
    final String usedBuildClassName = buildClassName != null ? buildClassName
        : DEFAULT_BUILD_CLASS_NAME;
    
    try {
      final Class<?> forName = Class.forName(usedBuildClassName);
      if (Build.class.isAssignableFrom(forName)) {
        // We just checked for this, so it must be okay
        @SuppressWarnings("unchecked")
        final Class<? extends Build> buildClass = (Class<? extends Build>) forName;
        
        Log.fine("Using \"" + buildClass.getName() + "\" as build class");
        return buildClass;
      } else {
        throw new BuildError(usedBuildClassName
            + " is not an implementation of " + Build.class.getName());
      }
    } catch (final ClassNotFoundException e) {
      throw new BuildError(usedBuildClassName + " not found.", e);
    }
  }
  
  private static void processArgs(final String[] args) {
    // TODO
  }
  
  @Target
  public abstract Action build();
}
