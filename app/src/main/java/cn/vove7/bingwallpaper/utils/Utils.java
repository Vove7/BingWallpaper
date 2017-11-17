package cn.vove7.bingwallpaper.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import cn.vove7.bingwallpaper.R;

import static cn.vove7.bingwallpaper.services.DownloadService.IMAGE_DIRECTORY;

/**
 * Created by Vove on 2017/11/13.
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

   public static void shareTo(Context context) {
      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("text/plain");
      intent.putExtra(Intent.EXTRA_TEXT, "https://www.coolapk.com/apk/cn.vove7.bingwallpaper");
      context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_to)));
   }

   public static void shareImageTo(File file) {
      Uri imageUri = Uri.fromFile(file);
      Intent shareIntent = new Intent();
      shareIntent.setAction(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
      shareIntent.setType("image/*");

      MyApplication.getApplication().getViewImageActivity()
              .startActivity(Intent.createChooser(shareIntent, "分享图片"));
   }

   public static String[] List2Array(ArrayList<String> arrayList) {
      String[] strs = new String[arrayList.size()];
      int i = 0;
      for (String str : arrayList) {
         strs[i++] = str;
      }
      return strs;
   }
}
