package com.myrn;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RNBridgePackage implements ReactPackage {
  @NonNull
  @NotNull
  @Override
  public List<NativeModule> createNativeModules(@NonNull @NotNull ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<NativeModule>();
    modules.add(new RNBridge());
    return modules;
  }

  @NonNull
  @NotNull
  @Override
  public List<ViewManager> createViewManagers(@NonNull @NotNull ReactApplicationContext reactContext) {
    return Collections.emptyList();
  }
}
