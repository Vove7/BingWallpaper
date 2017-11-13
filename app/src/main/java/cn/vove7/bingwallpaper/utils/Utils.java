package cn.vove7.bingwallpaper.utils;

import java.io.File;

/**
 * Created by Vove on 2017/11/13.
 */

public class Utils {
   public static boolean isFileExist(String filename) {
      return new File(filename).exists();
   }
}
