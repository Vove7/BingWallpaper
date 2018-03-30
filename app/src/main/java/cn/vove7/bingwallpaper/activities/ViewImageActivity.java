package cn.vove7.bingwallpaper.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.adapters.ViewPageAdapter;
import cn.vove7.bingwallpaper.services.DownloadService;
import cn.vove7.bingwallpaper.services.DownloadServiceConnection;
import cn.vove7.bingwallpaper.utils.BingImage;
import cn.vove7.bingwallpaper.utils.LogHelper;
import cn.vove7.bingwallpaper.utils.MyApplication;
import cn.vove7.bingwallpaper.utils.Utils;

import static cn.vove7.bingwallpaper.adapters.ViewPageAdapter.IMAGE_FROM_INTERNET;
import static cn.vove7.bingwallpaper.adapters.ViewPageAdapter.IMAGE_FROM_LOCAL;
import static cn.vove7.bingwallpaper.services.DownloadService.IMAGE_DIRECTORY;
import static cn.vove7.bingwallpaper.services.DownloadService.RESOLUTION_RATIO_1080;
import static cn.vove7.bingwallpaper.services.DownloadService.RESOLUTION_RATIO_1200;
import static cn.vove7.bingwallpaper.utils.Utils.isLocalHave;

public class ViewImageActivity extends AppCompatActivity implements View.OnClickListener {

   private ArrayList<BingImage> bingImages = new ArrayList<>();
   private ViewPageAdapter viewPageAdapter;

   private ViewPager viewPager;
   private int imageFrom;
   private ImageButton downloadBtn;
   //   private ImageButton shareBtn;
   private ImageButton deleteBtn;
   private ImageButton moreBtn;

   private DownloadServiceConnection downloadConnection = new DownloadServiceConnection();


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      MyApplication.getApplication().setViewImageActivity(this);
      Intent intent = getIntent();
      String[] imageUrls = intent.getStringArrayExtra("images");
      String[] startdates = intent.getStringArrayExtra("startdates");
      String[] hshs = intent.getStringArrayExtra("hshs");
      imageFrom = intent.getIntExtra("from", ViewPageAdapter.IMAGE_FROM_INTERNET);
      addToArray(imageUrls, startdates, hshs);
      final int pos = intent.getIntExtra("pos", 0);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_view_image);

      viewPageAdapter = new ViewPageAdapter(this, bingImages, imageFrom);


      downloadBtn = findViewById(R.id.view_download);
      downloadBtn.setOnClickListener(this);
