package com.myrn.utils.download;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;


public class FileDownloadered {

  private static final String TAG = "文件下载类";  //设置一个查log时的一个标志
  private static final int RESPONSEOK = 200;    //设置响应码为200,代表访问成功
  private FileService fileService;        //获取本地数据库的业务Bean
  private boolean exited;             //停止下载的标志
  private Context context;            //程序的上下文对象
  private int downloadedSize = 0;               //已下载的文件长度
  private int fileSize = 0;           //开始的文件长度
  private DownloadThread[] threads;        //根据线程数设置下载的线程池
  private File saveFile;              //数据保存到本地的文件中
  private Map<Integer, Integer> data = new ConcurrentHashMap<Integer, Integer>();  //缓存个条线程的下载的长度
  private int block;                            //每条线程下载的长度
  private String downloadUrl;                   //下载的路径


  /**
   * 获取线程数
   */
  public int getThreadSize()
  {
    //return threads.length;
    return 0;
  }

  /**
   * 退出下载
   * */
  public void exit()
  {
    this.exited = true;    //将退出的标志设置为true;
  }

  public boolean getExited()
  {
    return this.exited;
  }

  /**
   * 获取文件的大小
   * */
  public int getFileSize()
  {
    return fileSize;
  }

  /**
   * 累计已下载的大小
   * 使用同步锁来解决并发的访问问题
   * */
  protected synchronized void append(int size)
  {
    //把实时下载的长度加入到总的下载长度中
    downloadedSize += size;
  }

  /**
   * 更新指定线程最后下载的位置
   * @param threadId 线程id
   * @param pos 最后下载的位置
   * */
  protected synchronized void update(int threadId,int pos)
  {
    //把指定线程id的线程赋予最新的下载长度,以前的值会被覆盖掉
    this.data.put(threadId, pos);
    //更新数据库中制定线程的下载长度
    this.fileService.update(this.downloadUrl, threadId, pos);
  }


  /**
   * 构建文件下载器
   * @param downloadUrl 下载路径
   * @param fileSaveDir 文件的保存目录
   * @param threadNum  下载线程数
   * @return
   */
  public FileDownloadered(Context context,String downloadUrl,String filename, File fileSaveDir,int threadNum)
  {
    try {
      this.context = context;     //获取上下文对象,赋值
      this.downloadUrl = downloadUrl;  //为下载路径赋值
      fileService = new FileService(this.context);   //实例化数据库操作的业务Bean类,需要传一个context值
      URL url = new URL(this.downloadUrl);     //根据下载路径实例化URL
      if(!fileSaveDir.exists()) fileSaveDir.mkdir();  //如果文件不存在的话指定目录,这里可创建多层目录
      this.threads = new DownloadThread[threadNum];   //根据下载的线程数量创建下载的线程池


      HttpURLConnection conn = (HttpURLConnection) url.openConnection();   //创建远程连接句柄,这里并未真正连接
      conn.setConnectTimeout(5000);      //设置连接超时事件为5秒
      conn.setRequestMethod("GET");      //设置请求方式为GET
      //设置用户端可以接收的媒体类型
      conn.setRequestProperty("Accept", "*/*");
      conn.setRequestProperty("Accept-Language", "zh-CN");  //设置用户语言
      conn.setRequestProperty("Referer", downloadUrl);    //设置请求的来源页面,便于服务端进行来源统计
      conn.setRequestProperty("Charset", "UTF-8");    //设置客户端编码
      conn.setRequestProperty("Connection", "Keep-Alive");  //设置connection的方式
      conn.connect();      //和远程资源建立正在的链接,但尚无返回的数据流
      printResponseHeader(conn);   //打印返回的Http的头字段集合
      //对返回的状态码进行判断,用于检查是否请求成功,返回200时执行下面的代码
      if(conn.getResponseCode() == RESPONSEOK)
      {
        this.fileSize = conn.getContentLength();  //根据响应获得文件大小
        if(this.fileSize <= 0)throw new RuntimeException("不知道文件大小");  //文件长度小于等于0时抛出运行时异常
        if (filename == null) filename = getFileName(conn);      //获取文件名称
        this.saveFile = new File(fileSaveDir,filename);  //根据文件保存目录和文件名保存文件
        Map<Integer,Integer> logdata = fileService.getData(downloadUrl);    //获取下载记录
        //如果存在下载记录
        if(logdata.size() > 0)
        {
          //遍历集合中的数据,把每条线程已下载的数据长度放入data中
          for(Map.Entry<Integer, Integer> entry : logdata.entrySet())
          {
            data.put(entry.getKey(), entry.getValue());
          }
        }
        //如果已下载的数据的线程数和现在设置的线程数相同时则计算所有现场已经下载的数据总长度
        if(this.data.size() == this.threads.length)
        {
          //遍历每条线程已下载的数据
          for(int i = 0;i < this.threads.length;i++)
          {
            this.downloadedSize += this.data.get(i+1);
          }
          print("已下载的长度" + this.downloadedSize + "个字节");
        }
        //使用条件运算符求出每个线程需要下载的数据长度
        this.block = (this.fileSize % this.threads.length) == 0?
                this.fileSize / this.threads.length:
                this.fileSize / this.threads.length + 1;
      }else{
        //打印错误信息
        print("服务器响应错误:" + conn.getResponseCode() + conn.getResponseMessage());
        throw new RuntimeException("服务器反馈出错");
      }


    }catch (Exception e)
    {
      print(e.toString());   //打印错误
      throw new RuntimeException("无法连接URL");
    }
  }


