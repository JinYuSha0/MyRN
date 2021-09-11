package com.myrn.iface;

import android.content.ContentValues;

import com.myrn.RNDBHelper;

import java.util.ArrayList;

public interface ReactNativeDB {
  public int getRowsCount();

  public void insertRow(ContentValues contentValues);

  public void insertRows(ArrayList<ContentValues> contentValuesArrList);

  public ContentValues createContentValues(String ComponentName, String version, String Hash, String Filepath);

  public RNDBHelper.Result selectByComponentName(String componentName);
}
