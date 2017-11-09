package cn.vove7.bingwallpaper.utils;

import android.util.Log;

/**
 * Created by Vove on 2017/11/7.
 * LogHelper
 */

public class LogHelper {
   private static boolean output = true;

   public static void logD(String tag, String msg) {
      if (output)
         Log.d(tag == null ? "" : tag, msg);
   }

   public static void logE(String tag, String msg) {
      if (output)
         Log.e(tag == null ? "" : tag, msg);
   }
}
