package com.myrn.utils.download;

import android.content.Context;

import java.io.File;

public class DownloadTask implements Runnable {
  private Context context;
  private String path;
  private String filename;
  private File saveDir;
  private FileDownloadered loader;
  private DownloadProgressListener downloadProgressListener;

  public DownloadTask(Context ctx, String path, String filename, File saveDir, DownloadProgressListener downloadProgressListener) {
    this.context = ctx;
    this.path = path;
    this.filename = filename;
    this.saveDir = saveDir;
    this.downloadProgressListener = downloadProgressListener;
  }

  @Override
  public void run() {
    try {
      loader = new FileDownloadered(context, path, filename, saveDir, 1);
      loader.download(this.downloadProgressListener);
    } catch (Exception e) {
      this.downloadProgressListener.onDownloadFailure(e);
      e.printStackTrace();
    }
  }
}
