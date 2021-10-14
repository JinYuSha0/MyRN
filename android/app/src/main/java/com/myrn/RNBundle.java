package com.myrn;

import android.os.Bundle;

import androidx.annotation.Nullable;

public class RNBundle {
  public String moduleName;
  public Bundle params;

  RNBundle() {}

  RNBundle(String _mainComponentName, @Nullable Bundle _params) {
    moduleName = _mainComponentName;
    params = _params;
  }

  public static RNBundle genBundle( String _mainComponentName, @Nullable Bundle _params) {
    return new RNBundle(_mainComponentName, _params);
  }

  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putString("moduleName", moduleName);
    bundle.putBundle("params", params);
    return bundle;
  }
}