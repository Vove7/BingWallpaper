package cn.vove7.bingwallpaper.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.activities.AlarmActivity;
import cn.vove7.bingwallpaper.activities.MainActivity;
import cn.vove7.bingwallpaper.activities.SettingsActivity;

import static cn.vove7.bingwallpaper.services.DownloadService.IMAGE_DIRECTORY;

/**
 * Created by Vove on 2017/11/19.
 * cn.vove7
 */

public class SettingHelper {
   public static final String[] keys = {
           "quality_of_image", "switch_alarm_wallpaper",
           "wallpaper_refresh_interval", "set_wallpaper_mode",
           "resolution_ratio_wallpaper", "auto_update",
           "update_only_wifi"
   };
   public static final String KEY_NOW_WALLPAPER = "now_wallpaper";
   public final static Map<String, String> modeMap = new HashMap<>();

   static {
      modeMap.put("order", "顺序");
      modeMap.put("random", "随机");
   }

   private static final String PREFERENCE_NAME = "cn.vove7.bingwallpaper_preferences";

   private static SharedPreferences preferences;

   private static void initPreference() {
      if (preferences == null) {
         Activity activity = MyApplication.getApplication()
                 .getMainActivity();
         if (activity == null)
            activity = MyApplication.getApplication().getSettingsActivity();
         if (activity == null)
            activity = MyApplication.getApplication().getAlarmActivity();
         preferences = activity.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
      }
   }

   public static boolean getOnlyWifi() {
      initPreference();
      return preferences.getBoolean(keys[6], true);
   }

   public static void initSetting() {//应用启动初始化设置
      initPreference();
      //init目录
      File file = new File(IMAGE_DIRECTORY);
      if (!file.exists()) {//无目录
         file.mkdir();
      }

      String quality = preferences.getString(keys[0], "80");
      double q = Double.parseDouble(quality) / 100;
      LogHelper.logD(quality);
      MyApplication.getApplication().setQualityOfImage(q);
      boolean alarm_status = preferences.getBoolean(keys[1], false);//定时状态
      long interval = SettingHelper.getInterval();

      MainActivity activity = MyApplication.getApplication().getMainActivity();
      if (alarm_status) {//开启定时
         LogHelper.logD("开启定时");
         new AlarmHelper(activity).startAlarmForActivityWithInterval(
                 AlarmActivity.ACTION_ALARM_SET_WALLPAPER, interval,
                 AlarmHelper.REQUEST_CODE_ALARM_SET_WALLPAPER
         );
      }
      boolean auto_update = preferences.getBoolean(keys[5], false);//自动更新状态
      if (auto_update) {//开启更新
         LogHelper.logD("开启自动更新");
         new AlarmHelper(activity).startAlarmForActivityWithInterval(
                 AlarmActivity.ACTION_ALARM_AUTO_UPDATE, AlarmManager.INTERVAL_DAY,
                 AlarmHelper.REQUEST_CODE_AUTO_UPDATE);
      }
   }

   public static int getImgResolutionRatio() {
      initPreference();
      Set<String> defaultRR = new HashSet<>();
      defaultRR.add("1920x1200");//默认
      Set resolutionRatioSet = preferences.getStringSet(keys[4], defaultRR);
      int result = (resolutionRatioSet.contains("1920x1080") ? 1 : 0) +
              2 * (resolutionRatioSet.contains("1920x1200") ? 1 : 0);
      LogHelper.logD(result);
      return result;
   }

   public static String getOrderMode(Context contex) {
      initPreference();
      return preferences.getString(keys[3], contex.getString(R.string.default_order_wallpaper));
   }

   public static String getNowWallpaper() {
      initPreference();
      return preferences.getString(KEY_NOW_WALLPAPER, null);
   }

   public static void setNowWallpaper(String nowWall) {
      initPreference();
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString(KEY_NOW_WALLPAPER, nowWall);
      editor.apply();
   }

   public static void setTotalSwitch(boolean b) {//设置总开关
      initPreference();
      SharedPreferences.Editor editor = preferences.edit();
      editor.putBoolean(keys[1], b);
      editor.apply();
   }

   public static void setInterval(String intervalStr) {
      LogHelper.logD(intervalStr);
      initPreference();
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString(keys[2], intervalStr);
      editor.apply();
   }


   public static String getIntervalStr() {
      initPreference();
      return preferences.getString(keys[2], "01:00");
   }

   public static long getInterval() {
      String inStr = getIntervalStr();
      return Utils.interval2Mills(inStr);
   }

   public static void initSettingView(SettingsActivity settingsActivity) {

      initPreference();
      boolean b = preferences.getBoolean(keys[1], false);
      LogHelper.logD(null, keys[1] + "--" + b);
      for (int i = 2; i < 5; i++) {
         settingsActivity.findPreference(keys[i]).setEnabled(b);
      }
      String quality = preferences.getString(keys[0], "80");
      settingsActivity.findPreference(keys[0]).setSummary(quality + "%%");

      String interval = preferences.getString(keys[2], "01:00");
      settingsActivity.findPreference(keys[2]).setSummary(interval);

      String mode = preferences.getString(keys[3], "order");
      settingsActivity.findPreference(keys[3]).setSummary(modeMap.get(mode));

      Set<String> resolutionRatio = preferences.getStringSet(keys[4], null);
      if (resolutionRatio != null)
         settingsActivity.findPreference(keys[4]).setSummary(resolutionRatio.toString());
   }

   public static void disEnabledAlarmPreference(SettingsActivity settingsActivity) {
      for (int i = 2; i < 5; i++) {
         settingsActivity.findPreference(keys[i]).setEnabled(false);
      }
   }

   public static void enabledAlarmPreference(SettingsActivity settingsActivity) {
      for (int i = 2; i < 5; i++) {
         settingsActivity.findPreference(keys[i]).setEnabled(true);
      }
   }
}
