package cn.vove7.bingwallpaper.activities;

import android.app.AlarmManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.TimePicker;


import java.util.Set;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.utils.AlarmHelper;
import cn.vove7.bingwallpaper.utils.LogHelper;
import cn.vove7.bingwallpaper.utils.MyApplication;
import cn.vove7.bingwallpaper.utils.SettingHelper;
import cn.vove7.bingwallpaper.utils.Utils;

import static cn.vove7.bingwallpaper.utils.SettingHelper.disEnabledAlarmPreference;
import static cn.vove7.bingwallpaper.utils.SettingHelper.keys;
import static cn.vove7.bingwallpaper.utils.SettingHelper.modeMap;

public class SettingsActivity extends AppCompatPreferenceActivity
        implements Preference.OnPreferenceChangeListener {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      MyApplication.getApplication().setSettingsActivity(this);
      setupActionBar();

      addPreferencesFromResource(R.xml.preference);
      SettingHelper.initSettingView(this);
      initPreferenceListener();
   }

   @Override
   public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
      return super.onCreateView(parent, name, context, attrs);
   }

   private void setupActionBar() {
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
         // Show the Up button in the action bar.
         actionBar.setDisplayHomeAsUpEnabled(true);

      }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home: {//返回菜单
            finish();
            startActivity(new Intent(this, MainActivity.class));
            return true;
         }
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public void onBackPressed() {
      finish();
      startActivity(new Intent(this, MainActivity.class));
   }


   private void initPreferenceListener() {
      findPreference(keys[2]).setOnPreferenceClickListener((preference) -> {

         if (preference.getKey().equals(keys[2])) {
            LogHelper.logD("设置间隔");
            Dialog dialog = ((EditTextPreference) preference).getDialog();
            final TimePicker timePicker = (dialog.findViewById(R.id.time_picker));
            timePicker.setIs24HourView(true);
            timePicker.setDrawingCacheEnabled(true);

            String[] s = SettingHelper.getIntervalStr().split(":");//old

            timePicker.setCurrentHour(Integer.parseInt(s[0]));//
            timePicker.setCurrentMinute(Integer.parseInt(s[1]));

            //OK
            dialog.findViewById(android.R.id.button1).setOnClickListener(view -> {
               String interval = timePicker.getCurrentHour() + ":" +
                       timePicker.getCurrentMinute();

               LogHelper.logD(interval);
               SettingHelper.setInterval(interval);
               new AlarmHelper(SettingsActivity.this).startAlarmForActivityWithInterval(
                       AlarmActivity.ACTION_ALARM_SET_WALLPAPER, Utils.interval2Mills(interval),
                       AlarmHelper.REQUEST_CODE_ALARM_SET_WALLPAPER);
               showSnack(R.string.update_setting);
               preference.setSummary(interval);
               LogHelper.logD("点击OK");
               dialog.dismiss();//关闭
            });

         }
         return false;
      });


      for (String key : keys) {
         findPreference(key).setOnPreferenceChangeListener(this);
      }
   }


   @Override
   public boolean onPreferenceChange(Preference preference, Object o) {
      String key = preference.getKey();

      LogHelper.logD("key", key);
      LogHelper.logD("value", o.toString());
      if (key.equals(keys[0])) {//图片质量
         double quality = Double.parseDouble(o.toString()) / 100;
         MyApplication.getApplication().setQualityOfImage(quality);
         findPreference(key).setSummary(o.toString() + "%%");
      } else if (key.equals(keys[1])) {//定时总开关
         if ((boolean) (o)) {
            SettingHelper.enabledAlarmPreference(this);
            //检查
            //开启定时任务
//            AlarmHelper
            long interval = SettingHelper.getInterval();

            new AlarmHelper(this).startAlarmForActivityWithInterval(
                    AlarmActivity.ACTION_ALARM_SET_WALLPAPER, interval,
                    AlarmHelper.REQUEST_CODE_ALARM_SET_WALLPAPER);
            showSnack(R.string.start_alarm);

         } else {
            disEnabledAlarmPreference(this);
            //关闭定时任务
            new AlarmHelper(this).cancelAlarm(AlarmActivity.ACTION_ALARM_SET_WALLPAPER,
                    AlarmHelper.REQUEST_CODE_ALARM_SET_WALLPAPER);
            showSnack(R.string.cancel_alarm);
         }

      } else if (key.equals(keys[2])) {//间隔
         //新定义Layout

         return false;

      } else if (key.equals(keys[3])) {//模式
         findPreference(key).setSummary(modeMap.get(o.toString()));
         showSnack(R.string.update_setting);

      } else if (key.equals(keys[4])) {//分辨率
         if (((Set) o).size() == 0) {
            showSnack(R.string.atleast_one_rr);
            return false;
         }
         findPreference(key).setSummary(o.toString());
         showSnack(R.string.update_setting);

      } else if (key.equals(keys[5])) {//自动更新
         //启动自动更新
         if ((boolean) (o)) {
            new AlarmHelper(this).startAlarmForActivityWithInterval(
                    AlarmActivity.ACTION_ALARM_AUTO_UPDATE, AlarmManager.INTERVAL_DAY,
                    AlarmHelper.REQUEST_CODE_AUTO_UPDATE);
            showSnack(R.string.start_alarm);
         } else {
            new AlarmHelper(this).cancelAlarm(AlarmActivity.ACTION_ALARM_AUTO_UPDATE,
                    AlarmHelper.REQUEST_CODE_AUTO_UPDATE);
            showSnack(R.string.cancel_alarm);
         }
      }
      return true;
      //返回true保证自动存储，false不再自动存取
   }

   private void showSnack(int resId) {
      Snackbar.make(getListView(), getString(resId), Snackbar.LENGTH_SHORT).show();
   }
}
