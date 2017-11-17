package cn.vove7.bingwallpaper.handler;

import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.adapters.RecViewAdapter;
import cn.vove7.bingwallpaper.fragments.MainFragment;
import cn.vove7.bingwallpaper.utils.BingImages;
import cn.vove7.bingwallpaper.utils.LogHelper;
import cn.vove7.bingwallpaper.utils.XmlContentHandler;


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
            mainFragment.setOnRefreshing(false);
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
         mainFragment.setOnRefreshing(false);
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
              R.string.parse_xml_error, Snackbar.LENGTH_SHORT).show();
      mainFragment.stopRefreshing();
   }

   private boolean parseJson(String jsonData) {
      Gson gson = new GsonBuilder().create();
      BingImages images = gson.fromJson(jsonData, BingImages.class);

      if (images != null) {
//         LogHelper.logD(null,images.toString());
         mainFragment.addBingImages(images.getImages());
         return true;
      }
      return false;
   }

   private boolean parseXMLWithSax(String xmlData) {
      try {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         XMLReader reader = factory.newSAXParser().getXMLReader();

         XmlContentHandler xmlContentHandler = new XmlContentHandler();
         reader.setContentHandler(xmlContentHandler);
         reader.parse(new InputSource(new StringReader(xmlData)));
         LogHelper.logD("urlList->", xmlContentHandler.getUrlList().toString());

         mainFragment.addBingImages(xmlContentHandler.getBingImages());
         return true;
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }
}