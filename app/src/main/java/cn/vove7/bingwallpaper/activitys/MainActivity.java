package cn.vove7.bingwallpaper.activitys;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.adapters.RecViewAdapter;
import cn.vove7.bingwallpaper.handler.MessageHandler;
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
   private boolean isAllLoad = false;//是否全部加载

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      initComponentView();
      initMainView();
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
            LogHelper.logD("dx:dy->", dx + ":" + dy);
            LogHelper.logD("isBottom->", String.valueOf(isSlideToBottom()));
            LogHelper.logD("isAllLoad>", String.valueOf(isAllLoad));
            if (!isAllLoad && isSlideToBottom() && !onRefreshing) {//上拉加载,
               onRefreshing=true;
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
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();

      //noinspection SimplifiableIfStatement
      if (id == R.id.action_settings) {
         return true;
      }

      return super.onOptionsItemSelected(item);
   }

   @SuppressWarnings("StatementWithEmptyBody")
   @Override
   public boolean onNavigationItemSelected(MenuItem item) {
      // Handle navigation view item clicks here.
      int id = item.getItemId();

      if (id == R.id.nav_gallery) {

      } else if (id == R.id.nav_share) {

      } else if (id == R.id.nav_donate) {

      }

      DrawerLayout drawer = findViewById(R.id.drawer_layout);
      drawer.closeDrawer(GravityCompat.START);
      return true;
   }
}
