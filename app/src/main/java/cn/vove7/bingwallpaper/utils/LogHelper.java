package cn.vove7.bingwallpaper.utils;

import android.util.Log;

/**
 * Created by Vove on 2017/11/7.
 * LogHelper
 */

public class LogHelper {
   private static boolean output = true;

   public static void logD(Object msg) {
      logD(null, String.valueOf(msg));
   }

   public static void logD(String msg) {
      logD(null, msg);
   }

   public static void logD(String tag, int msg) {
      logD(tag, String.valueOf(msg));
   }

   public static void logD(String tag, String msg) {
      if (output) {
         tag = (tag == null ? "null" : tag);
         Log.d(String.format("%9s", tag), ("---------------------->") + (msg == null ? "msg==null" : msg));
      }
   }

   public static void logE(String tag, String msg) {
      if (output)
         Log.e((tag == null ? " " : tag), ("---------------------->") + msg);
   }
}
