package cn.vove7.bingwallpaper.tasks;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.services.DownloadListener;
import cn.vove7.bingwallpaper.services.DownloadService;
import cn.vove7.bingwallpaper.utils.DBHelper;
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
   private static final int STATUS_CONTENT_LENGTH_0 = 4;

   private boolean isPause = false;
   private boolean isCanceled = false;
   @SuppressLint("StaticFieldLeak")
   private static DownloadService service;
   private String downloadUrl;
   private String filename;

   public static void setService(DownloadService service) {
      DownloadTask.service = service;
   }

   public DownloadTask(String downloadUrl, String filename) {
      this.downloadUrl = downloadUrl;
      this.filename = filename;
   }

   private DownloadListener downloadListener = new DownloadListener() {
      @Override
      public void dealContent_0() {
         if (service != null) {
            service.notifyResult(false);
            service.showNotification();

            Toast.makeText(service, filename + "无权下载，可选择1920x1080", Toast.LENGTH_SHORT).show();
         }
      }

      @Override
      public void onProgress() {//暂未用
         if (service != null) {
            service.showNotification();
         }
      }

      @Override
      public void onSuccess() {
         DBHelper.setDownloadOk(downloadUrl);

         if (service != null) {
            service.notifyResult(true);//先通知
            service.showNotification();

         }
      }

      @Override
      public void onFailed() {
         if (service != null) {
            service.notifyResult(false);
            service.showNotification();
         }
      }

      @Override
      public void onPause() {//暂未用
         if (service != null) {
            Toast.makeText(service, "Paused", Toast.LENGTH_SHORT).show();
         }
      }

      @Override
      public void onCanceled() {//暂未用
         if (service != null) {
            service.stopForeground(true);
            Toast.makeText(service, "Canceled", Toast.LENGTH_SHORT).show();
         }
      }
   };


   @Override
   protected Integer doInBackground(Object... params) {
      String directory = DownloadService.IMAGE_DIRECTORY;
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
      try {
         long downloadLength = 0;//文件长度

         file = new File(directory + filename);
         if (file.exists()) {//续点
            downloadLength = file.length();
         }
         //long contentLength = getContentLength(downloadUrl);
         //if (contentLength == 0) {
         //   LogHelper.d(null, "contentLength = 0");
         //
         //   return STATUS_CONTENT_LENGTH_0;
         //} else
         if (file.exists() && DBHelper.haveDownloaded(downloadUrl)) {
            LogHelper.d(null, "文件已下载-db-->" + filename);
            return STATUS_SUCCESS;
         }

         OkHttpClient client = new OkHttpClient();
         Request request = new Request.Builder()//断点续传，指定位置
                 .addHeader("RANGE", "bytes=" + downloadLength + "-")
                 .url(downloadUrl)
                 .build();
         Response response = client.newCall(request).execute();
         ResponseBody body = response.body();
         if (body != null && body.contentLength() != 0) {
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
               }
            }
            body.close();
            return STATUS_SUCCESS;
         } else {
            return STATUS_CONTENT_LENGTH_0;
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
         case STATUS_CONTENT_LENGTH_0:
            downloadListener.dealContent_0();
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

}

