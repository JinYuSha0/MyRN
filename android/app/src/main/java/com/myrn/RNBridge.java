package com.myrn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myrn.constant.EventName;
import com.myrn.entity.Component;
import com.myrn.entity.ComponentSetting;
import com.myrn.iface.Callback;
import com.myrn.iface.MyResponse;
import com.myrn.utils.FileUtil;
import com.myrn.utils.PhoneInfo;
import com.myrn.utils.RNConvert;
import com.myrn.utils.RequestManager;
import com.myrn.utils.download.DownloadProgressListener;
import com.myrn.utils.download.DownloadTask;

import net.lingala.zip4j.ZipFile;

import static com.myrn.utils.MathUtil.getRandomString;

public class RNBridge extends ReactContextBaseJavaModule {
  public static final String PREFIX = getRandomString(6) + "_";

  @NonNull
  @NotNull
  @Override
  public String getName() {
    return "RNBridge";
  }

  @Nullable
  @org.jetbrains.annotations.Nullable
  @Override
  public Map<String, Object> getConstants() {
    HashMap<String, Object> map = new HashMap<>();
    map.put("prefix", PREFIX);
    map.put("model", PhoneInfo.getPhoneModel());
    return map;
  }

  @ReactMethod
  public void log(String content) {
    Log.d("MY_RN_LOG", content);
  }

  @ReactMethod
  public void getAllComponent(Promise promise) {
    WritableArray array = Arguments.createArray();
    ArrayList<RNDBHelper.Result> results = RNDBHelper.selectAll();
    for (RNDBHelper.Result result : results) {
      array.pushMap((WritableMap) RNConvert.convert(result));
    }
    promise.resolve(array);
  }

  @ReactMethod
  public void openComponent(String moduleName, @Nullable Integer statusBarMode) {
    Activity activity = getActivity();
    if (activity == null) return;
    Intent intent = new Intent(activity, MainActivity.class);
    Bundle params = new Bundle();
    params.putBoolean("goBack", true);
    Bundle bundle = createBundle(moduleName, statusBarMode, params);
    intent.putExtras(bundle);
    activity.startActivity(intent);
  }

  @ReactMethod
  public void checkUpdate(Promise promise) {
    RNBridge.checkUpdate(getActivity(), new Callback() {
      @Override
      public void onSuccess(Object result) {
        promise.resolve(RNConvert.convert(result));
      }

      @Override
      public void onError(String errorMsg) {
        promise.reject(null, errorMsg);
      }
    });
  }

  @ReactMethod
  public void goBack() {
    getActivity().finish();
  }

