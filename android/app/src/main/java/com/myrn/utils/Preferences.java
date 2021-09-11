package com.myrn.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
  private static Context mContext;

  public static void init(Context context) {
    mContext = context;
  }

  public static SharedPreferences getInstance() throws Exception {
    if (mContext == null) throw new Exception("context is null");
    return mContext.getSharedPreferences("data",Context.MODE_PRIVATE);
  }

  public static void storageKV(String key, Object value) {
    try {
      if (value instanceof String) {
        getInstance().edit().putString(key, (String) value).commit();
      } else if (value instanceof Boolean) {
        getInstance().edit().putBoolean(key, (Boolean) value).commit();
      } else if (value instanceof Long) {
        getInstance().edit().putLong(key, (Long) value).commit();
      } else {
        throw new Exception("Don't support type");
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public static Object getValueByKey(String key, Class clazz) {
   try {
     if (clazz == String.class) {
       return getInstance().getString(key,"");
     } else if (clazz == Boolean.class) {
       return getInstance().getBoolean(key,false);
     } else if (clazz == Long.class) {
       return getInstance().getLong(key,0);
     }
   } catch (Exception exception) {
     exception.printStackTrace();
   }
    return null;
  }
}
