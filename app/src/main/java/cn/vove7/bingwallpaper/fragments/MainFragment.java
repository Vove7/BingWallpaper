package cn.vove7.bingwallpaper.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.content.ServiceConnection;

import java.io.IOException;

import android.content.ComponentName;
import android.os.IBinder;

import java.util.ArrayList;

import cn.vove7.bingwallpaper.services.DownloadService.DownloadBinder;

import java.util.concurrent.TimeUnit;

import android.os.Message;
import android.view.ViewGroup;

import cn.vove7.bingwallpaper.services.DownloadService;
import cn.vove7.bingwallpaper.utils.LogHelper;

import cn.vove7.bingwallpaper.handler.MessageHandler;
import cn.vove7.bingwallpaper.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.adapters.RecViewAdapter;
import cn.vove7.bingwallpaper.utils.BingImage;

import static android.content.Context.BIND_AUTO_CREATE;
import static cn.vove7.bingwallpaper.handler.MessageHandler.ACTION_LOAD_MORE;
import static cn.vove7.bingwallpaper.handler.MessageHandler.ACTION_REFRESH_GET;
import static cn.vove7.bingwallpaper.handler.MessageHandler.ACTION_REFRESH_GET_WITH_IMAGE;
import static cn.vove7.bingwallpaper.handler.MessageHandler.NET_ERROR;
import static cn.vove7.bingwallpaper.handler.MessageHandler.NET_NORMAL;
import static cn.vove7.bingwallpaper.handler.MessageHandler.NET_NO_BODY;

public class MainFragment extends Fragment {
   private View contentView;
   private SwipeRefreshLayout swipeRefreshLayout;
   private View netErrorLayout;
   private View loadingLayout;
   private ArrayList<BingImage> bingImages = new ArrayList<>();
   private MessageHandler messageHandler;

   private RecViewAdapter recyclerAdapter;
   private RecyclerView recyclerView;
   private DownloadService.DownloadBinder downloadBinder;

   public ArrayList<BingImage> getBingImages() {
      return bingImages;
   }

   private boolean isAllLoad = false;//是否全部加载

   public MainFragment() {
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      contentView = inflater.inflate(R.layout.fragment_main, container, false);
      initComponent();
      initMainView();

      return contentView;
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

   public RecViewAdapter getRecyclerAdapter() {
      return recyclerAdapter;
   }

   public RecyclerView getRecyclerView() {
      return recyclerView;
   }

   protected boolean isSlideToBottom() {
      return recyclerView != null &&
              (recyclerView.computeVerticalScrollExtent() +
                      recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange());

   }

   private boolean onRefreshing = false;//上拉正在刷新标志

   public void setOnRefreshing(boolean onRefreshing) {
      this.onRefreshing = onRefreshing;
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

   private static final int[][] index = {{0, 7}, {8, 8}};

   private String getXmlUrl(int pageIndex) {
      return "http://www.bing.com/HpImageArchive.aspx?format=xml&idx=" +
              index[pageIndex][0] + "&n=" + index[pageIndex][1] + "&mkt=zh-CN";
   }

   private void initMainView() {//初始化主界面
      getBingImages(ACTION_REFRESH_GET);
   }

   private void initComponent() {
      setHasOptionsMenu(true);//生效菜单

      messageHandler = new MessageHandler(this);
      //下拉刷新控件
      swipeRefreshLayout = (SwipeRefreshLayout) $(R.id.swipe_refresh);
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
      netErrorLayout = $(R.id.net_error_layout);
      //loadingLayout
      loadingLayout = $(R.id.loading_layout);

      //recycleView
      recyclerView = (RecyclerView) $(R.id.recycle_view);
      LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
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
      //开启服务
      Intent intent = new Intent(this.getContext(), DownloadService.class);
      getActivity().startService(intent);
      getActivity().bindService(intent, connection, BIND_AUTO_CREATE);
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      inflater.inflate(R.menu.download_menu, menu);
      super.onCreateOptionsMenu(menu, inflater);
   }

   @Override
   public void onDestroy() {
      getActivity().unbindService(connection);
      super.onDestroy();
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();

      if (id == R.id.menu_download_all) {
         if (!downloadBinder.isDownloading()) {
            Snackbar.make(recyclerView, getString(R.string.begin_download), Snackbar.LENGTH_SHORT).show();
            downloadBinder.startDownload(bingImages);
         } else {
            Snackbar.make(recyclerView, getString(R.string.is_downloading), Snackbar.LENGTH_SHORT).show();
         }
         return true;
      } else if (id == R.id.menu_share) {
         Utils.shareTo(this.getContext());
      }

      return super.onOptionsItemSelected(item);
   }


   private View $(int id) {
      return contentView.findViewById(id);

   }

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
}
