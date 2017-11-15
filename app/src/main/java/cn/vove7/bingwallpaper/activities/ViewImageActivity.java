package cn.vove7.bingwallpaper.activities;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.adapters.ViewPageAdapter;
import cn.vove7.bingwallpaper.services.DownloadService;
import cn.vove7.bingwallpaper.utils.BingImage;
import cn.vove7.bingwallpaper.utils.LogHelper;
import cn.vove7.bingwallpaper.utils.MyApplication;
import cn.vove7.bingwallpaper.utils.Utils;

import static cn.vove7.bingwallpaper.adapters.ViewPageAdapter.IMAGE_FROM_LOCAL;
import static cn.vove7.bingwallpaper.services.DownloadService.*;
import static cn.vove7.bingwallpaper.utils.Utils.isFileExist;

public class ViewImageActivity extends AppCompatActivity implements View.OnClickListener {

   private String[] images;
   private String[] startdates;
   private ViewPageAdapter viewPageAdapter;

   private ViewPager mViewPager;
   private int imageFrom;
   private ImageButton downloadBtn;
   private ImageButton shareBtn;
   private ImageButton deleteBtn;

   private DownloadService.DownloadBinder downloadBinder;


   private ServiceConnection connection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
         downloadBinder = (DownloadService.DownloadBinder) iBinder;
         LogHelper.logD("serCon->", "onServiceConnected*******");
      }

      @Override
      public void onBindingDied(ComponentName name) {
         LogHelper.logD("serCon->", "onDied*******");
         downloadBinder = null;
      }

      @Override
      public void onServiceDisconnected(ComponentName componentName) {
         LogHelper.logD("serCon->", "onServiceDisconnected*******");
         downloadBinder = null;
      }
   };


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      Intent intent = getIntent();
      images = intent.getStringArrayExtra("images");
      startdates = intent.getStringArrayExtra("startdates");
      imageFrom = intent.getIntExtra("from", ViewPageAdapter.IMAGE_FROM_INTERNET);
      final int pos = intent.getIntExtra("pos", 0);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_view_image);

      // Create the adapter that will return a fragment for each of the three
      // primary sections of the activity.
      viewPageAdapter = new ViewPageAdapter(this, images, startdates, imageFrom);


      downloadBtn = findViewById(R.id.view_download);
      downloadBtn.setOnClickListener(this);
      shareBtn = findViewById(R.id.view_share);
      shareBtn.setOnClickListener(this);
      deleteBtn = findViewById(R.id.view_delete);
      deleteBtn.setOnClickListener(this);

      findViewById(R.id.view_back).setOnClickListener(this);


      // Set up the ViewPager with the sections adapter.
      mViewPager = findViewById(R.id.container);
      mViewPager.setAdapter(viewPageAdapter);
      mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
         @Override
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

         }

         @Override
         public void onPageSelected(int position) {
            setDownloadBtn(position);
         }

         @Override
         public void onPageScrollStateChanged(int state) {

         }
      });
      mViewPager.setCurrentItem(pos);
      //设置下载按钮状态
      setDownloadBtn(pos);


      Intent intentService = new Intent(this, DownloadService.class);
      startService(intentService);
      bindService(intentService, connection, BIND_AUTO_CREATE);

   }

   private void setDownloadBtn(int position) {
      LogHelper.logD(null, "position---->" + position);
      if (imageFrom == IMAGE_FROM_LOCAL) {
         deleteBtn.setVisibility(View.VISIBLE);
         downloadBtn.setVisibility(View.GONE);
         return;
      }
      String filename = IMAGE_DIRECTORY + "/" + startdates[position] + ".jpg";
      if (isFileExist(filename)) {
         LogHelper.logD(null, "file exist");
         downloadBtn.setVisibility(View.GONE);
         deleteBtn.setVisibility(View.VISIBLE);
      } else {
         downloadBtn.setVisibility(View.VISIBLE);
         deleteBtn.setVisibility(View.GONE);
         LogHelper.logD(null, "file dont exist");
      }
   }

   @Override
   public void onClick(View view) {
      switch (view.getId()) {
         case R.id.view_download: {
            if (!downloadBinder.isDownloading()) {
               int pos = mViewPager.getCurrentItem();
               if (!isFileExist(IMAGE_DIRECTORY + "/" + startdates[pos] + ".jpg")) {
                  Toast.makeText(this, R.string.begin_download, Toast.LENGTH_SHORT).show();
                  BingImage bingImage = new BingImage(startdates[pos], images[pos]);
                  ArrayList<BingImage> arrayList = new ArrayList<>();
                  arrayList.add(bingImage);
                  downloadBinder.startDownload(arrayList);
               } else {
                  Toast.makeText(this, R.string.file_aleady_exist, Toast.LENGTH_SHORT).show();
               }
            } else {
               Toast.makeText(this, R.string.downloading, Toast.LENGTH_SHORT).show();
            }
         }
         break;
         case R.id.view_back: {//返回
            new Handler().postDelayed(new Runnable() {
               @Override
               public void run() {
                  onBackPressed();
               }
            }, 100);
         }
         break;
         case R.id.view_delete: {
            final int pos = mViewPager.getCurrentItem();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.confirm_delete);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialogInterface, int i) {
                  String filename = IMAGE_DIRECTORY + "/" +
                          startdates[pos] + (imageFrom == IMAGE_FROM_LOCAL ? "" : ".jpg");
                  File file = new File(filename);
                  if (file.exists()) {
                     if (file.delete()) {
                        Toast.makeText(ViewImageActivity.this, R.string.delete_successful, Toast.LENGTH_SHORT).show();
                        if (imageFrom == IMAGE_FROM_LOCAL)
                           MyApplication.getApplication().getGalleryFragment().refreshView();
                        onBackPressed();//返回
                     }
                  }
               }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
         }
         break;
         case R.id.view_share: {
            Utils.shareImageTo(null);
         }
         break;
      }
   }

}
