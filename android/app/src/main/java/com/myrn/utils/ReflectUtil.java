package com.myrn.utils;

import java.lang.reflect.Field;

public class ReflectUtil {
  public static Object getProperty(Object obj, String propertyName) {
    try {
      Class clazz = obj.getClass();
      Field field = clazz.getDeclaredField(propertyName);
      field.setAccessible(true);
      return field.get(obj);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
