package cn.vove7.bingwallpaper.activitys;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.adapters.RecViewAdapter;
import cn.vove7.bingwallpaper.handler.MessageHandler;
import cn.vove7.bingwallpaper.services.DownloadService;
import cn.vove7.bingwallpaper.services.DownloadService.DownloadBinder;
import cn.vove7.bingwallpaper.utils.BingImage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import cn.vove7.bingwallpaper.utils.LogHelper;
import okhttp3.ResponseBody;

import static cn.vove7.bingwallpaper.handler.MessageHandler.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
   private ArrayList<BingImage> bingImages = new ArrayList<>();
   private SwipeRefreshLayout swipeRefreshLayout;
   private RecViewAdapter recyclerAdapter;
   private RecyclerView recyclerView;
   private View netErrorLayout;
   private View loadingLayout;
   private MessageHandler messageHandler;
   private DownloadBinder downloadBinder;
   private boolean isAllLoad = false;//是否全部加载

   //下载服务
   private ServiceConnection connection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
         downloadBinder = (DownloadBinder) iBinder;
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
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      initComponentView();
      initMainView();

      Intent intent = new Intent(MainActivity.this, DownloadService.class);
      startService(intent);
      bindService(intent, connection, BIND_AUTO_CREATE);

      requestPermission();
   }

   private long t = 0;

   @Override
   public void onBackPressed() {
      long now = System.currentTimeMillis();
      if (now - t < 1000) {
         unbindService(connection);
         Glide.get(this).clearMemory();
         finish();
      } else {
         t = now;
         Snackbar.make(recyclerView, R.string.back_again_exit, Snackbar.LENGTH_SHORT).show();
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

   private void initMainView() {//初始化主界面
      getBingImages(ACTION_REFRESH_GET);
   }

   public void setAllLoad(boolean allLoad) {
      isAllLoad = allLoad;
   }

   public boolean haveImages() {
      return !(bingImages == null || bingImages.size() == 0);
   }

   public void clearImages() {
      if (bingImages != null) {
         bingImages.clear();
         isAllLoad = false;
      }
   }

   public RecyclerView getRecyclerView() {
      return recyclerView;
   }


   public RecViewAdapter getRecyclerAdapter() {
      return recyclerAdapter;
   }

   private void initComponentView() {
      messageHandler = new MessageHandler(this);
      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      DrawerLayout drawer = findViewById(R.id.drawer_layout);
      ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
              this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
      drawer.addDrawerListener(toggle);
      toggle.syncState();
      ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this);

      //下拉刷新控件
      swipeRefreshLayout = findViewById(R.id.swipe_refresh);
      swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
         @Override
         public void onRefresh() {//下拉刷新
            if (haveImages()) {//有图
               getBingImages(ACTION_REFRESH_GET_WITH_IMAGE);
            } else {
               getBingImages(ACTION_REFRESH_GET);
            }
//            refreshImage();
         }
      });
      //netErrorLayout
      netErrorLayout = findViewById(R.id.net_error_layout);
      //loadingLayout
      loadingLayout = findViewById(R.id.loading_layout);

      //recycleView
      recyclerView = findViewById(R.id.recycle_view);
      LinearLayoutManager layoutManager = new LinearLayoutManager(this);
      recyclerView.setLayoutManager(layoutManager);
      recyclerAdapter = new RecViewAdapter(this, bingImages);
      recyclerView.setAdapter(recyclerAdapter);
      recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {//上拉加载
         @Override
         public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
//            LogHelper.logD("dx:dy->", dx + ":" + dy);
//            LogHelper.logD("isBottom->", String.valueOf(isSlideToBottom()));
//            LogHelper.logD("isAllLoad>", String.valueOf(isAllLoad));
            if (!isAllLoad && isSlideToBottom() && !onRefreshing) {//上拉加载,
               onRefreshing = true;
               recyclerAdapter.setFooter(RecViewAdapter.STATUS_LOADING);//显示footer
               new Handler().postDelayed(new Runnable() {
                  @Override
                  public void run() {
                     getBingImages(ACTION_LOAD_MORE);
                  }
               }, 1000);//延时
            }
         }
      });
   }

   private boolean onRefreshing = false;//上拉正在刷新标志

   public void setOnRefreshing(boolean onRefreshing) {
      this.onRefreshing = onRefreshing;
   }

   protected boolean isSlideToBottom() {
      return recyclerView != null &&
              (recyclerView.computeVerticalScrollExtent() +
                      recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange());

   }

   public void getBingImages(final int getAction) {
      int pageIndex =
              getAction == ACTION_LOAD_MORE ? 1 : 0;
      OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
      Request request = new Request.Builder().url(getXmlUrl(pageIndex))
              .get().build();
      Call call = client.newCall(request);
      call.enqueue(new Callback() {
         private Message message = new Message();

         @Override
         public void onFailure(@NonNull Call call, @NonNull IOException e) {//响应失败更新UI
            message.arg1 = NET_ERROR;//失败标志
            message.arg2 = getAction;
            messageHandler.sendMessage(message);
         }

         @Override
         public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {//响应成功更新UI
            ResponseBody body = response.body();
            if (body == null) {
               message.arg1 = NET_NO_BODY;//无body
            } else {
               Bundle bundle = new Bundle();
               message.arg1 = NET_NORMAL;//成功标志
               String bodyStr = body.string();
               bundle.putString("xmlData", bodyStr);
               message.setData(bundle);
            }
            message.arg2 = getAction;
            messageHandler.sendMessage(message);
         }
      });
   }


   public void stopRefreshing() {
      if (swipeRefreshLayout.isRefreshing())
         swipeRefreshLayout.setRefreshing(false);
   }

   public void notifyRefreshRecView() {
      recyclerAdapter.notifyDataSetChanged();//主线程
      stopRefreshing();
   }

   public void showNetErrView() {
      loadingLayout.setVisibility(View.GONE);
      recyclerView.setVisibility(View.GONE);
      netErrorLayout.setVisibility(View.VISIBLE);
      if (swipeRefreshLayout.isRefreshing())
         swipeRefreshLayout.setRefreshing(false);
   }

   public void showRecView() {
      loadingLayout.setVisibility(View.GONE);
      recyclerView.setVisibility(View.VISIBLE);
      netErrorLayout.setVisibility(View.GONE);
      if (swipeRefreshLayout.isRefreshing())
         swipeRefreshLayout.setRefreshing(false);
   }

   public void addBingImages(ArrayList<BingImage> images) {
      if (bingImages == null) {
         bingImages = images;
      } else bingImages.addAll(images);
   }

   private static final int[][] index = {{0, 7}, {8, 8}};

   private String getXmlUrl(int pageIndex) {
      return "http://www.bing.com/HpImageArchive.aspx?format=xml&idx=" +
              index[pageIndex][0] + "&n=" + index[pageIndex][1] + "&mkt=zh-CN";
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();

      if (id == R.id.menu_download_all) {
         if (!downloadBinder.isDownloading()) {
            Snackbar.make(recyclerView, getString(R.string.begin_download), Snackbar.LENGTH_SHORT).show();
            downloadBinder.startDownload(bingImages, this);
         } else {
            Snackbar.make(recyclerView, getString(R.string.is_downloading), Snackbar.LENGTH_SHORT).show();
         }
         return true;
      } else if (id == R.id.menu_share) {
         //
      }

      return super.onOptionsItemSelected(item);
   }

   @SuppressWarnings("StatementWithEmptyBody")
   @Override
   public boolean onNavigationItemSelected(MenuItem item) {
      int id = item.getItemId();

      switch (id) {
         case R.id.nav_clear: {
            clearCache();
         }
         break;
         case R.id.nav_donate: {
         }
         break;
      }

      DrawerLayout drawer = findViewById(R.id.drawer_layout);
      drawer.closeDrawer(GravityCompat.START);
      return true;
   }

   private void clearCache() {
      new AsyncTask() {
         @Override
         protected Object doInBackground(Object[] objects) {
            Glide.get(MainActivity.this).clearDiskCache();
            return null;
         }
      }.execute();
      Snackbar.make(recyclerView, "已清空", Snackbar.LENGTH_SHORT).show();
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
