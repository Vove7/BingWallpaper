package cn.vove7.bingwallpaper.utils;

import android.app.Application;
import android.os.StrictMode;

import org.litepal.LitePal;

import cn.vove7.bingwallpaper.activities.AlarmActivity;
import cn.vove7.bingwallpaper.activities.MainActivity;
import cn.vove7.bingwallpaper.activities.SettingsActivity;
import cn.vove7.bingwallpaper.activities.ViewImageActivity;
import cn.vove7.bingwallpaper.fragments.GalleryFragment;
import cn.vove7.bingwallpaper.fragments.MainFragment;
import cn.vove7.easytheme.EasyTheme;
import cn.vove7.easytheme.ThemeSet;

/**
 * Created by Vove on 2017/11/10.
 */

public class MyApplication extends Application {
   private static MyApplication application;
   private MainActivity mainActivity;
   private SettingsActivity settingsActivity;
   private MainFragment mainFragment;
   private GalleryFragment galleryFragment;
   private ViewImageActivity viewImageActivity;
   private AlarmActivity alarmActivity;
   private double qualityOfImage = 0.8;

   public SettingsActivity getSettingsActivity() {
      return settingsActivity;
   }

   public void setSettingsActivity(SettingsActivity settingsActivity) {
      this.settingsActivity = settingsActivity;
   }

   public AlarmActivity getAlarmActivity() {
      return alarmActivity;
   }

   public void setAlarmActivity(AlarmActivity alarmActivity) {
      this.alarmActivity = alarmActivity;
   }

   public double getQualityOfImage() {
      return qualityOfImage;
   }

   public void setQualityOfImage(double qualityOfImage) {
      this.qualityOfImage = qualityOfImage;
   }

   public ViewImageActivity getViewImageActivity() {
      return viewImageActivity;
   }

   public void setViewImageActivity(ViewImageActivity viewImageActivity) {
      this.viewImageActivity = viewImageActivity;
   }

   public MainActivity getMainActivity() {
      return mainActivity;
   }

   public void setMainActivity(MainActivity mainActivity) {
      this.mainActivity = mainActivity;
   }

   public static void setApplication(MyApplication application) {
      MyApplication.application = application;
   }

   public MainFragment getMainFragment() {
      return mainFragment;
   }

   public void setMainFragment(MainFragment mainFragment) {
      this.mainFragment = mainFragment;
   }

   public GalleryFragment getGalleryFragment() {
      return galleryFragment;
   }

   public void setGalleryFragment(GalleryFragment galleryFragment) {
      this.galleryFragment = galleryFragment;
   }

   public static MyApplication getApplication() {
      if (application == null)
         application = new MyApplication();
      return application;
   }

   @Override
   public void onCreate() {
      super.onCreate();
      application = this;
      EasyTheme.init(this, ThemeSet.ThemeMode.Light, ThemeSet.Theme.DeepOrange);
      LitePal.initialize(this);

      // android 7.0系统解决uri外部
      StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
      StrictMode.setVmPolicy(builder.build());
      builder.detectFileUriExposure();
   }
}
