package cn.vove7.bingwallpaper.tasks;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.interfaces.DownloadListener;
import cn.vove7.bingwallpaper.services.DownloadService;
import cn.vove7.bingwallpaper.utils.LogHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * Created by Vove on 2017/10/9.
 * DownloadTask
 */

public class DownloadTask extends AsyncTask<Object, Integer, Integer> {
   private static final int STATUS_SUCCESS = 0;
   private static final int STATUS_FAILED = 1;
   private static final int STATUS_PAUSE = 2;
   private static final int STATUS_CANCELED = 3;

   private boolean isPause = false;
   private boolean isCanceled = false;
   @SuppressLint("StaticFieldLeak")
   private static DownloadService service;

   private DownloadListener downloadListener = new DownloadListener() {
      @Override
      public void onProgress() {//暂未用
         service.getNotificationManager().notify(1, service.getNotification(service.getString(R.string.downloading)));
      }

      @Override
      public void onSuccess() {//
         //一个任务完成
//         service.stopForeground(true);
         service.notifyResult(true);//先通知
         Notification notification = service.getNotification(service.getString(R.string.downloading));
         service.getNotificationManager().notify(1, notification);
      }

      @Override
      public void onFailed() {
         service.notifyResult(false);
         service.getNotificationManager().notify(1, service.getNotification(service.getString(R.string.downloading)));
         Toast.makeText(service, R.string.download_failed, Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onPause() {//暂未用
         Toast.makeText(service, "Paused", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onCanceled() {//暂未用
         service.stopForeground(true);
         Toast.makeText(service, "Canceled", Toast.LENGTH_SHORT).show();
      }
   };


   @Override
   protected Integer doInBackground(Object... params) {
      String directory = DownloadService.directory;
      File dir = new File(directory);
      if (!dir.exists()) {
         if (!dir.mkdir()) {
            Toast.makeText(service, R.string.cannot_create_dir, Toast.LENGTH_SHORT).show();
            return 1;
         }
      }

      InputStream inputStream = null;
      RandomAccessFile saveFile = null;
      File file = null;
      String downloadUrl = (String) params[0];
      service = (DownloadService) params[1];
      String filename = (String) params[2];
      try {
         long downloadLength = 0;//文件长度

         file = new File(directory + filename);
         if (file.exists()) {//续点
            downloadLength = file.length();
         }
         long contentLength = getContentLength(downloadUrl);
         if (contentLength == 0) {
            LogHelper.logD(null, "contentLength = 0");
            return STATUS_FAILED;
         } else if (contentLength == downloadLength) {
            LogHelper.logD(null, "文件已存在-->" + filename);
            return STATUS_SUCCESS;
         }

         OkHttpClient client = new OkHttpClient();
         Request request = new Request.Builder()//断点续传，指定位置
                 .addHeader("RANGE", "bytes=" + downloadLength + "-")
                 .url(downloadUrl)
                 .build();
         Response response = client.newCall(request).execute();
         ResponseBody body = response.body();
         if (body != null) {
            inputStream = body.byteStream();
            saveFile = new RandomAccessFile(file, "rw");
            saveFile.seek(downloadLength);//
            byte[] b = new byte[1024];
//            int total = 0;
            int len;
            while ((len = inputStream.read(b)) != -1) {
               if (isCanceled)
                  return STATUS_CANCELED;
               else if (isPause)
                  return STATUS_PAUSE;
               else {
                  saveFile.write(b, 0, len);
//                  total += len;
//                  int progress = (int) ((total + downloadLength) * 100 / contentLength);
//                  publishProgress(progress);//实时更新
               }
            }
//            publishProgress();//下载完成更新
            body.close();
            return STATUS_SUCCESS;
         }


      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            if (inputStream != null) {
               inputStream.close();
            }
            if (saveFile != null) {
               saveFile.close();
            }
            if (isCanceled && file != null) {
               file.delete();
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      return STATUS_FAILED;
   }

   @Override
   protected void onProgressUpdate(Integer[] values) {
//      int progress = values[0];
//      if (progress > lastProgress) {
//         downloadListener.onProgress(progress);
//         lastProgress = progress;
//      }
   }

   @Override
   protected void onPostExecute(Integer status) {//接收doInBackground结果
      switch (status) {
         case STATUS_SUCCESS:
            downloadListener.onSuccess();
            break;
         case STATUS_CANCELED:
            downloadListener.onCanceled();
            break;
         case STATUS_FAILED:
            downloadListener.onFailed();
            break;
         case STATUS_PAUSE:
            downloadListener.onPause();
            break;
         default:
            break;

      }

   }

   public void pauseDownload() {
      isPause = true;
   }

   public void cancelDownload() {
      isCanceled = true;
   }

   private long getContentLength(String downloadUrl) throws IOException {
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

}

