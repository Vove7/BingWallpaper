package cn.vove7.bingwallpaper.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;


import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.fragments.MainFragment;
import cn.vove7.bingwallpaper.fragments.GalleryFragment;
import cn.vove7.bingwallpaper.utils.DonateHelper;
import cn.vove7.bingwallpaper.utils.MyApplication;
import cn.vove7.bingwallpaper.utils.ViewUtils;

import static cn.vove7.bingwallpaper.utils.ViewUtils.createFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

   private FragmentManager mFragmentManager;
   private NavigationView navigationView;
   private Fragment mCurrentFragment;
   private MenuItem mPreMenuItem;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      MyApplication.getApplication().setMainActivity(this);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      initComponentView();
      initDefaultFragment();
      requestPermission();
   }

   private void initDefaultFragment() {

      mCurrentFragment = ViewUtils.createFragment(MainFragment.class);

      mFragmentManager.beginTransaction().add(R.id.fragment_layout, mCurrentFragment).commit();
      mPreMenuItem = navigationView.getMenu().getItem(0);
      mPreMenuItem.setChecked(true);
   }

   private long t = 0;

   @Override
   public void onBackPressed() {
      long now = System.currentTimeMillis();
      if (now - t < 1000) {

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
      findViewById(R.id.email_me).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Uri uri = Uri.parse("mailto: 1132412166@qq.com");
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            startActivity(intent);
         }
      });
      return super.onCreateOptionsMenu(menu);
   }

   private void initComponentView() {
      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      DrawerLayout drawer = findViewById(R.id.main_layout);
      ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
              this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
      drawer.addDrawerListener(toggle);
      toggle.syncState();
      navigationView = findViewById(R.id.nav_view);
      navigationView.setNavigationItemSelectedListener(this);
      mFragmentManager = getSupportFragmentManager();
   }


   //切换Fragment
   private void switchFragment(Class<?> clazz) {
      Fragment to = createFragment(clazz);
      if (to.isAdded()) {
         mFragmentManager.beginTransaction().hide(mCurrentFragment).show(to).commitAllowingStateLoss();
      } else {
         mFragmentManager.beginTransaction().hide(mCurrentFragment).add(R.id.fragment_layout, to).commitAllowingStateLoss();
      }
      mCurrentFragment = to;
   }


   @SuppressWarnings("StatementWithEmptyBody")
   @Override
   public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      int id = item.getItemId();
      switch (id) {
         case R.id.nav_recent: {
            switchFragment(MainFragment.class);
         }
         break;
         case R.id.nav_clear: {
            clearCache();
         }
         break;
         case R.id.nav_theme: {
            //
            Snackbar.make(navigationView, "这块留着ฅ•̀∀•́ฅ", Snackbar.LENGTH_SHORT).show();
         }
         break;
         case R.id.nav_donate: {
            new DonateHelper(this).donateWithAlipay();
         }
         break;
         case R.id.nav_gallery: {
            switchFragment(GalleryFragment.class);
            GalleryFragment fragment = MyApplication.getApplication().getGalleryFragment();
            if (fragment != null)
               fragment.refreshView();
         }
         break;
         case R.id.nav_about: {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setView(R.layout.layout_about);
            dialog.show();
         }
         break;
      }

      DrawerLayout drawer = findViewById(R.id.main_layout);
      drawer.closeDrawer(GravityCompat.START);
      return true;
   }

   @SuppressLint("StaticFieldLeak")
   private AsyncTask clearTask = new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] objects) {
         Glide.get(MainActivity.this).clearDiskCache();
         return null;
      }
   };

   private void clearCache() {
      AlertDialog.Builder dialog = new AlertDialog.Builder(this);
      dialog.setTitle(R.string.confirm_clear_cache);
      dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialogInterface, int i) {
            clearTask.execute();
            Toast.makeText(MainActivity.this, getString(R.string.clear_successful), Toast.LENGTH_SHORT).show();
         }
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
      }

   }
}