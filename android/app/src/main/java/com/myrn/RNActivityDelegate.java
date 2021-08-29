package com.myrn;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactDelegate;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactRootView;

import java.lang.reflect.Field;

public class RNActivityDelegate extends ReactActivityDelegate  {
  private ReactDelegate mReactDelegate;
  private ReactNativeHost mReactNativeHost;
  private static final boolean enableCache = false;

  public RNActivityDelegate(Activity activity, String mainComponentName) {
    super(activity, mainComponentName);
    mReactNativeHost = getReactNativeHost();
  }

  public RNActivityDelegate(ReactActivity activity, String mainComponentName) {
    super(activity, mainComponentName);
    mReactNativeHost = getReactNativeHost();
  }

  @Override
  protected void onCreate(Bundle bundle) {
    String moduleName = bundle.getString("moduleName");
    Bundle params = bundle.getBundle("params");

    if (mReactDelegate == null) {
      if (enableCache) {
        mReactDelegate =
                new ReactDelegate(getPlainActivity(), mReactNativeHost, moduleName, params) {
                  private ReactRootView mReactRootView;

                  @Override
                  protected ReactRootView createRootView() {
                    return RNActivityDelegate.this.createRootView();
                  }

                  @Override
                  public void loadApp(String appKey) {
                    if (mReactRootView != null) {
                      throw new IllegalStateException("Cannot loadApp while app is already running.");
                    }
                    mReactRootView = RNCacheViewManager.getRootView(appKey);
                    try {
                      if (mReactRootView == null) {
                        // 2.缓存中不存在RootView,直接创建
                        mReactRootView = createRootView();
                        mReactRootView.startReactApplication(
                                mReactNativeHost.getReactInstanceManager(), appKey, params);
                      }
                      ViewParent viewParent = mReactRootView.getParent();
                      if (viewParent != null) {
                        ViewGroup vp = (ViewGroup) viewParent;
                        vp.removeView(mReactRootView);
                      }
                    } catch (Exception err) {
                      err.printStackTrace();
                    }
                  }

                  @Override
                  public ReactRootView getReactRootView() {
                    return mReactRootView;
                  }
                };
      } else {
        mReactDelegate = new ReactDelegate(this.getPlainActivity(), mReactNativeHost, moduleName, params) {
          protected ReactRootView createRootView() {
            return RNActivityDelegate.this.createRootView();
          }
        };
      }
    }

    try {
      // 反射替换父类属性
      Field privateReactDelegateField = ReactActivityDelegate.class.getDeclaredField("mReactDelegate");
      privateReactDelegateField.setAccessible(true);
      privateReactDelegateField.set(this, mReactDelegate);
    } catch (Exception err) {
      err.printStackTrace();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void loadApp(String appKey) {
    super.loadApp(appKey);
  }

  @Override
  protected ReactNativeHost getReactNativeHost() {
    // todo
    return RNApplication.mReactNativeHost;
  }

}
