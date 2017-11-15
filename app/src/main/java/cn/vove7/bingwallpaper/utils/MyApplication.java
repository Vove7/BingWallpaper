package cn.vove7.bingwallpaper.utils;

import android.app.Application;

import cn.vove7.bingwallpaper.activities.MainActivity;
import cn.vove7.bingwallpaper.fragments.GalleryFragment;
import cn.vove7.bingwallpaper.fragments.MainFragment;

/**
 * Created by Vove on 2017/11/10.
 *
 */

public class MyApplication extends Application {
   private static MyApplication application;
   private MainActivity mainActivity;
   private MainFragment mainFragment;
   private GalleryFragment galleryFragment;

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
      return application;
   }

   @Override
   public void onCreate() {
      super.onCreate();
      application = this;
   }
}
