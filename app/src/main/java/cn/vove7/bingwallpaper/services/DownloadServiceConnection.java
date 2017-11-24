package cn.vove7.bingwallpaper.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import cn.vove7.bingwallpaper.utils.LogHelper;

/**
 * Created by Vove on 2017/11/21.
 * cn.vove7
 */

public class DownloadServiceConnection implements ServiceConnection {
   private DownloadService.DownloadBinder downloadBinder;

   public DownloadService.DownloadBinder getDownloadBinder() {
      return downloadBinder;
   }

   @Override
   public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      downloadBinder = (DownloadService.DownloadBinder) iBinder;
      LogHelper.logD("serCon->", "onServiceConnected*******");
   }

   @Override
   public void onBindingDied(ComponentName name) {
      LogHelper.logD("serCon->", "onDied*******");
      downloadBinder = null;
   }

   @Override
   public void onServiceDisconnected(ComponentName componentName) {
      LogHelper.logD("serCon->", "onServiceDisconnected*******");
      downloadBinder = null;
   }
}
