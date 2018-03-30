package cn.vove7.bingwallpaper.activities;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import cn.vove7.bingwallpaper.tasks.DownloadLatestTask;
import cn.vove7.bingwallpaper.utils.AlarmHelper;
import cn.vove7.bingwallpaper.utils.BingImage;
import cn.vove7.bingwallpaper.utils.BingImages;
import cn.vove7.bingwallpaper.utils.LogHelper;
import cn.vove7.bingwallpaper.utils.MyApplication;
import cn.vove7.bingwallpaper.utils.SettingHelper;
import cn.vove7.bingwallpaper.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static cn.vove7.bingwallpaper.services.DownloadService.IMAGE_DIRECTORY;
import static cn.vove7.bingwallpaper.services.DownloadService.POSTFIX_1200;
import static cn.vove7.bingwallpaper.services.DownloadService.PREFIX_1200;

//无界面 launchMode=singleInstance
public class AlarmActivity extends Activity {

   public static final String ACTION_ALARM_SET_WALLPAPER = "cn.vove7.bingwallpaper.SET_WALLPAPER";
   public static final String ACTION_ALARM_AUTO_UPDATE = "cn.vove7.bingwallpaper.AUTO_UPDATE";


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      String action = getIntent().getAction();
      LogHelper.logD(action);
      MyApplication.getApplication().setAlarmActivity(this);

      if (ACTION_ALARM_SET_WALLPAPER.equals(action)) {
         chooseWallpaper();
      } else if (ACTION_ALARM_AUTO_UPDATE.equals(action)) {//00:00启动

         if (!SettingHelper.getOnlyWifi()) {
            LogHelper.logD("downloading on canWifi");
            getLatestImg();
         } else {
            if (Utils.isWifi(this)) {//是否wifi下
               LogHelper.logD("download on wifi");
               getLatestImg();
            } else {
               LogHelper.logD("on data no download");
            }
         }
      }
      finish();
      LogHelper.logD("Alarm finish");
   }

   private void chooseWallpaper() {
      ArrayList<String> images = filterFile();//筛选图片
      if (images == null) {//无图片
         LogHelper.logD("无筛选到图片");
         new AlarmHelper(this).cancelAlarm(ACTION_ALARM_SET_WALLPAPER, AlarmHelper.REQUEST_CODE_ALARM_SET_WALLPAPER);
         return;
      }
      String order = SettingHelper.getOrderMode(this);//随机、顺序
      LogHelper.logD("播放方式", order);


      String nextImage = (order.equals("order")) ?
              getNextWithOrder(images) :
              getNextWithRandom(images);

      LogHelper.logD("chooseWallpaper", nextImage);
      SettingHelper.setNowWallpaper(nextImage);

      Utils.setWallpaper(this, BitmapFactory.decodeFile(IMAGE_DIRECTORY + nextImage));
      Utils.delayToast(this, 2000, "已为你更新壁纸");
   }

   private String getNextWithOrder(ArrayList<String> images) {
      String nowWallpaper = SettingHelper.getNowWallpaper();//上一张
      LogHelper.logD("lastWallpaper", nowWallpaper);

      if (nowWallpaper == null) {//初始
         return images.get(0);
      } else {
         int nextIndex = images.indexOf(nowWallpaper);//获取当前序号
         nextIndex = nextIndex == images.size() - 1 ? 0 : nextIndex + 1;
         LogHelper.logD("nextIndex", nextIndex);
         return images.get(nextIndex);
      }
   }

   private String getNextWithRandom(ArrayList<String> images) {
      String nowWallpaper = SettingHelper.getNowWallpaper();//上一张
      LogHelper.logD("lastWallpaper", nowWallpaper);
      String next;
      do {//选取下一张
         int i = (int) (Math.random() * images.size());
         next = images.get(i);
      } while (next.equals(nowWallpaper));//防止nowWallpaper空指针
      return next;
   }

   private ArrayList<String> filterFile() {
      File dir = new File(IMAGE_DIRECTORY);
      ArrayList<String> files = new ArrayList<>(Arrays.asList(dir.list()));
      if (files.size() == 0) {
         LogHelper.logD("无图片资源");
         return null;
      }

      ArrayList<String> images = new ArrayList<>();
      int rr = SettingHelper.getImgResolutionRatio();
      boolean high = rr / 2 == 1;//1920x1200
      boolean low = rr % 2 == 1;//1920x1080

      ArrayList<Integer> needDel = new ArrayList<>();
      if (high) {
         int index = 0;
         for (String name : files) {
            if (name.contains("1920x1200") && new File(IMAGE_DIRECTORY + name).isFile()) {
               images.add(name);
               needDel.add(index);
            }
            index++;
         }
      }
      if (low) {
         if (images.size() != 0) {//删除原集
            int n = 0;
            for (int i : needDel) {
               files.remove(i - n++);//移位
            }
         }

         for (String name : files) {
            if (name.length() == 12 && new File(IMAGE_DIRECTORY + name).isFile()) {
               images.add(name);
            }
         }
      }
      Collections.sort(images);
      Collections.sort(images, Collections.reverseOrder());
      LogHelper.logD(images);
      return images;
   }

   private static final String latestUrl = "https://www.bing.com/HpImageArchive.aspx?format=js&idx=0&n=1";
   private static BingImage latestImg;
   private static String latestName;

   private void downloadLatest(String url) {
      LogHelper.logD("begin download latest");
      DownloadLatestTask downloadTask = new DownloadLatestTask(this);

      LogHelper.logD(url);
      LogHelper.logD(latestName);
      downloadTask.execute(url, latestName);
   }

   private void getLatestImg() {//解析json
      OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
      final Request request = new Request.Builder().url(latestUrl)
              .get().build();
      Call call = client.newCall(request);
      call.enqueue(new Callback() {
         @Override
         public void onFailure(@NonNull Call call, @NonNull IOException e) {
            LogHelper.logD("getLatestImg Failed--网络错误");
         }

         @Override
         public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            ResponseBody body = response.body();
            if (body != null) {
               latestImg = new Gson().fromJson(body.string(), BingImages.class)
                       .getImages().get(0);

               String url = PREFIX_1200 + latestImg.getHsh();
               latestName = latestImg.getStartDate() + POSTFIX_1200;
               downloadLatest(url);
            } else {
               LogHelper.logD("body==null");
            }
         }
      });
   }

   public void continueGetImg(int status) {
      switch (status) {
         case DownloadLatestTask.SUCCESSFUL: {
            Utils.setWallpaper(this, BitmapFactory.
                    decodeFile(IMAGE_DIRECTORY + latestName));
            Utils.delayToast(this, 2000, "已为你更新壁纸");
         }
         break;
         case DownloadLatestTask.CONTENT_LENGTH_0: {//无权下载，下载1080
            LogHelper.logD("无权下载，下载1080");

            latestName = latestImg.getStartDate() + ".jpg";
            downloadLatest(latestImg.getUrlBase() + "_1920x1080.jpg");
         }
         break;
         case DownloadLatestTask.Failed:
            LogHelper.logD("latest download Failed");
      }
   }
}
