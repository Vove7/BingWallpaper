package cn.vove7.bingwallpaper.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.fragments.GalleryFragment;
import cn.vove7.bingwallpaper.fragments.MainFragment;
import cn.vove7.bingwallpaper.utils.DonateHelper;
import cn.vove7.bingwallpaper.utils.LogHelper;
import cn.vove7.bingwallpaper.utils.MyApplication;
import cn.vove7.bingwallpaper.utils.SettingHelper;
import cn.vove7.bingwallpaper.utils.Utils;
import cn.vove7.easytheme.BaseThemeActivity;
import cn.vove7.easytheme.EasyTheme;

public class MainActivity extends BaseThemeActivity
        implements NavigationView.OnNavigationItemSelectedListener {

   private FragmentManager mFragmentManager;
   private NavigationView navigationView;
   private Fragment mCurrentFragment;
   private MainFragment mainFragment;
   private GalleryFragment galleryFragment;
   private DrawerLayout drawer;
   Toolbar toolbar;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      MyApplication.getApplication().setMainActivity(this);
      super.onCreate(savedInstanceState);

      supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.activity_main);
      initComponentView();
      initDefaultFragment();
      requestPermission();
      SettingHelper.initSetting();
   }

   private void initDefaultFragment() {
      switchFragment(INDEX_MAIN_FRAGMENT);
      navigationView.getMenu().getItem(0)
              .setChecked(true);
   }

   private long t = 0;

   @Override
   public void onBackPressed() {
      if (drawer.isDrawerOpen(navigationView)) {
         drawer.closeDrawer(GravityCompat.START);
         return;
      }
      long now = System.currentTimeMillis();
      if (now - t < 2000) {
         finish();
      } else {
         t = now;
         Snackbar.make(navigationView, R.string.back_again_exit, Snackbar.LENGTH_SHORT).show();
      }
   }

   private static final int REQUEST_CODE_WRITE_EXTERNAL = 1;

   private void requestPermission() {
      if (ContextCompat.checkSelfPermission(MainActivity.this,
              Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
         ActivityCompat.requestPermissions(MainActivity.this,
                 new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL);
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      View email = findViewById(R.id.email_me);
      if (email != null)
         email.setOnClickListener(view -> {
            Uri uri = Uri.parse("mailto: 1132412166@qq.com");
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            startActivity(intent);
         });
      View bthSetting = findViewById(R.id.btn_setting);
      if (bthSetting != null)
         bthSetting.setOnClickListener(view ->
                 startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   protected void onStop() {
      LogHelper.logD("MainActivity onStop");
      super.onStop();
   }

   private void initComponentView() {
      drawer = findViewById(R.id.main_layout);
      //hideActionBar();
      toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      DrawerLayout drawer = findViewById(R.id.main_layout);
      ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
              this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
      drawer.addDrawerListener(toggle);
      toggle.syncState();
      navigationView = findViewById(R.id.nav_view);
      navigationView.setNavigationItemSelectedListener(this);
      navigationView.setMinimumWidth((int) (Utils.getScreenWidth(this) * 0.8));
      mFragmentManager = getSupportFragmentManager();
   }

   private static final int INDEX_MAIN_FRAGMENT = 0;
   private static final int INDEX_GALLERY_FRAGMENT = 1;

   //切换Fragment
   private void switchFragment(int fragmentIndex) {
      Fragment fragment = null;
      switch (fragmentIndex) {
         case INDEX_MAIN_FRAGMENT: {
            if (mainFragment == null)
               mainFragment = new MainFragment();
            fragment = mainFragment;
         }
         break;
         case INDEX_GALLERY_FRAGMENT: {
            if (galleryFragment == null) {
               galleryFragment = new GalleryFragment();
               mFragmentManager.beginTransaction()
                       .add(R.id.fragment_layout, galleryFragment).commit();
            }
            fragment = galleryFragment;
         }
         break;
      }
      if (mCurrentFragment == null) {
         mFragmentManager.beginTransaction()
                 .add(R.id.fragment_layout, fragment).commit();
      } else {
         mFragmentManager.beginTransaction()
                 .hide(mCurrentFragment)
                 .show(fragment).commit();
      }
      mCurrentFragment = fragment;

   }


   @SuppressWarnings("StatementWithEmptyBody")
   @Override
   public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      int id = item.getItemId();
      switch (id) {
         case R.id.nav_recent: {
            toolbar.setTitle(R.string.app_name);
            switchFragment(INDEX_MAIN_FRAGMENT);
         }
         break;
         case R.id.nav_clear: {
            clearCache();
         }
         break;
         case R.id.nav_theme: {

            EasyTheme.applyRandomTheme(this);
            Snackbar.make(navigationView, "这块留着ฅ•̀∀•́ฅ", Snackbar.LENGTH_SHORT).show();
         }
         break;
         case R.id.nav_donate: {
            new DonateHelper(this).donateWithAlipay();
         }
         break;
         case R.id.nav_gallery: {
            toolbar.setTitle(R.string.gallery);
            switchFragment(INDEX_GALLERY_FRAGMENT);
         }
         break;
         case R.id.nav_about: {

            Utils.openMarket(this, getPackageName());
            //AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            //View view = LayoutInflater.from(this).inflate(R.layout.layout_about, null);
            //view.findViewById(R.id.check_upgrade).setOnClickListener(v -> {
            //           Utils.openMarket(this, getPackageName());
            //           dialog.dismiss();
            //        }
            //);
            //
            //dialogBuilder.setView(view);
            //dialog = dialogBuilder.show();

         }
         break;
      }
      drawer.closeDrawer(GravityCompat.START);
      return true;
   }

   //Dialog dialog;


   @SuppressLint("StaticFieldLeak")
   public AsyncTask<Void, Void, Void> getTask() {
      return new AsyncTask<Void, Void, Void>() {
         @Override
         protected Void doInBackground(Void... voids) {
            Glide.get(MainActivity.this).clearDiskCache();
            return null;
         }
      };
   }

   private void clearCache() {
      AlertDialog.Builder dialog = new AlertDialog.Builder(this);
      dialog.setTitle(R.string.confirm_clear_cache);
      dialog.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
         AsyncTask<Void, Void, Void> clearTask = getTask();
         clearTask.execute();
         new Handler().postDelayed(() ->
                         Toast.makeText(MainActivity.this, getString(R.string.clear_successful), Toast.LENGTH_SHORT).show()
                 , 1000);
      });
      dialog.setNegativeButton(R.string.cancel, null);
      dialog.show();
   }

   @Override
   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      switch (requestCode) {
         case REQUEST_CODE_WRITE_EXTERNAL: {
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)) {
               Toast.makeText(this, R.string.grant_write_permission, Toast.LENGTH_SHORT).show();
               finish();//
            }
         }
         break;
      }

   }
}