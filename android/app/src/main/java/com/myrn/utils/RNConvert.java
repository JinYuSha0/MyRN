package com.myrn.utils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class RNConvert {
  public static WritableMap obj2WritableMap(Object obj) {
    if (obj == null) return  null;
    WritableMap map = Arguments.createMap();
    Class clazz = obj.getClass();
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      try {
        field.setAccessible(true);
        String key = field.getName();
        Object value = field.get(obj);
        if (value == null) {
          map.putNull(key);
        } else {
          if (value instanceof String) {
            map.putString(key,(String) value);
          } else if (value instanceof Integer) {
            map.putInt(key, (Integer) value);
          } else if (value instanceof Long) {
            map.putString(key, String.valueOf(value));
          } else if (value instanceof Double || value instanceof Float) {
            map.putDouble(key, (Double) value);
          } else if (value instanceof Boolean) {
            map.putBoolean(key, (Boolean) value);
          } else if (value instanceof ArrayList) {
            map.putArray(key, array2WritableArray((ArrayList) value));
          } else  {
            map.putMap(key, obj2WritableMap(value));
          }
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return map;
  }

  public static WritableArray array2WritableArray(ArrayList list) {
    if (list == null) return null;
    WritableArray writableArray =  Arguments.createArray();
    try {
      for (int i = 0; i < list.size(); i++) {
        Object value = list.get(i);
        if (value == null) {
          writableArray.pushNull();
        } else {
          if (value instanceof String) {
            writableArray.pushString((String) value);
          } else if (value instanceof Integer) {
            writableArray.pushInt((Integer) value);
          } else if (value instanceof Long) {
            writableArray.pushString(String.valueOf(value));
          } else if (value instanceof Double || value instanceof Float) {
            writableArray.pushDouble((Double) value);
          } else if (value instanceof Boolean) {
            writableArray.pushBoolean((Boolean) value);
          } else if (value instanceof ArrayList) {
            writableArray.pushArray(array2WritableArray((ArrayList) value));
          } else  {
            writableArray.pushMap(obj2WritableMap(value));
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return writableArray;
  }

  private static boolean isBaseType(Object value) {
    if (value == null) return true;
    Boolean isBase = true;
    if (value instanceof String) {
    } else if (value instanceof Integer || value instanceof Double || value instanceof Float || value instanceof Long) {
    } else if (value instanceof Boolean) {
    } else if (value instanceof WritableMap) {
    } else if (value instanceof ReadableMap) {
    } else if (value instanceof WritableArray) {
    } else if (value instanceof ReadableArray) {
    } else if (value instanceof ArrayList) {
      isBase = false;
    } else  {
      isBase = false;
    }
    return isBase;
  }

  private static Object compatible(Object value) {
    if (value instanceof Float) {
      return (Double) value;
    } else if (value instanceof Long) {
      return String.valueOf(value);
    }
    return value;
  }

  public static Object convert(Object obj) {
    if (isBaseType(obj)) {
      return compatible(obj);
    } else {
      if (obj instanceof ArrayList) {
        return array2WritableArray((ArrayList) obj);
      } else {
        return obj2WritableMap(obj);
      }
    }
  }
}
