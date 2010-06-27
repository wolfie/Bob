package com.github.wolfie.bob;

import java.util.HashMap;
import java.util.Map;

public class HashMap2D<K1, K2, V> {
  
  public interface Entry<K1, K2, V> {
    K1 key1();
    
    K2 key2();
    
    V value();
  }
  
  private class EntryImpl implements Entry<K1, K2, V> {
    private final K1 key1;
    private final K2 key2;
    private final V value;
    
    public EntryImpl(final K1 key1, final K2 key2, final V value) {
      this.key1 = key1;
      this.key2 = key2;
      this.value = value;
    }
    
    @Override
    public K1 key1() {
      return key1;
    }
    
    @Override
    public K2 key2() {
      return key2;
    }
    
    @Override
    public V value() {
      return value;
    }
  }
  
  private final Map<K1, Map<K2, V>> map1 = new HashMap<K1, Map<K2, V>>();
  private final Map<K2, Map<K1, V>> map2 = new HashMap<K2, Map<K1, V>>();
  
  public void put(final K1 key1, final K2 key2, final V value) {
    put(map1, key1, key2, value);
    put(map2, key2, key1, value);
  }
  
  public V get(final K1 key1, final K2 key2) {
    final Map<K2, V> map = map1.get(key1);
    if (map != null) {
      return map.get(key2);
    } else {
      return null;
    }
  }
  
  private static <KK1 extends Object, KK2 extends Object, VV extends Object> void put(
      final Map<KK1, Map<KK2, VV>> map, final KK1 key1, final KK2 key2,
      final VV value) {
    final Map<KK2, VV> tempMap = getAndPutIfMissing(map, key1);
    tempMap.put(key2, value);
  }
  
  private static <VV, KK1, KK2> Map<KK2, VV> getAndPutIfMissing(
      final Map<KK1, Map<KK2, VV>> map, final KK1 key1) {
    
    Map<KK2, VV> tempMap = map.get(key1);
    if (tempMap == null) {
      tempMap = new HashMap<KK2, VV>();
      map.put(key1, tempMap);
    }
    return tempMap;
  }
}
