package com.github.wolfie.bob;

public class Touple<T1, T2> {
  
  private final T1 t1;
  private final T2 t2;
  
  private Touple(final T1 first, final T2 second) {
    this.t1 = first;
    this.t2 = second;
  }
  
  public static <T1 extends Object, T2 extends Object> Touple<T1, T2> ofNull() {
    return of(null, null);
  }
  
  public static <T1 extends Object, T2 extends Object> Touple<T1, T2> of(
      final T1 first, final T2 second) {
    return new Touple<T1, T2>(first, second);
  }
  
  public T1 getFirst() {
    return t1;
  }
  
  public T2 getSecond() {
    return t2;
  }
}
