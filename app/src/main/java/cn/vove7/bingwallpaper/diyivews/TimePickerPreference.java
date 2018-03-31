package cn.vove7.bingwallpaper.diyivews;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.activities.AlarmActivity;
import cn.vove7.bingwallpaper.utils.AlarmHelper;
import cn.vove7.bingwallpaper.utils.LogHelper;
import cn.vove7.bingwallpaper.utils.MyApplication;
import cn.vove7.bingwallpaper.utils.SettingHelper;
import cn.vove7.bingwallpaper.utils.Utils;


public class TimePickerPreference extends DialogPreference {
   public TimePickerPreference(Context context) {
      super(context);
   }

   public TimePickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
      super(context, attrs, defStyleAttr, defStyleRes);
   }

   public TimePickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
   }

   public TimePickerPreference(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   @Override
   protected View onCreateDialogView() {
      setDialogLayoutResource(R.layout.dialog_time_picker);
      setDialogTitle(R.string.select_refresh_interval);

      return super.onCreateDialogView();
   }

   @Override
   protected void onBindDialogView(View view) {
      super.onBindDialogView(view);
//      Dialog dialog = getDialog();
      timePicker = (view.findViewById(R.id.time_picker));
      timePicker.setIs24HourView(true);
      timePicker.setDrawingCacheEnabled(true);
      String[] s = SettingHelper.getIntervalStr().split(":");//old

      timePicker.setCurrentHour(Integer.parseInt(s[0]));//
      timePicker.setCurrentMinute(Integer.parseInt(s[1]));
   }

   private TimePicker timePicker;

   @Override
   public void onClick(DialogInterface dialogInterface, int which) {
      switch (which) {
         case Dialog.BUTTON_POSITIVE:
            //OK
            String intervalStr = timePicker.getCurrentHour() + ":" +
                    timePicker.getCurrentMinute();
            LogHelper.d(intervalStr);
            long interval = Utils.interval2Mills(intervalStr);
            if (interval < 0) {
               MyApplication.getApplication().getSettingsActivity().showSnack(R.string.time_format_error);
            } else {
               SettingHelper.setInterval(intervalStr);
               new AlarmHelper(MyApplication.getApplication().getSettingsActivity()).startAlarmForActivityWithInterval(
                       AlarmActivity.ACTION_ALARM_SET_WALLPAPER, interval,
                       AlarmHelper.REQUEST_CODE_ALARM_SET_WALLPAPER);
               MyApplication.getApplication().getSettingsActivity().showSnack(R.string.update_setting);
               this.setSummary(intervalStr);
            }
            LogHelper.d("点击OK");
            //dialog.dismiss();//关闭,不再触发onPreferenceChange

            break;
         case Dialog.BUTTON_NEGATIVE:
            //do something
            break;
         case Dialog.BUTTON_NEUTRAL:
            //dosomething
            break;
      }
      super.onClick(dialogInterface, which);
   }

   @Override
   protected View onCreateView(ViewGroup parent) {
      return super.onCreateView(parent);
   }
}
