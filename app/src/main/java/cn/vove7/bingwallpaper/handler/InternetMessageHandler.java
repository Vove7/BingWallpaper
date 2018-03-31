package cn.vove7.bingwallpaper.handler;

import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.adapters.RecViewAdapter;
import cn.vove7.bingwallpaper.fragments.MainFragment;
import cn.vove7.bingwallpaper.utils.BingImages;
import cn.vove7.bingwallpaper.utils.MyApplication;


public class InternetMessageHandler extends Handler {
   private MainFragment mainFragment;
   public static final int ACTION_REFRESH_GET = 1;//初始刷新
   public static final int ACTION_REFRESH_GET_WITH_IMAGE = 2;//初始成功（已加载图片），下拉刷新
   public static final int ACTION_LOAD_MORE = 3;//加载更多，/OR网络错误


   public static final int NET_NORMAL = 11;//网络正常
   public static final int NET_ERROR = 10;//无网络
   public static final int NET_NO_BODY = 13;//无信息

   public InternetMessageHandler(MainFragment fragment) {
      mainFragment = fragment;
   }

   @Override
   public void handleMessage(Message msg) {
      switch (msg.arg1) {
         case NET_ERROR:
            refreshUIWithFailure(msg);
            break;
         case NET_NORMAL:
            refreshUIWithSuccess(msg);
            break;
         case NET_NO_BODY: {//无请求体
            refreshWithNoBody(msg);
         }
         break;
      }
   }

   private void refreshUIWithSuccess(Message msg) {
      String jsonData = msg.getData().getString("jsonData");
      switch (msg.arg2) {
         case ACTION_REFRESH_GET_WITH_IMAGE: {
            mainFragment.clearImages();//清空
            if (parseJson(jsonData)) {
               mainFragment.addNowPage();
               mainFragment.notifyRefreshRecView();
            } else {//解析失败
               parseFailed(msg);
            }
         }
         break;
         case ACTION_LOAD_MORE: {
            if (parseJson(jsonData)) {
               mainFragment.addNowPage();
               mainFragment.getRecyclerAdapter()
                       .setFooter(RecViewAdapter.STATUS_ALL_OK);
            } else {
               mainFragment.getRecyclerAdapter()
                       .setFooter(RecViewAdapter.STATUS_XML_ERROR);
            }
            mainFragment.stopRefreshing();
         }
         break;
         case ACTION_REFRESH_GET: {
            if (parseJson(jsonData)) {
               mainFragment.addNowPage();
               mainFragment.showRecView();//
               mainFragment.notifyRefreshRecView();
            } else {//解析失败
               parseFailed(msg);
            }
         }
         break;
      }

   }

   private void refreshUIWithFailure(Message message) {
      switch (message.arg2) {
         case ACTION_REFRESH_GET: {
            mainFragment.showNetErrView();
         }
         break;
         case ACTION_REFRESH_GET_WITH_IMAGE: {
            Snackbar.make(mainFragment.getRecyclerView()
                    , R.string.no_net, Snackbar.LENGTH_SHORT).show();
            mainFragment.stopRefreshing();

         }
         break;
         case ACTION_LOAD_MORE: {
            mainFragment.getRecyclerAdapter()
                    .setFooter(RecViewAdapter.STATUS_NET_ERROR);
         }
         mainFragment.stopRefreshing();
         break;

      }
   }

   private void refreshWithNoBody(Message message) {
      Snackbar.make(mainFragment.getRecyclerView(),
              R.string.no_response_body, Snackbar.LENGTH_SHORT).show();
      mainFragment.stopRefreshing();

      switch (message.arg2) {
         case ACTION_REFRESH_GET:

      }
   }

   private void parseFailed(Message message) {
      Snackbar.make(mainFragment.getRecyclerView(),
              R.string.parse_json_error, Snackbar.LENGTH_SHORT).show();
      mainFragment.stopRefreshing();
      mainFragment.showNetErrView();
   }

   private boolean parseJson(String jsonData) {
      Gson gson = new GsonBuilder().create();
      BingImages images = null;
      try {
         images = gson.fromJson(jsonData, BingImages.class);
      } catch (JsonSyntaxException e) {
         Toast.makeText(MyApplication.getApplication().getMainActivity(),
                 "Bing资源获取失败", Toast.LENGTH_SHORT).show();
         e.printStackTrace();
      }

      if (images != null) {
//         LogHelper.d(null,images.toString());
         mainFragment.addBingImages(images.getImages());
         return true;
      }
      return false;
   }
}