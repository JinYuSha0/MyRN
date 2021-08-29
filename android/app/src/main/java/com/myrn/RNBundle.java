package com.myrn;

import android.os.Bundle;

import androidx.annotation.Nullable;

public class RNBundle {
  public String bundlePath;
  public String bundleURL;
  public RNActivity.BundleType bundleType;
  public String moduleName;
  public Bundle params;

  RNBundle() {}

  RNBundle(String _bundlePath, String _bundleURL, RNActivity.BundleType _bundleType, String _mainComponentName, @Nullable Bundle _params) {
    bundlePath = _bundlePath;
    bundleURL = _bundleURL;
    bundleType = _bundleType;
    moduleName = _mainComponentName;
    params = _params;
  }

  public static RNBundle genAssetsBundle(String _bundlePath, String _mainComponentName, @Nullable Bundle _params) {
    return new RNBundle(_bundlePath, "", RNActivity.BundleType.ASSETS, _mainComponentName, _params);
  }

  public static RNBundle genNetworkBundle(String _bundleURL, String _mainComponentName, @Nullable Bundle _params) {
    return new RNBundle("", _bundleURL, RNActivity.BundleType.NETWORK, _mainComponentName, _params);
  }

  public static RNBundle genFileBundle(String _bundlePath, String _mainComponentName, @Nullable Bundle _params) {
    return new RNBundle(_bundlePath, "", RNActivity.BundleType.FILE, _mainComponentName, _params);
  }

  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putString("bundlePath", bundlePath);
    bundle.putString("bundleURL", bundleURL);
    bundle.putString("bundleType", MainActivity.bundleType2String(bundleType));
    bundle.putString("moduleName", moduleName);
    bundle.putBundle("params", params);
    return bundle;
  }
}