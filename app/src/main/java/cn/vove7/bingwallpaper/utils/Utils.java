package cn.vove7.bingwallpaper.utils;

import android.content.Context;
import android.content.Intent;

import java.io.File;

import cn.vove7.bingwallpaper.R;

/**
 * Created by Vove on 2017/11/13.
 */

public class Utils {
   public static boolean isFileExist(String filename) {
      return new File(filename).exists();
   }

   public static void shareTo(Context context) {
      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("text/plain");
      intent.putExtra(Intent.EXTRA_TEXT, "https://www.coolapk.com/apk/cn.vove7.bingwallpaper");
      context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_to)));
   }
}
