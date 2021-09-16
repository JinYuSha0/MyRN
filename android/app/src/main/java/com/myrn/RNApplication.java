package com.myrn;

import android.app.Activity;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import com.facebook.react.bridge.JSIModulePackage;
import com.myrn.constant.StorageKey;
import com.myrn.entity.Component;
import com.myrn.utils.FileUtil;
import com.myrn.utils.Preferences;
import com.myrn.utils.RequestManager;
import com.myrn.utils.download.DownloadProgressListener;
import com.myrn.utils.download.DownloadTask;
import com.swmansion.reanimated.ReanimatedJSIModulePackage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class RNApplication extends Application implements ReactApplication {

  public static ReactNativeHost mReactNativeHost;
  public static final Boolean isDebug = false;

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
    return getReactNativeHost(isDebug,RNApplication.this, null);
  }

  private ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
    @Override
    public void onActivityStarted(@NonNull Activity activity) {
      checkUpdate();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
  };

  @Override
  public void onCreate() {
    super.onCreate();
    registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    getReactNativeHost();
    Preferences.init(this);
    RNDBHelper.init(this);
    initDB();
    SoLoader.init(this, /* native exopackage */ false);
    initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
  }

  private void initDB() {
    Boolean isInit = (Boolean) Preferences.getValueByKey(StorageKey.INIT_DB,Boolean.class);
    if (!isInit) {
      try {
        InputStream is = this.getAssets().open("appSetting.json");
        String json = FileUtil.convertStream2String(is);
        JSONObject jsonObject = new JSONObject(json);
        JSONObject componentsObj = jsonObject.getJSONObject("components");
        Long publishTime = (Long) jsonObject.get("timestamp");
        Iterator iterator = componentsObj.keys();
        ArrayList<ContentValues> contentValuesArr = new ArrayList<>();
        while (iterator.hasNext()) {
          String key = (String) iterator.next();
          JSONObject value = (JSONObject) componentsObj.get(key);
          String hash = (String) value.get("hash");
          String componentName = null;
          try {
            componentName = (String) value.get("componentName");
          } catch (Exception ignore) {}
          String filePath = "assets://" + key;
          contentValuesArr.add(RNDBHelper.createContentValues(key,componentName,0,hash,filePath,publishTime));
        }
        RNDBHelper.insertRows(contentValuesArr);
        Preferences.storageKV(StorageKey.INIT_DB,true);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  /**
   * 检查是否有新的模块需要下载
   */
  private void checkUpdate() {
    try {
      final File downloadPath = this.getExternalFilesDir(null);
      final Context mContext = this;
      ArrayList<RNDBHelper.Result> results = RNDBHelper.selectAll();
      final HashMap<String, String> resultMap = new HashMap<>();
      for (int i = 0; i < results.size(); i++) {
        RNDBHelper.Result curr = results.get(i);
        if (curr.ComponentName != null) {
          resultMap.put(curr.ComponentName, curr.Hash);
        }
      }
      RequestManager.getInstance(this).Get("/rn/checkUpdate", new HashMap<String, String>(), new RequestManager.RequestCallBack<ArrayList<Component>>() {
        @Override
        public void onFailure(RequestManager.MyResponse<Object> error) {
          System.out.printf(error.message);
        }

        @Override
        public void onSuccess(RequestManager.MyResponse<ArrayList<Component>> data) {
          for (int i = 0; i < data.data.size(); i++) {
            Component curr = data.data.get(i);
//            if (resultMap.get(curr.componentName).equals(curr.hash)) {
//              new Thread(new DownloadTask(
//                      mContext,
//                      curr.downloadUrl,
//                      String.format("%s-%s.zip",curr.componentName,curr.hash),
//                      downloadPath,
//                      new DownloadProgressListener() {
//                        @Override
//                        public void onDownloadSize(int downloadedSize) {
//                          System.out.println(downloadedSize);
//                        }
//
//                        @Override
//                        public void onDownloadFailure(Exception e) {
//                          System.out.println("download failure");
//                        }
//
//                        @Override
//                        public void onDownLoadComplete(File file) {
//                          System.out.println("download success");
//                        }
//                      })
//              ).start();
//            }
          }
        }
      }).request();
    } catch (Exception e) {
      e.printStackTrace();
    }
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
