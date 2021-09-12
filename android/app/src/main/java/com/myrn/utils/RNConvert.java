package com.myrn.utils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.lang.reflect.Field;

public class RNConvert {
  public static WritableMap obj2Map(Object obj) {
    WritableMap map = Arguments.createMap();
    Class clazz = obj.getClass();
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      try {
        field.setAccessible(true);
        String key = field.getName();
        Object value = field.get(obj);
        if (value instanceof String) {
          map.putString(key,(String) value);
        } else if (value instanceof Integer) {
          map.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
          map.putBoolean(key, (Boolean) value);
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return map;
  }
}
