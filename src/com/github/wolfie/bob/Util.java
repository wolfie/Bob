package com.github.wolfie.bob;

import java.util.Collection;

public class Util {
  public static String implode(String glue, final Object... bits) {
    if (glue == null) {
      glue = "";
    }
    
    final StringBuilder builder = new StringBuilder();
    for (final Object bit : bits) {
      builder.append(bit.toString()).append(glue);
    }
    
    // remove the last bit
    builder.delete(builder.length() - glue.length(), builder.length());
    
    return builder.toString();
  }
  
  public static String implode(final String glue, final Collection<?> bits) {
    return implode(glue, bits.toArray());
  }
}
