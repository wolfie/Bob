package com.github.wolfie.bob;

public class Tuple<T1, T2> {
  
  private final T1 t1;
  private final T2 t2;
  
  private Tuple(final T1 first, final T2 second) {
    this.t1 = first;
    this.t2 = second;
  }
  
  /** Returns a tuple containing <code>null</code>s */
  public static <T1 extends Object, T2 extends Object> Tuple<T1, T2> ofNull() {
    return of(null, null);
  }
  
  /** Construct a Tuple out of two objects */
  public static <T1 extends Object, T2 extends Object> Tuple<T1, T2> of(
      final T1 first, final T2 second) {
    return new Tuple<T1, T2>(first, second);
  }
  
  public T1 getFirst() {
    return t1;
  }
  
  public T2 getSecond() {
    return t2;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((t1 == null) ? 0 : t1.hashCode());
    result = prime * result + ((t2 == null) ? 0 : t2.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Tuple<?, ?> other = (Tuple<?, ?>) obj;
    if (t1 == null) {
      if (other.t1 != null) {
        return false;
      }
    } else if (!t1.equals(other.t1)) {
      return false;
    }
    if (t2 == null) {
      if (other.t2 != null) {
        return false;
      }
    } else if (!t2.equals(other.t2)) {
      return false;
    }
    return true;
  }
  
  @Override
  public String toString() {
    return "Tuple [t1=" + t1 + ", t2=" + t2 + "]";
  }
  
}
