package com.myrn.utils.download;

import java.io.File;

public interface DownloadProgressListener {
  public void onDownloadSize(int downloadedSize);

  public void onDownloadFailure(Exception e);

  public void onDownLoadComplete(File file);
}
