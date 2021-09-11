package com.myrn.utils;

import android.os.Build;

public class PhoneInfo {
  // 获取手机型号
  public static String getPhoneModel() {
    return Build.MODEL;
  }


}
