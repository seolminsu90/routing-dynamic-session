package com.route.datasource.util;

public class ThreadLocalContext {
  private static ThreadLocal<Integer> lookUpKey = new ThreadLocal<>();
  
  public static Integer get() {
    return lookUpKey.get();
  }
  
  public static void set(Integer _lookUpKey) {
    lookUpKey.set(_lookUpKey);
  }
  
  public static void remove() {
    lookUpKey.remove();
  }
}
