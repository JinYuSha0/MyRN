package com.myrn;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.myrn.iface.ReactNativeDB;

import java.util.ArrayList;

public class RNDBHelper extends SQLiteOpenHelper implements ReactNativeDB {
  private static final int DB_VERSION = 1;
  private static final String DB_NAME = "rn.db";
  private static final String TABLE_NAME = "Version";

  public RNDBHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    String sql = "create table if not exists " + TABLE_NAME + " (BundleName text, Version interge, Hash text, Filepath text, PublishTime interge, InstallTime integer, primary key(BundleName, Version))";
    sqLiteDatabase.execSQL(sql);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
    sqLiteDatabase.execSQL(sql);
    onCreate(sqLiteDatabase);
  }

  @Override
  public int getRowsCount() {
    Cursor cursor = this.getReadableDatabase().rawQuery("select count(*) from " + TABLE_NAME,null);
    cursor.moveToFirst();
    int count = cursor.getInt(0);
    cursor.close();
    return count;
  }

  @Override
  public void insertRow(ContentValues contentValues) {
    try {
      this.getWritableDatabase().insertOrThrow(TABLE_NAME, null, contentValues);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  @Override
  public void insertRows(ArrayList<ContentValues> contentValuesArrayList) {
    for (ContentValues contentValues : contentValuesArrayList) {
      this.insertRow(contentValues);
    }
  }

  @Override
  public ContentValues createContentValues(String BundleName, Integer Version, String Hash, String Filepath, Long PublishTime) {
    ContentValues contentValues = new ContentValues();
    contentValues.put("BundleName", BundleName);
    contentValues.put("Version", Version);
    contentValues.put("Hash", Hash);
    contentValues.put("Filepath", Filepath);
    contentValues.put("PublishTime", PublishTime);
    contentValues.put("InstallTime", System.currentTimeMillis());
    return contentValues;
  }

  @Override
  public RNDBHelper.Result selectByBundleName(String BundleName) {
    Cursor cursor = this.getReadableDatabase().query(TABLE_NAME,new String[]{"*"},"BundleName = ?", new String[]{BundleName}, null,null,null);
    Result result = null;
    if (cursor.moveToNext()) {
      result = new Result(BundleName,
              cursor.getInt(cursor.getColumnIndex("Version")),
              cursor.getString(cursor.getColumnIndex("Hash")),
              cursor.getString(cursor.getColumnIndex("Filepath")),
              cursor.getLong(cursor.getColumnIndex("PublishTime")),
              cursor.getLong(cursor.getColumnIndex("InstallTime")));
    }
    return result;
  }

  public class Result {
    String BundleName;
    Integer Version;
    String Hash;
    String FilePath;
    Long PublishTime;
    Long InstallTime;

    public Result(String bundleName, Integer version, String hash, String filePath, Long publishTime, Long installTime) {
      BundleName = bundleName;
      Version = version;
      Hash = hash;
      FilePath = filePath;
      PublishTime = publishTime;
      InstallTime = installTime;
    }
  }
}
