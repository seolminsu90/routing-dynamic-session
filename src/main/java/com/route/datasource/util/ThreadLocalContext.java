package com.route.datasource.util;

public class ThreadLocalContext {
  private static ThreadLocal<Integer> world = new ThreadLocal<>();
  
  public static Integer get() {
    return world.get();
  }
  
  public static void set(Integer worldId) {
    world.set(worldId);
  }
  
  public static void remove() {
    world.remove();
  }
}