  /**
   * 获取文件名
   * */
  private String getFileName(HttpURLConnection conn)
  {
    //从下载的路径的字符串中获取文件的名称
    String filename = this.downloadUrl.substring(this.downloadUrl.lastIndexOf('/') + 1);
    if(filename == null || "".equals(filename.trim())){     //如果获取不到文件名称
      for(int i = 0;;i++)  //使用无限循环遍历
      {
        String mine = conn.getHeaderField(i);     //从返回的流中获取特定索引的头字段的值
        if (mine == null) break;          //如果遍历到了返回头末尾则退出循环
        //获取content-disposition返回字段,里面可能包含文件名
        if("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())){
          //使用正则表达式查询文件名
          Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
          if(m.find()) return m.group(1);    //如果有符合正则表达式规则的字符串,返回
        }
      }
      filename = UUID.randomUUID()+ ".tmp";//如果都没找到的话,默认取一个文件名
      //由网卡标识数字(每个网卡都有唯一的标识号)以及CPU时间的唯一数字生成的一个16字节的二进制作为文件名
    }
    return filename;
  }

  /**
   *  开始下载文件
   * @param listener 监听下载数量的变化,如果不需要了解实时下载的数量,可以设置为null
   * @return 已下载文件大小
   * @throws Exception
   */
  //进行下载,如果有异常的话,抛出异常给调用者
  public int download(DownloadProgressListener listener) throws Exception{
    try {
      RandomAccessFile randOut = new RandomAccessFile(this.saveFile, "rwd");
      //设置文件大小
      if(this.fileSize>0) randOut.setLength(this.fileSize);
      randOut.close();    //关闭该文件,使设置生效
      URL url = new URL(this.downloadUrl);
      if(this.data.size() != this.threads.length){
        //如果原先未曾下载或者原先的下载线程数与现在的线程数不一致
        this.data.clear();
        //遍历线程池
        for (int i = 0; i < this.threads.length; i++) {
          this.data.put(i+1, 0);//初始化每条线程已经下载的数据长度为0
        }
        this.downloadedSize = 0;   //设置已经下载的长度为0
      }

      for (int i = 0; i < this.threads.length; i++) {//开启线程进行下载
        int downLength = this.data.get(i+1);
        //通过特定的线程id获取该线程已经下载的数据长度
        //判断线程是否已经完成下载,否则继续下载
        if(downLength < this.block && this.downloadedSize<this.fileSize){
          //初始化特定id的线程
          this.threads[i] = new DownloadThread(this, url, this.saveFile, this.block, this.data.get(i+1), i+1);
          //设置线程优先级,Thread.NORM_PRIORITY = 5;
          //Thread.MIN_PRIORITY = 1;Thread.MAX_PRIORITY = 10,数值越大优先级越高
          this.threads[i].setPriority(7);
          this.threads[i].start();    //启动线程
        }else{
          this.threads[i] = null;   //表明线程已完成下载任务
        }
      }

      fileService.delete(this.downloadUrl);
      //如果存在下载记录，删除它们，然后重新添加
      fileService.save(this.downloadUrl, this.data);
      //把下载的实时数据写入数据库中
      boolean notFinish = true;
      //下载未完成
      while (notFinish) {
        // 循环判断所有线程是否完成下载
        Thread.sleep(900);
        notFinish = false;
        //假定全部线程下载完成
        for (int i = 0; i < this.threads.length; i++){
          if (this.threads[i] != null && !this.threads[i].isFinish()) {
            //如果发现线程未完成下载
            notFinish = true;
            //设置标志为下载没有完成
            if(this.threads[i].getDownLength() == -1){
              //如果下载失败,再重新在已下载的数据长度的基础上下载
              //重新开辟下载线程,设置线程的优先级
              this.threads[i] = new DownloadThread(this, url, this.saveFile, this.block, this.data.get(i+1), i+1);
              this.threads[i].setPriority(7);
              this.threads[i].start();
            }
          }
        }
        if(listener!=null) listener.onDownloadSize(this.downloadedSize);
        //通知目前已经下载完成的数据长度
      }
      if(downloadedSize == this.fileSize) fileService.delete(this.downloadUrl);
      //下载完成删除记录
      listener.onDownLoadComplete(this.saveFile);
    } catch (Exception e) {
      print(e.toString());
      listener.onDownloadFailure(e);
      throw new Exception("文件下载异常");
    }
    return this.downloadedSize;
  }


  /**
   * 获取Http响应头字段
   * @param http
   * @return
   */
  public static Map<String, String> getHttpResponseHeader(HttpURLConnection http) {
    //使用LinkedHashMap保证写入和便利的时候的顺序相同,而且允许空值
    Map<String, String> header = new LinkedHashMap<String, String>();
    //此处使用无线循环,因为不知道头字段的数量
    for (int i = 0;; i++) {
      String mine = http.getHeaderField(i);  //获取第i个头字段的值
      if (mine == null) break;      //没值说明头字段已经循环完毕了,使用break跳出循环
      header.put(http.getHeaderFieldKey(i), mine); //获得第i个头字段的键
    }
    return header;
  }
  /**
   * 打印Http头字段
   * @param http
   */
  public static void printResponseHeader(HttpURLConnection http){
    //获取http响应的头字段
    Map<String, String> header = getHttpResponseHeader(http);
    //使用增强for循环遍历取得头字段的值,此时遍历的循环顺序与输入树勋相同
    for(Map.Entry<String, String> entry : header.entrySet()){
      //当有键的时候则获取值,如果没有则为空字符串
      String key = entry.getKey()!=null ? entry.getKey()+ ":" : "";
      print(key+ entry.getValue());      //打印键和值得组合
    }
  }

  /**
   * 打印信息
   * @param msg 信息字符串
   * */
  private static void print(String msg) {
    Log.i(TAG, msg);
  }
}
