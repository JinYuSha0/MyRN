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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import com.facebook.react.bridge.JSIModulePackage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myrn.constant.EventName;
import com.myrn.constant.StorageKey;
import com.myrn.entity.Component;
import com.myrn.entity.ComponentSetting;
import com.myrn.entity.VersionInfo;
import com.myrn.iface.MyResponse;
import com.myrn.utils.FileUtil;
import com.myrn.utils.Preferences;
import com.myrn.utils.RequestManager;
import com.myrn.utils.download.DownloadProgressListener;
import com.myrn.utils.download.DownloadTask;
import com.swmansion.reanimated.ReanimatedJSIModulePackage;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipInputStream;

public class RNApplication extends Application implements ReactApplication {

  public static ReactNativeHost mReactNativeHost;
  public static final Boolean isDebug = false;
  private Boolean isBackGround = false;

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
      if (isBackGround) {
        isBackGround = false;
        checkUpdate();
      }
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

  @Override
  public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    if (level == TRIM_MEMORY_UI_HIDDEN) {
      isBackGround = true;
    }
  }

  /**
   * 初始化数据库
   */
  private void initDB() {
    Boolean isInit = (Boolean) Preferences.getValueByKey(StorageKey.INIT_DB,Boolean.class);
    if (!isInit) {
      try {
        String json = FileUtil.readFileFromAssets(this,"appSetting.json");
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
    // debug
    if (mReactNativeHost != null && mReactNativeHost.getUseDeveloperSupport()) return;
    try {
      final File downloadPath = this.getExternalFilesDir(null);
      final Context mContext = this;
      ArrayList<RNDBHelper.Result> results = RNDBHelper.selectAll();
      final HashMap<String, VersionInfo> componentMap = new HashMap<>();
      for (int i = 0; i < results.size(); i++) {
        RNDBHelper.Result curr = results.get(i);
        if (curr.ComponentName != null) {
          VersionInfo info = new VersionInfo(curr.Hash, curr.Version);
          componentMap.put(curr.ComponentName, info);
        }
      }
      RNBridge.sendEventInner(EventName.CHECK_UPDATE_START, null);
      RequestManager.getInstance(this).Get("/rn/checkUpdate", new HashMap<String, String>(), new RequestManager.RequestCallBack<MyResponse<ArrayList<Component>>, MyResponse<Object>>() {
        @Override
        public void onFailure(MyResponse<Object> error, Exception exception) {
          String cause;
          if (exception != null) {
            cause = exception.getMessage();
          } else {
            cause = error.message;
          }
          RNBridge.sendEventInner(EventName.CHECK_UPDATE_FAILURE, cause);
        }

        @Override
        public void onSuccess(MyResponse<ArrayList<Component>> result) {
          RNBridge.sendEventInner(EventName.CHECK_UPDATE_SUCCESS, result);
          for (int i = 0; i < result.data.size(); i++) {
            final Component newComponent = result.data.get(i);
            final VersionInfo oldComponent = componentMap.get(newComponent.componentName);
            // 如果hash不相同 且版本大于当前版本 下载新的bundle包
            if (!oldComponent.hash.equals(newComponent.hash) && newComponent.version > oldComponent.version) {
              RNBridge.sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS,null);
              new Thread(new DownloadTask(
                      mContext,
                      newComponent.downloadUrl,
                      String.format("%s-%s.zip",newComponent.componentName,newComponent.hash),
                      downloadPath,
                      new DownloadProgressListener() {
                        @Override
                        public void onDownloadSize(int downloadedSize) {
                        }

                        @Override
                        public void onDownloadFailure(Exception e) {
                          RNBridge.sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS_FAILURE,e.getMessage());
                        }

                        @Override
                        public void onDownLoadComplete(File originFile) {
                          File file = new File(String.format("%s/%s",downloadPath.getAbsolutePath(),newComponent.hash));
                          try {
                            originFile.renameTo(file);
                            String dest = String.format("%s/%s/",downloadPath.getAbsolutePath(),newComponent.componentName);
                            ZipFile zipFile = new ZipFile(file);
                            zipFile.extractAll(dest);
                            setupComponent(String.format("%s%s",dest,newComponent.hash),newComponent.version);
                            RNBridge.sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS_SUCCESS,null);
                          } catch (Exception e) {
                            e.printStackTrace();
                          } finally {
                            file.delete();
                          }
                        }
                      })
              ).start();
            }
          }
        }

        @Override
        public Type getType(Boolean isFailure) {
          if (!isFailure) {
            return new TypeToken<MyResponse<ArrayList<Component>>>() {}.getType();
          } else {
            return new TypeToken<MyResponse<Object>>() {}.getType();
          }
        }
      }).request();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * 应用下载完的组件
   * @param componentDir
   */
  private void setupComponent(String componentDir,Integer version) {
    try {
      File settingJSONFile = new File(String.format("%s/%s",componentDir,"setting.json"));
      String settingJSON = FileUtil.readFile(settingJSONFile);
      ComponentSetting componentSetting = new Gson().fromJson(settingJSON, new TypeToken<ComponentSetting>() {}.getType());
      String bundleFilePath = String.format("%s/%s", componentDir, componentSetting.bundleName);
      if (FileUtil.fileExists(bundleFilePath)) {
        String saveBundleFilePath = bundleFilePath.replaceAll(this.getExternalFilesDir(null).getAbsolutePath() + "/","file://");
        RNDBHelper.insertRow(RNDBHelper.createContentValues(
                componentSetting.bundleName,
                componentSetting.componentName,
                version,
                componentSetting.hash,
                saveBundleFilePath,
                componentSetting.timestamp
        ));
        // 立即应用新模块
        if (!RNActivity.isExistsModule(componentSetting.componentName)) {
          RNBundleLoader.loadScriptFromFile(this,RNBundleLoader.getCatalystInstance(mReactNativeHost),bundleFilePath,false);
        }
        RNBridge.sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS_APPLY,null);
      }
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
