package com.myrn;

import android.os.Bundle;

import com.myrn.constant.StatusBar;

public class MainActivity extends RNActivity {
  protected String getDefaultBundleName() {
    return "home.buz.android.bundle";
  }

  protected String getDefaultComponentName() {
    return "Home";
  }

  protected Bundle getDefaultParams() {
    Bundle bundle = new Bundle();
    bundle.putInt("statusBarMode", StatusBar.lightMode);
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
    Bundle params = getDefaultParams();
    Bundle extraParams = bundle.getBundle("params");
    if (extraParams == null) extraParams = new Bundle();
    params.putAll(bundle);
    params.putAll(extraParams);
    String bundleName = bundle.getString("bundleName", getDefaultBundleName());
    String moduleName = bundle.getString("moduleName", getDefaultComponentName());
    return new RNBundle(bundleName, moduleName, params);
  }
}
