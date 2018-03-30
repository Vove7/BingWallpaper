package cn.vove7.bingwallpaper.activities;


import android.os.Bundle;

import cn.vove7.easytheme.EasyTheme;

public class BasePreferenceActivity extends AppCompatPreferenceActivity {
   long lastChange = 0;
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      lastChange = EasyTheme.lastChange;
      initTheme();
   }

   //初始化主题
   private void initTheme() {
      setTheme(EasyTheme.currentThemeId);
   }

   @Override
   protected void onResume() {
      super.onResume();
      if (lastChange != EasyTheme.lastChange) {
         lastChange = EasyTheme.lastChange;
         EasyTheme.applyTheme(this);
      }
   }

   @Override
   public void finish() {
      super.finish();
      if (lastChange != EasyTheme.lastChange) {
         //切换动画
         overridePendingTransition(EasyTheme.enterAnim, EasyTheme.exitAnim);
      }
   }
}
