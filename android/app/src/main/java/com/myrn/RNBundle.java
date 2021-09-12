package com.myrn;

import android.os.Bundle;

import androidx.annotation.Nullable;

public class RNBundle {
  public String bundleName;
  public String moduleName;
  public Bundle params;

  RNBundle() {}

  RNBundle(String _bundleName, String _mainComponentName, @Nullable Bundle _params) {
    bundleName = _bundleName;
    moduleName = _mainComponentName;
    params = _params;
  }

  public static RNBundle genBundle(String _bundleName, String _mainComponentName, @Nullable Bundle _params) {
    return new RNBundle(_bundleName, _mainComponentName, _params);
  }

  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putString("bundleName", bundleName);
    bundle.putString("moduleName", moduleName);
    bundle.putBundle("params", params);
    return bundle;
  }
}