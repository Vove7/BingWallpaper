package cn.vove7.bingwallpaper.utils;

import android.util.Log;

/**
 * Created by Vove on 2017/11/7.
 * LogHelper
 */

public class LogHelper {
   private static boolean output = false;

   public static void d(Object msg) {
      d(null, String.valueOf(msg));
   }

   public static void d(String msg) {
      d(null, msg);
   }

   public static void d(String tag, int msg) {
      d(tag, String.valueOf(msg));
   }

   public static void d(String tag, String msg) {
      if (output) {
         tag = (tag == null ? "##########" : tag);
         Log.d(String.format("%9s", tag), ("---------------------->") + (msg == null ? "msg==null" : msg));
      }
   }

   public static void logE(String tag, String msg) {
      if (output)
         Log.e((tag == null ? " " : tag), ("---------------------->") + msg);
   }
}
