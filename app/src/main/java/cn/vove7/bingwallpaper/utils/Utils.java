package cn.vove7.bingwallpaper.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.activities.ViewImageActivity;
import cn.vove7.bingwallpaper.handler.NotifyMessageHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static cn.vove7.bingwallpaper.handler.NotifyMessageHandler.MSG_TOAST_ARG1;
import static cn.vove7.bingwallpaper.services.DownloadService.IMAGE_DIRECTORY;

/**
 * Created by Vove on 2017/11/13.
 * cn.vove7
 */

public class Utils {
   public static boolean isFileExist(String filename) {
      return new File(filename).exists();
   }

   public static int isLocalHave(String startDate) {
      int i = new File(IMAGE_DIRECTORY + startDate + ".jpg").exists() ? 1 : 0;
      int j = new File(IMAGE_DIRECTORY + startDate + "-1920x1200.png").exists() ? 1 : 0;
      LogHelper.logD(null, String.valueOf(i + 2 * j));
      return i + 2 * j;
   }

   public static void openMarket(Context context, String pkgName) {
      String str = "market://details?id=" + pkgName;
      Intent localIntent = new Intent("android.intent.action.VIEW");
      localIntent.setData(Uri.parse(str));
      context.startActivity(localIntent);
   }


   public static void shareTo(Context context) {
      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("text/plain");
      intent.putExtra(Intent.EXTRA_TEXT, "https://www.coolapk.com/apk/cn.vove7.bingwallpaper");
      context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_app_to)));
   }

   public static void shareImageTo(File file) {
      Uri imageUri = Uri.fromFile(file);
      Intent shareIntent = new Intent();
      shareIntent.setAction(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
      shareIntent.setType("image/*");
      ViewImageActivity viewImageActivity =
              MyApplication.getApplication().getViewImageActivity();
      viewImageActivity.startActivity(Intent.createChooser(shareIntent, viewImageActivity.getString(R.string.share_pic_to)));
   }

   public static void setWallpaper(Context context, final Bitmap bitmap) {
      final WallpaperManager manager = (WallpaperManager) context.getSystemService(
              Context.WALLPAPER_SERVICE);
      if (manager != null) {
         new Thread(new Runnable() {
            @Override
            public void run() {
               try {
                  manager.setBitmap(bitmap);
                  LogHelper.logD("设置成功");

               } catch (IOException e) {
                  sendSetWallError();
                  LogHelper.logD("设置失败");
                  e.printStackTrace();
               }
            }
         }).start();
      } else {
         sendSetWallError();
         LogHelper.logD(null, "WallpaperManager == null");
      }

   }

   private static void sendSetWallError() {
      Message message = new Message();
      message.what = MSG_TOAST_ARG1;
      message.arg1 = R.string.error_set_wallpaper;
      new NotifyMessageHandler().sendMessage(message);
   }

   public static String[] List2Array(ArrayList<String> arrayList) {
      String[] strs = new String[arrayList.size()];
      int i = 0;
      for (String str : arrayList) {
         strs[i++] = str;
      }
      return strs;
   }

   public static int getScreenWidth(Context context) {
      DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
      return displayMetrics.widthPixels;
   }

   public static int getScreenHeight(Context context) {
      DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
      return displayMetrics.heightPixels;
   }

   public static long interval2Mills(String s) {//"(1-60)min or (1-24)h"
      String[] num = s.split(":");
      if (num.length == 2) {
         try {
            int hour = Integer.parseInt(num[0]);
            int min = Integer.parseInt(num[1]);
            if (hour == 0 && min == 0) {
               return -1;
            }
            return (hour * 60 + min) * 60000;
         } catch (NumberFormatException e) {
            e.printStackTrace();
         }
      }
      return -1;
   }


   public static long getContentLength(String downloadUrl) throws IOException {
      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder()
              .url(downloadUrl)
              .build();
      Response response = client.newCall(request).execute();
      if (response != null && response.isSuccessful()) {
         long contentLength = response.body().contentLength();
         response.close();
         return contentLength;
      }
      return 0;
   }

   public static boolean isWifi(Context mContext) {
      ConnectivityManager connectivityManager = (ConnectivityManager)
              mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
      if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
         return true;
      }
      return false;
   }

   public static void delayToast(final Context context, long millis, final String msg) {
      new Handler().postDelayed(new Runnable() {
         @Override
         public void run() {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
         }
      }, millis);
   }

   public static void closeSoftKeyboard(Context context) {
      InputMethodManager imm = (InputMethodManager) context
              .getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
   }
}
