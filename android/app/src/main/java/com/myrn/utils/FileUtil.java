package com.myrn.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;

public class FileUtil {
  public static String readFileFromAssets(Context context, String filename) {
    String s = null;
    try {
      InputStream is = context.getAssets().open(filename);
      Scanner scanner = new Scanner(is,"UTF-8").useDelimiter("\\A");
      if (scanner.hasNext()) {
        s = scanner.next();
      }
      is.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return s;
  }

  public static String readFile(File file) {
    String line;
    StringBuffer stringBuffer = new StringBuffer();
    try {
      if (file.exists() && file.canRead()) {
        FileReader fileReader = new FileReader(file);
        BufferedReader bfr = new BufferedReader(fileReader);
        while ((line = bfr.readLine()) != null) {
          stringBuffer.append(line);
        }
        if (bfr != null) {
          bfr.close();
        }
        return stringBuffer.toString();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Boolean fileExists(String filepath) {
    try {
      return new File(filepath).exists();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return false;
  }
}
