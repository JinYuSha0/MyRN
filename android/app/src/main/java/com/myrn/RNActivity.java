package com.myrn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;

public abstract class RNActivity extends androidx.fragment.app.FragmentActivity implements DefaultHardwareBackBtnHandler, PermissionAwareActivity {
  public enum BundleType {ASSETS, FILE, NETWORK}
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
    // 设置StatusBar样式
    if (getIntent() != null) {
      setStatusBar(getIntent().getExtras());
    }
    isDev = mReactNativeHost.getUseDeveloperSupport();
  }

  protected RNActivityDelegate createRNActivityDelegate() {
    if (mDelegate == null) {
      mDelegate = new RNActivityDelegate(this, getMainComponentName()) {
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
    mDelegate.onCreate(getBundle().toBundle());
    if (!isDev) {
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
    String bundlePath = bundle.bundlePath;
    String bundleURL = bundle.bundleURL;
    BundleType bundleType = bundle.bundleType;
    CatalystInstance instance = RNBundleLoader.getCatalystInstance(mReactNativeHost);
    if (bundleType == BundleType.ASSETS) {
      RNBundleLoader.loadScriptFromAsset(getApplicationContext(),instance,bundlePath,false);
      loadScriptListener.onLoadComplete(true,null);
    }
  }

  protected void runApp(String bundlePath) {
    RNBundle rnBundle = getBundle();
    if (rnBundle.bundleType == BundleType.NETWORK) {
    } else {
      initView();
    }
  }

  protected void initView() {
    RNBundle innerBundle = getBundle();
    Bundle bundle = new Bundle();
    bundle.putString("moduleName", innerBundle.moduleName);
    bundle.putBundle("params", innerBundle.params);
    if (innerBundle.moduleName != null && !"".equals(innerBundle.moduleName)) {
      mDelegate.loadApp(innerBundle.moduleName);
    }
  }

  protected void setStatusBar(@Nullable Bundle bundle) {
    if (bundle == null) return;
    Boolean statusBarTransparent = bundle.getBoolean("aax_statusBarTransparent",false);
    String statusBarDarkMode = bundle.getString("aax_statusBarDarkMode", "");
    if (statusBarTransparent) {
      StatusBarUtil.setTransparent(this);
    }
    if (!"".equals(statusBarDarkMode)) {
      if (statusBarDarkMode.equals("dark")) {
        StatusBarUtil.setDarkMode(this);
      } else {
        StatusBarUtil.setLightMode(this);
      }
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

  public abstract String getMainComponentName();

  public static interface LoadScriptListener {
    public void onLoadComplete(boolean success, String bundlePath);
  }

  protected final ReactNativeHost getReactNativeHost() {
    return RNApplication.mReactNativeHost;
  }

  protected final ReactInstanceManager getReactInstanceManager() {
    return getReactNativeHost().getReactInstanceManager();
  }
}
