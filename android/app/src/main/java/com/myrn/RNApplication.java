package com.myrn;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.Nullable;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactInstanceManagerBuilder;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.ReactMarker;
import com.facebook.react.bridge.ReactMarkerConstants;
import com.facebook.react.common.LifecycleState;
import com.facebook.soloader.SoLoader;
import java.lang.reflect.InvocationTargetException;
import com.facebook.react.bridge.JSIModulePackage;
import com.swmansion.reanimated.ReanimatedJSIModulePackage;
import java.util.List;

public class RNApplication extends Application implements ReactApplication {

  public static ReactNativeHost mReactNativeHost;

  public static final ReactNativeHost getReactNativeHost(Boolean isDebug, Application application, @Nullable Activity activity) {
    if (mReactNativeHost == null) {
      mReactNativeHost = new ReactNativeHost(application) {
        @Override
        public boolean getUseDeveloperSupport() {
          return isDebug;
        }

        @Override
        protected List<ReactPackage> getPackages() {
          @SuppressWarnings("UnnecessaryLocalVariable")
          List<ReactPackage> packages = new PackageList(this).getPackages();
          // Packages that cannot be autolinked yet can be added manually here, for example:
          // packages.add(new MyReactNativePackage());
          return packages;
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        @Override
        protected String getBundleAssetName() {
          // 公共包
          return "common.android.bundle";
        }

        @Override
        protected String getJSMainModuleName() {
          return "index";
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        @Override
        protected JSIModulePackage getJSIModulePackage() {
          return new ReanimatedJSIModulePackage();
        }

        @Override
        protected ReactInstanceManager createReactInstanceManager() {
          ReactMarker.logMarker(ReactMarkerConstants.BUILD_REACT_INSTANCE_MANAGER_START);
          ReactInstanceManagerBuilder builder =
                  ReactInstanceManager.builder()
                          .setApplication(application)
                          .setJSMainModulePath(getJSMainModuleName())
                          .setUseDeveloperSupport(getUseDeveloperSupport())
                          .setRedBoxHandler(getRedBoxHandler())
                          .setJavaScriptExecutorFactory(getJavaScriptExecutorFactory())
                          .setUIImplementationProvider(getUIImplementationProvider())
                          .setJSIModulesPackage(getJSIModulePackage())
                          .setInitialLifecycleState(LifecycleState.BEFORE_CREATE);

          if (activity != null) {
            builder.setCurrentActivity(activity);
          }

          for (ReactPackage reactPackage : getPackages()) {
            builder.addPackage(reactPackage);
          }
          builder.addPackage(new RNBridgePackage());

          String jsBundleFile = getJSBundleFile();
          if (jsBundleFile != null) {
            builder.setJSBundleFile(jsBundleFile);
          } else {
            builder.setBundleAssetName(Assertions.assertNotNull(getBundleAssetName()));
          }

          ReactInstanceManager reactInstanceManager = builder.build();
          ReactMarker.logMarker(ReactMarkerConstants.BUILD_REACT_INSTANCE_MANAGER_END);

          return reactInstanceManager;
        }
      };
    }
    return mReactNativeHost;
  }

  @Override
  public ReactNativeHost getReactNativeHost() {
    return getReactNativeHost(false,RNApplication.this, null);
//    return getReactNativeHost(BuildConfig.DEBUG,RNApplication.this, null);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    getReactNativeHost();
    SoLoader.init(this, /* native exopackage */ false);
    initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
  }

  /**
   * Loads Flipper in React Native templates. Call this in the onCreate method with something like
   * initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
   *
   * @param context
   * @param reactInstanceManager
   */
  private static void initializeFlipper(
      Context context, ReactInstanceManager reactInstanceManager) {
    if (BuildConfig.DEBUG) {
      try {
        /*
         We use reflection here to pick up the class that initializes Flipper,
        since Flipper library is not available in release mode
        */
        Class<?> aClass = Class.forName("com.myrn.ReactNativeFlipper");
        aClass
            .getMethod("initializeFlipper", Context.class, ReactInstanceManager.class)
            .invoke(null, context, reactInstanceManager);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }
}
