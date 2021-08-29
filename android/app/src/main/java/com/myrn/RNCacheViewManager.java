package com.myrn;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewParent;
import android.widget.Toast;

import com.facebook.common.logging.FLog;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.ReactConstants;

import java.util.Map;
import java.util.WeakHashMap;

public class RNCacheViewManager {
  public static Map<String, ReactRootView> CACHE;
  public static final int REQUEST_OVERLAY_PERMISSION_CODE = 1111;
  public static final String REDBOX_PERMISSION_MESSAGE =
          "Overlay permissions needs to be granted in order for react native apps to run in dev mode";

  public static ReactRootView getRootView(String moduleName) {
    if (CACHE == null) return null;
    return CACHE.get(moduleName);
  }

  public static ReactInstanceManager getReactInstanceManager() {
    return  RNApplication.mReactNativeHost.getReactInstanceManager();
  }

  /**
   * 预加载所需的RN模块
   * @param activity 预加载时所在的Activity
   * @param launchOptions 启动参数
   * @param moduleNames 预加载模块名
   * 建议在主界面onCreate方法调用，最好的情况是主界面在应用运行期间一直存在不被关闭
   */
  public static void init(Activity activity, Bundle launchOptions, String... moduleNames) {
    if (CACHE == null) CACHE = new WeakHashMap<>();
    boolean needsOverlayPermission = false;
    if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
      needsOverlayPermission = true;
      Intent serviceIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
      FLog.w(ReactConstants.TAG, REDBOX_PERMISSION_MESSAGE);
      Toast.makeText(activity, REDBOX_PERMISSION_MESSAGE, Toast.LENGTH_LONG).show();
      activity.startActivityForResult(serviceIntent, REQUEST_OVERLAY_PERMISSION_CODE);
    }

    if (!needsOverlayPermission) {
      for (String moduleName : moduleNames) {
        ReactRootView rootView  = new ReactRootView(activity, null);
        rootView.startReactApplication(
                getReactInstanceManager(),
                moduleName,
                launchOptions);
        CACHE.put(moduleName, rootView);
        FLog.i(ReactConstants.TAG, moduleName+" has preload");
      }
    }
  }

  /**
   * 销毁指定的预加载RN模块
   *
   * @param componentName
   */
  public static void onDestroyOne(String componentName) {
    try {
      ReactRootView reactRootView = CACHE.get(componentName);
      if (reactRootView != null) {
        ViewParent parent = reactRootView.getParent();
        if (parent != null) {
          ((android.view.ViewGroup) parent).removeView(reactRootView);
        }
        reactRootView.unmountReactApplication();
        CACHE.remove(componentName);
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  /**
   * 销毁全部RN模块
   * 建议在主界面onDestroy方法调用
   */
  public static void onDestroy() {
    try {
      for (Map.Entry<String, ReactRootView> entry : CACHE.entrySet()) {
        ReactRootView reactRootView = entry.getValue();
        ViewParent parent = reactRootView.getParent();
        if (parent != null) {
          ((android.view.ViewGroup) parent).removeView(reactRootView);
        }
        reactRootView.unmountReactApplication();
        reactRootView=null;
      }
      CACHE.clear();
      CACHE = null;
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}
