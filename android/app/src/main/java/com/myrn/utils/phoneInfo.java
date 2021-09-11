package com.myrn.utils;

import android.os.Build;

public class phoneInfo {
  // 获取手机型号
  public static String getPhoneModel() {
    return Build.MODEL;
  }


}
