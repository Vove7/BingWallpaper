package cn.vove7.bingwallpaper.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Date;

import static android.app.AlarmManager.INTERVAL_DAY;


/**
 * Created by Vove on 2017/11/19.
 * cn.vove7
 * AlarmManager 根据 PendingIntent requestCode 来判断是否是同一个定时服务，所以当
 * requestCode相等的时候只有最后一个生效
 * 精度一分钟
 */


public class AlarmHelper {
   private AlarmManager manager;
   private Context context;

   public static final int REQUEST_CODE_AUTO_UPDATE = 0;
   public static final int REQUEST_CODE_ALARM_SET_WALLPAPER = 1;

   public AlarmHelper(Context context) {
      manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      this.context = context;
   }

   public void startAlarmForActivityWithInterval(String action, long interval, int requestCode) {
      if (interval < -1) {
         Toast.makeText(context, "开启失败 error:" + interval, Toast.LENGTH_SHORT).show();
         return;
      }
      Intent intent = new Intent(action);
      PendingIntent pendingIntent = PendingIntent
              .getActivity(context, requestCode, intent, 0);
      /*
       * 定义提醒，间隔定时开启一次Activity
       */
      long startTime = System.currentTimeMillis();
      if (requestCode == REQUEST_CODE_AUTO_UPDATE) {//计算当天下个00:00
         startTime = startTime + 2 * INTERVAL_DAY / 3 +
                 (INTERVAL_DAY - (startTime % INTERVAL_DAY)) + 1000;
      } else if (requestCode == REQUEST_CODE_ALARM_SET_WALLPAPER) {//延迟
         startTime += 30000;
      }

      LogHelper.d("startAlarm--" + action);
      LogHelper.d("starttime--" + new Date(startTime));
      LogHelper.d("interval--" + interval);
      manager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, interval, pendingIntent);
   }

   public void cancelAlarm(String action, int requestCode) {
      LogHelper.d("cancelAlarm--" + action);
      SettingHelper.setTotalSwitch(false);
      Intent intent = new Intent(action);
      PendingIntent pendingIntent = PendingIntent
              .getActivity(context, requestCode, intent, 0);
      manager.cancel(pendingIntent);

   }
}