  /**
   * 检查是否有新的模块需要下载
   */
  public static void checkUpdate(Context ctx, @Nullable Callback callback) {
    // debug
    if (RNApplication.mReactNativeHost != null && RNApplication.mReactNativeHost.getUseDeveloperSupport()) return;
    try {
      final File downloadPath = ctx.getExternalFilesDir(null);
      final Context mContext = ctx;
      final HashMap<String, RNDBHelper.Result> componentMap = RNDBHelper.selectAllMap();
      RNBridge.sendEventInner(EventName.CHECK_UPDATE_START, null);
      HashMap<String, String> params = new HashMap<>();
      params.put("platform", "android");
      params.put("commonHash", componentMap.get("common").Hash);
      RequestManager.getInstance(ctx).Get("/rn/checkUpdate", params, new RequestManager.RequestCallBack<MyResponse<ArrayList<Component>>, MyResponse<Object>>() {
        @Override
        public void onFailure(MyResponse<Object> error, Exception exception) {
          String cause;
          if (exception != null) {
            cause = exception.getMessage();
          } else {
            cause = error.message;
          }
          if (callback != null) callback.onError(cause);
          RNBridge.sendEventInner(EventName.CHECK_UPDATE_FAILURE, cause);
        }

        @Override
        public void onSuccess(MyResponse<ArrayList<Component>> result) {
          if (callback != null) callback.onSuccess(result);
          RNBridge.sendEventInner(EventName.CHECK_UPDATE_SUCCESS, result);
          for (int i = 0; i < result.data.size(); i++) {
            final Component newComponent = result.data.get(i);
            final RNDBHelper.Result oldComponent = componentMap.get(newComponent.componentName);
            // 如果hash不相同 且版本大于当前版本 下载新的bundle包
            if (!oldComponent.Hash.equals(newComponent.hash) && newComponent.version > oldComponent.Version) {
              RNBridge.sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS,newComponent);
              new Thread(new DownloadTask(
                mContext,
                newComponent.downloadUrl,
                String.format("%s-%s.zip",newComponent.componentName,newComponent.hash),
                downloadPath,
                new DownloadProgressListener() {
                  @Override
                  public void onDownloadSize(int downloadedSize, int fileSize) {
                    WritableMap progress = Arguments.createMap();
                    progress.putString("componentName",newComponent.componentName);
                    progress.putDouble("progress", (double) downloadedSize / (double) fileSize);
                    RNBridge.sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_PROGRESS,progress);
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
                      setupComponent(ctx,String.format("%s%s",dest,newComponent.hash),newComponent.version);
                      RNBridge.sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS_SUCCESS,newComponent);
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
  private static void setupComponent(Context ctx,String componentDir,Integer version) {
    try {
      File settingJSONFile = new File(String.format("%s/%s",componentDir,"setting.json"));
      String settingJSON = FileUtil.readFile(settingJSONFile);
      ComponentSetting componentSetting = new Gson().fromJson(settingJSON, new TypeToken<ComponentSetting>() {}.getType());
      String bundleFilePath = String.format("%s/%s", componentDir, componentSetting.bundleName);
      if (FileUtil.fileExists(bundleFilePath)) {
        String saveBundleFilePath = bundleFilePath.replaceAll(ctx.getExternalFilesDir(null).getAbsolutePath() + "/","file://");
        RNDBHelper.insertRow(RNDBHelper.createContentValues(
                componentSetting.bundleName,
                componentSetting.componentName,
                version,
                componentSetting.hash,
                saveBundleFilePath,
                componentSetting.timestamp
        ));
        RNBridge.sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS_APPLY,componentSetting.componentName);
        // 立即应用新模块
        if (!RNActivity.isExistsModule(componentSetting.componentName)) {
          RNBundleLoader.loadScriptFromFile(ctx,RNBundleLoader.getCatalystInstance(RNApplication.mReactNativeHost),bundleFilePath,false);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Bundle createBundle(String moduleName, @Nullable Integer statusBarMode, @Nullable Bundle params) {
    Bundle bundle = new Bundle();
    bundle.putString("moduleName", moduleName);
    bundle.putInt("statusBarMode", statusBarMode == null ? 0 : statusBarMode);
    if (params != null) bundle.putAll(params);
    return bundle;
  }

  public static void sendEvent(String eventName, Object eventData) {
    ReactContext reactContext = RNApplication.mReactNativeHost.getReactInstanceManager().getCurrentReactContext();
    eventData = RNConvert.convert(eventData);
    if (reactContext != null) {
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName,eventData);
    }
  }

  public static void sendEventInner(String eventName, Object eventData) {
    sendEvent(PREFIX+eventName,eventData);
  }

  public static Activity getActivity () {
    try {
      Activity lastActivity = RNActivity.getActivity();
      if (lastActivity != null) return lastActivity;
      @SuppressLint("PrivateApi") Class activityThreadClass = Class.forName("android.app.ActivityThread");
      Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(
              null);
      Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
      activitiesField.setAccessible(true);
      Map activities = (Map) activitiesField.get(activityThread);
      for (Object activityRecord : activities.values()) {
        Class activityRecordClass = activityRecord.getClass();
        Field pausedField = activityRecordClass.getDeclaredField("paused");
        pausedField.setAccessible(true);
        if (!pausedField.getBoolean(activityRecord)) {
          Field activityField = activityRecordClass.getDeclaredField("activity");
          activityField.setAccessible(true);
          Activity activity = (Activity) activityField.get(activityRecord);
          return activity;
        }
      }
    } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }
}
