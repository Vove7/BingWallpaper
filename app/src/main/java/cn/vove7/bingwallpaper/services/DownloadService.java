package cn.vove7.bingwallpaper.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.activitys.MainActivity;
import cn.vove7.bingwallpaper.tasks.DownloadTask;
import cn.vove7.bingwallpaper.utils.BingImage;
import cn.vove7.bingwallpaper.utils.LogHelper;


public class DownloadService extends Service {//多任务下载，可管理任一任务
   private static ArrayList<DownloadTask> downloadTaskArray;
   private ArrayList<BingImage> bingImages;
   private int surplusTaskNum;//剩余任务
   private int totalTaskNum;//总任务
   private int failedNum;

   private DownloadBinder mBinder = new DownloadBinder();

   public static final String directory = Environment.
           getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/BingWallpapers/";

   public class DownloadBinder extends Binder {//与外部Activity通信

      public boolean isDownloading() {
         return (downloadTaskArray != null && downloadTaskArray.size() != 0);
      }

      public void startDownload(ArrayList<BingImage> urlArray, MainActivity activity) {
         LogHelper.logD(null, "开始下载");
         if (downloadTaskArray == null) {//
            failedNum = 0;
            downloadTaskArray = new ArrayList<>();
            bingImages = urlArray;
            surplusTaskNum = totalTaskNum = bingImages.size();
            for (BingImage image : bingImages) {
               DownloadTask downloadTask = new DownloadTask();
               String fileName = image.getStartDate() + ".jpg";
               downloadTaskArray.add(downloadTask);
               downloadTask.execute(image.getUrlBase() + "_1920x1080.jpg", DownloadService.this, fileName);
            }
            startForeground(1, getNotification(getString(R.string.downloading)));
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

               File file = new File(directory + filename);
               if (file.exists()) {
                  file.delete();
               }

               getNotificationManager().cancel(1);
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


   public NotificationManager getNotificationManager() {
      return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
   }

   public void notifyResult(boolean isSuccessful) {//通知下载结果
      surplusTaskNum--;
      if (!isSuccessful) {
         failedNum++;
      }
   }

   public Notification getNotification(String title) {
      Builder builder = new Builder(this)
              .setSmallIcon(R.mipmap.ic_launcher)
              .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//              .setContentIntent(pi)
              .setContentTitle(title);//开始显示

      if (surplusTaskNum > 0) {
         stopForeground(false);
         builder.setContentText(String.format(getString(R.string.surplus_task_text), surplusTaskNum, failedNum));
         int process = (int) ((1 - ((float) surplusTaskNum / totalTaskNum)) * 100);
         builder.setProgress(100, process, false);
      } else {
         builder.setAutoCancel(true)
                 .setTicker(getString(R.string.download_finish))
                 .setContentTitle(String.format(getString(R.string.download_detail), totalTaskNum - failedNum, failedNum));

         stopForeground(true);

         //清理
         downloadTaskArray.clear();
         downloadTaskArray = null;
         LogHelper.logD("service->", "stop 下载完成");
//         Snackbar.make(mainActivity.getRecyclerView(), R.string.download_finish, Snackbar.LENGTH_SHORT)
//                 .show();
//         stopSelf();//停止服务
      }
      return builder.build();
   }
}
