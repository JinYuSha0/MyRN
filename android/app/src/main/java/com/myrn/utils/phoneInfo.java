package com.myrn.utils;

import android.app.Activity;
import android.os.Build;

public class phoneInfo {

  // 获取状态栏高度
  public static int getPhoneStatusBarHeight(Activity activity) {
    int resourceId = activity.getResources().getIdentifier("status_bar_height","dimen","android");
    return activity.getResources().getDimensionPixelSize(resourceId);
  }

  // 获取手机型号
  public static String getPhoneModel() {
    return Build.MODEL;
  }

}