//      shareBtn = findViewById(R.id.view_share);
//      shareBtn.setOnClickListener(this);
      deleteBtn = findViewById(R.id.view_delete);
      deleteBtn.setOnClickListener(this);
      moreBtn = findViewById(R.id.view_more);
      registerForContextMenu(moreBtn);//注册长按menu
      registerForContextMenu(downloadBtn);//注册下载
      moreBtn.setOnClickListener(this);

      findViewById(R.id.view_back).setOnClickListener(this);


      // Set up the ViewPager with the sections adapter.
      viewPager = findViewById(R.id.container);
      viewPager.setAdapter(viewPageAdapter);
      viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
         @Override
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

         }

         @Override
         public void onPageSelected(int position) {
            setButtonStatus(position);
         }

         @Override
         public void onPageScrollStateChanged(int state) {

         }
      });
      viewPager.setCurrentItem(pos);
      //设置下载按钮状态
      setButtonStatus(pos);

      intentService = new Intent(this, DownloadService.class);
      bindService(intentService, downloadConnection, BIND_AUTO_CREATE);
      startService(intentService);
   }

   Intent intentService;
   @Override
   protected void onStop() {
      LogHelper.logD("ViewActivity unbindService");

      unbindService(downloadConnection);
      stopService(intentService);
      super.onStop();
   }

   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      switch (v.getId()) {
         case R.id.view_more: {
            getMenuInflater().inflate(R.menu.view_more_menu, menu);
         }
         break;
         case R.id.view_download: {
            getMenuInflater().inflate(R.menu.view_download_menu, menu);

            int result = isLocalHave(bingImages.get(viewPager.getCurrentItem())
                    .getStartDate());
            menu.getItem(0).setVisible((result % 2) == 0);
            menu.getItem(1).setVisible((result / 2) == 0);
         }
         break;
      }
   }

   private static final int SET_WALLPAPER = 0;

   @Override
   public boolean onContextItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.view_share: {
            final int pos = viewPager.getCurrentItem();
            File imgFile = new File(getPath(pos));
            if (imgFile.exists()) {//本地存在
               Utils.shareImageTo(imgFile);
            } else {
               Toast.makeText(this, R.string.please_download, Toast.LENGTH_SHORT).show();
            }
         }
         break;
         case R.id.view_set_wallpaper: {
            final int pos = viewPager.getCurrentItem();
            if (!new File(getPath(pos)).exists()) {
               Toast.makeText(this, R.string.please_download, Toast.LENGTH_SHORT).show();
               return false;
            }
            //Utils.setWallpaper(this,BitmapFactory.decodeFile(getPath(pos)));
            setWallpaperWithChoose(pos);
         }
         break;
         case R.id.view_download_1080: {
            downloadImage(RESOLUTION_RATIO_1080);
         }
         break;
         case R.id.view_download_1200: {
            downloadImage(RESOLUTION_RATIO_1200);
         }
         break;
      }
      return false;
   }

   private void setWallpaperWithChoose(int pos) {
      Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      intent.putExtra("mimeType", "image/*");
      Uri uri = Uri.parse(MediaStore.Images.Media
              .insertImage(getContentResolver(),
                      BitmapFactory.decodeFile(getPath(pos)), getString(R.string.text_set_wallpaper), null));
      intent.setData(uri);
      startActivityForResult(intent, SET_WALLPAPER);
   }

   private void downloadImage(int rr) {
      int pos = viewPager.getCurrentItem();
      if (!downloadConnection.getDownloadBinder().isDownloading()) {
         Toast.makeText(this, R.string.begin_download, Toast.LENGTH_SHORT).show();
         BingImage bingImage = bingImages.get(pos);
         ArrayList<BingImage> arrayList = new ArrayList<>();
         arrayList.add(bingImage);
         downloadConnection.getDownloadBinder().startDownload(arrayList, true, rr);
      } else {
         Toast.makeText(this, R.string.file_already_exist, Toast.LENGTH_SHORT).show();
      }

   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      switch (requestCode) {
         case SET_WALLPAPER: {//设置壁纸返回
         }
         break;
      }

   }

   private void addToArray(String[] imageUrls, String[] startdates, String[] hshs) {
      for (int i = 0; i < startdates.length; i++) {
         if (imageFrom == IMAGE_FROM_INTERNET)
            bingImages.add(new BingImage(startdates[i], imageUrls[i], hshs[i]));
         else
            bingImages.add(new BingImage(startdates[i]));
      }
   }

   public void setButtonStatus(int position) {
      if (position == -1)
         position = viewPager.getCurrentItem();
      LogHelper.logD(null, "position---->" + position);
      if (imageFrom == IMAGE_FROM_LOCAL) {
         deleteBtn.setVisibility(View.VISIBLE);
         downloadBtn.setVisibility(View.GONE);
//         shareBtn.setVisibility(View.VISIBLE);
         return;
      }
      //main调用
      deleteBtn.setVisibility(View.GONE);


      int result = isLocalHave(bingImages.get(position).getStartDate());
      if (result == 3) {//两个分辨率图片都存在
         downloadBtn.setVisibility(View.GONE);
      } else {
         downloadBtn.setVisibility(View.VISIBLE);
      }
   }

   @Override
   public void onClick(View view) {
      final int pos = viewPager.getCurrentItem();
      switch (view.getId()) {
         case R.id.view_download: {
            openContextMenu(downloadBtn);
         }
         break;
         case R.id.view_back: {//返回
            new Handler().postDelayed(this::onBackPressed, 100);
         }
         break;
         case R.id.view_delete: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.confirm_delete);
            builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
               File file = new File(getPath(pos));
               if (file.exists()) {
                  if (file.delete()) {
                     Toast.makeText(ViewImageActivity.this, R.string.delete_successful, Toast.LENGTH_SHORT).show();
                     if (imageFrom == IMAGE_FROM_LOCAL) {
                        MyApplication.getApplication().getGalleryFragment().refreshView();
                     } else {
                        setButtonStatus(pos);
                     }
                     onBackPressed();//返回
                  }
               }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
         }
         break;
         case R.id.view_more: {//单击打开菜单
            openContextMenu(moreBtn);
         }
         break;
      }
   }

   private String getPath(int pos) {
      return IMAGE_DIRECTORY + "/" + bingImages.get(pos).getStartDate() + (imageFrom == IMAGE_FROM_LOCAL ? "" : ".jpg");
   }

}
