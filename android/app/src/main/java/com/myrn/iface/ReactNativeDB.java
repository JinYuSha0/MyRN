package com.myrn.iface;

import android.content.ContentValues;
import android.database.Cursor;

import com.myrn.RNDBHelper;

import java.util.ArrayList;

public interface ReactNativeDB {
  public int getRowsCount();

  public void insertRow(ContentValues contentValues);

  public void insertRows(ArrayList<ContentValues> contentValuesArrList);

  public ContentValues createContentValues(String BundleName, String ComponentName, Integer version, String Hash, String Filepath, Long PublishTime);

  public RNDBHelper.Result selectByBundleName(String componentName);

  public ArrayList<RNDBHelper.Result> selectAll();

  public RNDBHelper.Result parseCursor(Cursor cursor);
}
