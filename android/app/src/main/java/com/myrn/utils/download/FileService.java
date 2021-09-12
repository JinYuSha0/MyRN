package com.myrn.utils.download;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

public class FileService {
  //声明数据库管理器
  private DownloadDBHelper downloadDBHelper;

  //在构造方法中根据上下文对象实例化数据库管理器
  public FileService(Context context) {
    downloadDBHelper = new DownloadDBHelper(context);
  }

  /**
   * 获得指定URI的每条线程已经下载的文件长度
   * @param path
   * @return
   * */
  public Map<Integer, Integer> getData(String path)
  {
    //获得可读数据库句柄,通常内部实现返回的其实都是可写的数据库句柄
    SQLiteDatabase db = downloadDBHelper.getReadableDatabase();
    //根据下载的路径查询所有现场的下载数据,返回的Cursor指向第一条记录之前
    Cursor cursor = db.rawQuery("select threadid, downlength from filedownlog where downpath=?",
            new String[]{path});
    //建立一个哈希表用于存放每条线程已下载的文件长度
    Map<Integer,Integer> data = new HashMap<Integer, Integer>();
    //从第一条记录开始遍历Cursor对象
    cursor.moveToFirst();
    while(cursor.moveToNext())
    {
      //把线程id与该线程已下载的长度存放到data哈希表中
      data.put(cursor.getInt(0), cursor.getInt(1));
      data.put(cursor.getInt(cursor.getColumnIndexOrThrow("threadid")),
              cursor.getInt(cursor.getColumnIndexOrThrow("downlength")));
    }
    cursor.close();//关闭cursor,释放资源;
    db.close();
    return data;
  }

  /**
   * 保存每条线程已经下载的文件长度
   * @param path 下载的路径
   * @param map 现在的di和已经下载的长度的集合
   */
  public void save(String path,Map<Integer,Integer> map)
  {
    SQLiteDatabase db = downloadDBHelper.getWritableDatabase();
    //开启事务,因为此处需要插入多条数据
    db.beginTransaction();
    try{
      //使用增强for循环遍历数据集合
      for(Map.Entry<Integer, Integer> entry : map.entrySet())
      {
        //插入特定下载路径特定线程ID已经下载的数据
        db.execSQL("insert into filedownlog(downpath, threadid, downlength) values(?,?,?)",
                new Object[]{path, entry.getKey(), entry.getValue()});
      }
      //设置一个事务成功的标志,如果成功就提交事务,如果没调用该方法的话那么事务回滚
      //就是上面的数据库操作撤销
      db.setTransactionSuccessful();
    }finally{
      //结束一个事务
      db.endTransaction();
    }
    db.close();
  }

  /**
   * 实时更新每条线程已经下载的文件长度
   * @param path
   * @param
   */
  public void update(String path,int threadId,int pos)
  {
    SQLiteDatabase db = downloadDBHelper.getWritableDatabase();
    //更新特定下载路径下特定线程已下载的文件长度
    db.execSQL("update filedownlog set downlength=? where downpath=? and threadid=?",
            new Object[]{pos, path, threadId});
    db.close();
  }


  /**
   *当文件下载完成后，删除对应的下载记录
   *@param path
   */
  public void delete(String path)
  {
    SQLiteDatabase db = downloadDBHelper.getWritableDatabase();
    db.execSQL("delete from filedownlog where downpath=?", new Object[]{path});
    db.close();
  }
}
