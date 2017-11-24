package cn.vove7.bingwallpaper.tasks;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import cn.vove7.bingwallpaper.activities.AlarmActivity;
import cn.vove7.bingwallpaper.services.DownloadService;
import cn.vove7.bingwallpaper.utils.LogHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static cn.vove7.bingwallpaper.utils.Utils.getContentLength;

/**
 * Created by Vove on 2017/11/23.
 * cn.vove7
 */

public class DownloadLatestTask extends AsyncTask<Object, Void, Integer> {

   public static final int SUCCESSFUL = 1;
   public static final int Failed = 0;
   public static final int CONTENT_LENGTH_0 = 2;

   @SuppressLint("StaticFieldLeak")
   private AlarmActivity alarmActivity;

   public DownloadLatestTask(AlarmActivity alarmActivity) {
      this.alarmActivity = alarmActivity;
   }

   @Override
   protected Integer doInBackground(Object[] params) {
      String directory = DownloadService.IMAGE_DIRECTORY;
      File dir = new File(directory);
      if (!dir.exists()) {
         if (!dir.mkdir()) {
            return Failed;
         }
      }
      InputStream inputStream = null;
      RandomAccessFile saveFile;
      File file;

      String downloadUrl = (String) params[0];
      String filename = (String) params[1];
      try {
         long downloadLength = 0;//文件长度

         file = new File(directory + filename);
         if (file.exists()) {//续点
            downloadLength = file.length();
         }
         long contentLength = getContentLength(downloadUrl);
         if (contentLength == 0) {
            LogHelper.logD(null, "contentLength = 0");
            return CONTENT_LENGTH_0;
         } else if (contentLength == downloadLength) {
            LogHelper.logD(null, "文件已存在-->" + filename);
            return SUCCESSFUL;
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

            int len;
            while ((len = inputStream.read(b)) != -1) {//下载
               saveFile.write(b, 0, len);
            }
            body.close();
            return SUCCESSFUL;
         }

      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            if (inputStream != null) {
               inputStream.close();
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      return Failed;
   }


   @Override
   protected void onPostExecute(Integer result) {//结束
      alarmActivity.continueGetImg(result);
   }
}
