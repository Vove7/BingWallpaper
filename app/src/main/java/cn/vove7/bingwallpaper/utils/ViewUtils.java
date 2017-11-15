package cn.vove7.bingwallpaper.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

import java.util.HashMap;
import java.util.Map;

/*
* author:_SOLID
* */
public class ViewUtils {

   private static Map<String, Fragment> fragmentList = new HashMap<>();

   /**
    * 根据Class创建Fragment
    *
    * @param clazz the Fragment of create
    * @return
    */
   public static Fragment createFragment(Class<?> clazz, boolean isObtain) {
      Fragment resultFragment = null;
      String className = clazz.getName();
      if (fragmentList.containsKey(className)) {
         resultFragment = fragmentList.get(className);
      } else {
         try {
            resultFragment = (Fragment) Class.forName(className).newInstance();
         } catch (Exception e) {
            e.printStackTrace();
         }
         if (isObtain)
            fragmentList.put(className, resultFragment);
      }
      return resultFragment;
   }

   public static Fragment createFragment(Class<?> clazz) {
      return createFragment(clazz, true);
   }


   public static int getScreenWidth(Context context) {
      DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
      return displayMetrics.widthPixels;
   }

   public static int getScreenHeight(Context context) {
      DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
      return displayMetrics.heightPixels;
   }
}
