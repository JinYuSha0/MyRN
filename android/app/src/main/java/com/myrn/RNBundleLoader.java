package com.myrn;

import android.content.Context;

import androidx.annotation.Nullable;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;

import java.util.HashSet;
import java.util.Set;

public class RNBundleLoader {
  public static Set<String> sLoadedBundle = new HashSet<>();

  @Nullable
  public static CatalystInstance getCatalystInstance(ReactNativeHost host) {
    ReactInstanceManager manager = host.getReactInstanceManager();
    if (manager == null) {
      return null;
    }

    ReactContext context = manager.getCurrentReactContext();
    if (context == null) {
      return null;
    }

    if (!context.hasActiveReactInstance()) {
      return null;
    }

    return context.getCatalystInstance();
  }

  public static void loadScript(Context context, CatalystInstance instance, String filePath, boolean isSync) {
    if (filePath.startsWith("assets://")) {
      loadScriptFromAsset(context, instance, filePath, isSync);
    }
  }

  public static void loadScriptFromAsset(Context context, CatalystInstance instance, String assetName, boolean isSync) {
    if (sLoadedBundle.contains(assetName)) {
      return;
    }
    String source = assetName;
    if(!assetName.startsWith("assets://")) {
      source = "assets://" + assetName;
    }
    instance.loadScriptFromAssets(context.getAssets(), source, isSync);
    sLoadedBundle.add(assetName);
  }
}
