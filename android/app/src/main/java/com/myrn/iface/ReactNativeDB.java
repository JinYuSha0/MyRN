package com.myrn.iface;

import android.content.ContentValues;

import com.myrn.RNDBHelper;

import java.util.ArrayList;

public interface ReactNativeDB {
  public int getRowsCount();

  public void insertRow(ContentValues contentValues);

  public void insertRows(ArrayList<ContentValues> contentValuesArrList);

  public ContentValues createContentValues(String ComponentName, Integer version, String Hash, String Filepath, Long PublishTime);

  public RNDBHelper.Result selectByBundleName(String componentName);
}
