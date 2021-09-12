package com.myrn.utils;

import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;

public class FileUtil {
  public static String convertStream2String(InputStream is) {
    String s = null;
    try {
      Scanner scanner = new Scanner(is,"UTF-8").useDelimiter("\\A");
      if (scanner.hasNext()) {
        s = scanner.next();
      }
      is.close();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return s;
  }

  public static Boolean isExists(String filepath) {
    try {
      return new java.io.File(new URI(filepath)).exists();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
}
