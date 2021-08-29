package com.myrn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends RNActivity {

  public static final RNActivity.BundleType string2BundleType(String bundleType) {
    switch (bundleType.toLowerCase()) {
      case "file":
        return BundleType.FILE;
      case "network":
        return BundleType.NETWORK;
      case "assets":
      default:
        return BundleType.ASSETS;
    }
  }

  public static final String bundleType2String(RNActivity.BundleType bundleType) {
    if (bundleType == BundleType.FILE) {
      return "file";
    } else if (bundleType == BundleType.NETWORK) {
      return "network";
    } else {
      return "assets";
    }
  }

  protected String getDefaultBundlePath() {
    return "home.buz.android.bundle";
  }

  protected String getDefaultComponentName() {
    return "Home";
  }

  protected Bundle getDefaultParams() {
    Bundle bundle = new Bundle();
    return bundle;
  }

  @Override
  public RNBundle getBundle() {
    Bundle bundle;
    if (getIntent() == null) {
      bundle = new Bundle();
    } else {
      bundle = getIntent().getExtras();
    }
    if (bundle == null) bundle = new Bundle();
    String bundleType = bundle.getString("bundleType",null);
    Bundle params = bundle.getBundle("params");
    if (params == null) params = new Bundle();
    params.putAll(getDefaultParams());
    if (bundleType == null) {
      return RNBundle.genAssetsBundle(getDefaultBundlePath(), getDefaultComponentName(), params);
    } else {
      String bundlePath = bundle.getString("bundlePath");
      String bundleURL = bundle.getString("bundleURL");
      String moduleName = bundle.getString("moduleName");
      return new RNBundle(bundlePath, bundleURL, string2BundleType(bundleType), moduleName, params);
    }
  }
}
