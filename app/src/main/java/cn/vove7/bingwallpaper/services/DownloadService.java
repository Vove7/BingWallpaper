package cn.vove7.bingwallpaper.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.tasks.DownloadTask;
import cn.vove7.bingwallpaper.utils.BingImage;
import cn.vove7.bingwallpaper.utils.LogHelper;
import cn.vove7.bingwallpaper.utils.MyApplication;


public class DownloadService extends Service {//多任务下载，可管理任一任务
   private static ArrayList<DownloadTask> downloadTaskArray;
   private ArrayList<BingImage> bingImages;
   private int surplusTaskNum;//剩余任务
   private int totalTaskNum;//总任务
   private int failedNum;//失败数
   private boolean isRefreshBtn;

   public static final int RESOLUTION_RATIO_1080 = 0;
   public static final int RESOLUTION_RATIO_1200 = 1;
   public static final String PREFIX_1200 = "https://www.bing.com/hpwp/";
   public static final String POSTFIX_1200 = "-1920x1200.png";

   private DownloadBinder mBinder = new DownloadBinder();

   public static final String IMAGE_DIRECTORY = Environment.
           getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/BingWallpapers/";

   public class DownloadBinder extends Binder {//与外部Activity通信

      public boolean isDownloading() {
         return (downloadTaskArray != null && downloadTaskArray.size() != 0);
      }

      public void startDownload(ArrayList<BingImage> urlArray, boolean isRefreshBtn, int resolutionRatio) {
         LogHelper.d(null, "开始下载");
         if (downloadTaskArray == null) {//
            failedNum = 0;
            DownloadService.this.isRefreshBtn = isRefreshBtn;
            downloadTaskArray = new ArrayList<>();
            bingImages = urlArray;
            surplusTaskNum = totalTaskNum = bingImages.size();
            DownloadTask.setService(DownloadService.this);//
            for (BingImage image : bingImages) {

               DownloadTask downloadTask;


               if (resolutionRatio == RESOLUTION_RATIO_1080) {//1920x1080
                  downloadTask = new DownloadTask(
                          image.getUrlBase() + "_1920x1080.jpg",
                          image.getStartDate() + ".jpg"
                  );
                  LogHelper.d(null, image.getUrlBase() + "_1920x1080.jpg");

               } else {//1920x1200
                  downloadTask = new DownloadTask(
                          PREFIX_1200 + image.getHsh(),
                          image.getStartDate() + POSTFIX_1200
                  );
                  LogHelper.d(null, PREFIX_1200 + image.getHsh());
               }
               downloadTaskArray.add(downloadTask);
               downloadTask.execute();//执行
            }
            buildNotificationManager(STATUS_INIT);
            startForeground(N_ID, getNotification());
         }
      }

      public void pauseDownload(int index) {
         if (index != -1) {
            if (downloadTaskArray.get(index) != null) {
               downloadTaskArray.get(index).pauseDownload();
            }
         } else {//全部暂停
            for (DownloadTask downloadTask : downloadTaskArray) {
               downloadTask.pauseDownload();
            }
         }
      }

      public void cancelDownload(int index) {
         if (downloadTaskArray != null) {
            if (index == -1) {
               downloadTaskArray.get(index).cancelDownload();
            } else {//全部取消
               for (DownloadTask downloadTask : downloadTaskArray) {
                  downloadTask.cancelDownload();
               }
            }
         } else {
            if (bingImages != null) {//删除文件 关闭通知

               String filename = bingImages.get(index).getStartDate() + ".jpg";

               File file = new File(IMAGE_DIRECTORY + filename);
               if (file.exists()) {
                  file.delete();
               }
               buildNotificationManager(STATUS_DOWNLOADING).cancel(N_ID);
               stopForeground(true);

               Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
         }
      }
   }


   public DownloadService() {

   }

   @Override
   public IBinder onBind(Intent intent) {
      return mBinder;
   }


   public void notifyResult(boolean isSuccessful) {//通知下载结果
      surplusTaskNum--;
      if (!isSuccessful) {
         failedNum++;
      }
   }

   public void showNotification() {
      buildNotification();

      int status = surplusTaskNum > 0 ? STATUS_DOWNLOADING : STATUS_FINISH;
      buildNotificationManager(status).notify(N_ID, notification);
      //return notification;
   }

   public static final int N_ID = 5127;

   private Notification notification;

   public Notification getNotification() {
      if (notification == null) {
         buildNotification();
      }
      return notification;
   }

   private void buildNotification() {
      Notification.Builder builder = new Notification.Builder(this)
              .setSmallIcon(R.mipmap.ic_launcher)
              .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
              //.setContentTitle(content);
      //.setContentText(content);//开始显示
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         builder.setChannelId("bing_wallpaper");
      }

      if (surplusTaskNum > 0) {
         stopForeground(false);
         int process = (int) ((1 - ((float) surplusTaskNum / totalTaskNum)) * 100);
         builder.setContentTitle(getString(R.string.downloading))
                 .setProgress(100, process, false)
                 .setContentText(String.format(getString(R.string.surplus_task_text), surplusTaskNum));
      } else {
         builder.setAutoCancel(false)
                 .setContentTitle(getString(R.string.download_finish))
                 .setContentText(String.format(getString(R.string.download_detail), totalTaskNum - failedNum, failedNum));
         stopForeground(true);


         //清理
         if (failedNum != downloadTaskArray.size()) {
            Toast.makeText(MyApplication.getApplication().getMainActivity(), R.string.download_finish, Toast.LENGTH_SHORT)
                    .show();
         }
         downloadTaskArray.clear();
         downloadTaskArray = null;
         if (isRefreshBtn)//刷新浏览activity按钮
            MyApplication.getApplication().getViewImageActivity().setButtonStatus(-1);
         LogHelper.d("service->", "stop 下载完成");

      }

      notification = builder.build();
   }

   public static final int STATUS_INIT = -1;
   public static final int STATUS_DOWNLOADING = 0;
   public static final int STATUS_FINISH = 1;
   NotificationManager mNotificationManager;

   public NotificationManager buildNotificationManager(int status) {
      if (mNotificationManager != null) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = buildChannel(status);
            mNotificationManager.createNotificationChannel(mChannel);
         }
         return mNotificationManager;
      }
      mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         NotificationChannel mChannel = buildChannel(status);
         mNotificationManager.createNotificationChannel(mChannel);
      }
      return mNotificationManager;
   }

   @RequiresApi(api = Build.VERSION_CODES.O)
   private NotificationChannel buildChannel(int s) {
      String id = "bing_wallpaper";
      CharSequence name = "download";
      String description = "";
      int importance = NotificationManager.IMPORTANCE_LOW;
      NotificationChannel mChannel = new NotificationChannel(id, name, importance);

      mChannel.setDescription(description);
      if (s != STATUS_DOWNLOADING)
         mChannel.enableVibration(true);
      return mChannel;
   }
}
