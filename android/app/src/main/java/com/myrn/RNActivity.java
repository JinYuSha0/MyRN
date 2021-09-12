package com.myrn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;
import com.jaeger.library.StatusBarUtil;
import com.myrn.constant.StatusBar;
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;

import static com.myrn.RNApplication.mReactNativeDB;

public abstract class RNActivity extends androidx.fragment.app.FragmentActivity implements DefaultHardwareBackBtnHandler, PermissionAwareActivity {
  private boolean bundleLoaded = false;
  private ReactNativeHost mReactNativeHost;
  private boolean isDev;
  private RNActivityDelegate mDelegate;

  protected RNActivity() {
    // 创建delegate
    mDelegate = createRNActivityDelegate();
    // 初始化ReactNativeHost
    if (mReactNativeHost == null) {
      mReactNativeHost = getReactNativeHost();
    }
    isDev = mReactNativeHost.getUseDeveloperSupport();
  }

  protected RNActivityDelegate createRNActivityDelegate() {
    if (mDelegate == null) {
      mDelegate = new RNActivityDelegate(this, null) {
        @Override
        protected ReactRootView createRootView() {
          return new RNGestureHandlerEnabledRootView(RNActivity.this);
        }
      };
    }
    return mDelegate;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle bundle = getBundle().toBundle();
    if (getIntent() != null && getIntent().getExtras() != null) {
      bundle.putAll(getIntent().getExtras());
    }
    // 设置StatusBar样式
    setStatusBar(getBundle().params);
    mDelegate.onCreate(bundle);
    if (isDev) {
      initView();
    } else {
      // 非开发模式走拆包流程
      ReactInstanceManager manager = mReactNativeHost.getReactInstanceManager();
      final Activity currActivity = this;
      if (!manager.hasStartedCreatingInitialContext() || RNBundleLoader.getCatalystInstance(mReactNativeHost) == null) {
        if (manager.hasStartedCreatingInitialContext()) {
          mReactNativeHost.getReactInstanceManager().destroy();
        }
        manager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
          @Override
          public void onReactContextInitialized(ReactContext context) {
            loadScript(new LoadScriptListener() {
              @Override
              public void onLoadComplete(boolean success, String bundlePath) {
                bundleLoaded = success;
                if (success) {
                  runApp(bundlePath);
                } else {
                  currActivity.finish();
                }
              }
            });
            manager.removeReactInstanceEventListener(this);
          }
        });
        mReactNativeHost.getReactInstanceManager().createReactContextInBackground();
      } else {
        loadScript(new LoadScriptListener() {
          @Override
          public void onLoadComplete(boolean success, String bundlePath) {
            bundleLoaded = success;
            if (success) {
              runApp(bundlePath);
            } else {
              currActivity.finish();
            }
          }
        });
      }
    }
  }

  protected void loadScript(LoadScriptListener loadScriptListener) {
    final RNBundle bundle = getBundle();
    String bundleName = bundle.bundleName;
    String moduleName = bundle.moduleName;
    RNDBHelper.Result result = mReactNativeDB.selectByBundleName(bundleName);
    CatalystInstance instance = RNBundleLoader.getCatalystInstance(mReactNativeHost);
    if (result == null) {
      // 未曾加载的模块
    } else {
      RNBundleLoader.loadScript(getApplicationContext(),instance,result.FilePath,false);
      loadScriptListener.onLoadComplete(true,null);
    }
  }

  protected void runApp(String bundlePath) {
    initView();
  }

  protected void initView() {
    RNBundle innerBundle = getBundle();
    if (innerBundle.moduleName != null && !"".equals(innerBundle.moduleName)) {
      mDelegate.loadApp(innerBundle.moduleName);
    }
  }

  protected void setStatusBar(@Nullable Bundle bundle) {
    if (bundle == null) return;
    Integer statusBarMode = bundle.getInt("statusBarMode",0);
    // 沉浸式状态栏
    if ((statusBarMode & StatusBar.transparent) > 0) {
      StatusBarUtil.setTransparent(this);
    }
    // 设置黑色字体
    if ((statusBarMode & StatusBar.darkMode) > 0) {
      StatusBarUtil.setLightMode(this);
      StatusBarUtil.setTranslucent(this);
    }
    // 设置白色字体
    if ((statusBarMode & StatusBar.lightMode) > 0) {
      StatusBarUtil.setDarkMode(this);
      StatusBarUtil.setTranslucent(this);
    }
  }

  protected final void loadApp(String appKey) {
    mDelegate.loadApp(appKey);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mDelegate.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mDelegate.onResume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mDelegate.onDestroy();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mDelegate.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return mDelegate.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    return mDelegate.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
  }

  @Override
  public boolean onKeyLongPress(int keyCode, KeyEvent event) {
    return mDelegate.onKeyLongPress(keyCode, event) || super.onKeyLongPress(keyCode, event);
  }

  @Override
  public void onBackPressed() {
    if (!mDelegate.onBackPressed()) {
      super.onBackPressed();
    }
  }

  @Override
  public void invokeDefaultOnBackPressed() {
    super.onBackPressed();
  }

  @Override
  public void onNewIntent(Intent intent) {
    if (!mDelegate.onNewIntent(intent)) {
      super.onNewIntent(intent);
    }
  }

  @Override
  public void requestPermissions(
          String[] permissions,
          int requestCode,
          PermissionListener listener) {
    mDelegate.requestPermissions(permissions, requestCode, listener);
  }

  @Override
  public void onRequestPermissionsResult(
          int requestCode,
          String[] permissions,
          int[] grantResults) {
    mDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  public abstract RNBundle getBundle();

  public static interface LoadScriptListener {
    public void onLoadComplete(boolean success, String bundlePath);
  }

  protected final ReactNativeHost getReactNativeHost() {
    return mDelegate.getReactNativeHost();
  }

  protected final ReactInstanceManager getReactInstanceManager() {
    return mDelegate.getReactInstanceManager();
  }
}
