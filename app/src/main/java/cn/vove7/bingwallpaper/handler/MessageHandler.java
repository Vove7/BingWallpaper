package cn.vove7.bingwallpaper.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;

import cn.vove7.bingwallpaper.activities.MainActivity;
import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.adapters.RecViewAdapter;
import cn.vove7.bingwallpaper.utils.LogHelper;
import cn.vove7.bingwallpaper.utils.XmlContentHandler;


public class MessageHandler extends Handler {
   private Context context;
   private MainActivity mainActivity;
   public static final int ACTION_REFRESH_GET = 1;//初始刷新
   public static final int ACTION_REFRESH_GET_WITH_IMAGE = 2;//初始成功（已加载图片），下拉刷新
   public static final int ACTION_LOAD_MORE = 3;//加载更多，/OR网络错误


   public static final int NET_NORMAL = 11;//网络正常
   public static final int NET_ERROR = 10;//无网络
   public static final int NET_NO_BODY = 13;//无信息

   public MessageHandler(Context context) {
      this.context = context;
      mainActivity = (MainActivity) context;
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
      String xmlData = msg.getData().getString("xmlData");
      switch (msg.arg2) {
         case ACTION_REFRESH_GET_WITH_IMAGE: {
            mainActivity.clearImages();//清空
            if (parseXMLWithSax(xmlData)) {
               mainActivity.notifyRefreshRecView();
            } else {//解析失败
               parseFailed(msg);
            }
         }
         break;
         case ACTION_LOAD_MORE: {
            if (parseXMLWithSax(xmlData)) {
               mainActivity.setAllLoad(true);
               mainActivity.getRecyclerAdapter()
                       .setFooter(RecViewAdapter.STATUS_ALL_OK);
            } else {
               mainActivity.getRecyclerAdapter()
                       .setFooter(RecViewAdapter.STATUS_XML_ERROR);
            }
            mainActivity.setOnRefreshing(false);
         }
         break;
         case ACTION_REFRESH_GET: {
            if (parseXMLWithSax(xmlData)) {
               mainActivity.showRecView();
               mainActivity.notifyRefreshRecView();
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
            mainActivity.showNetErrView();
         }
         break;
         case ACTION_REFRESH_GET_WITH_IMAGE: {
            Snackbar.make(mainActivity.getRecyclerView()
                    , R.string.no_net, Snackbar.LENGTH_SHORT).show();
            mainActivity.stopRefreshing();

         }
         break;
         case ACTION_LOAD_MORE: {
            mainActivity.getRecyclerAdapter()
                    .setFooter(RecViewAdapter.STATUS_NET_ERROR);
         }
         mainActivity.setOnRefreshing(false);
         break;

      }
   }

   private void refreshWithNoBody(Message message) {
      Snackbar.make(mainActivity.getRecyclerView(),
              R.string.no_response_body, Snackbar.LENGTH_SHORT).show();
      mainActivity.stopRefreshing();

      switch (message.arg2) {
         case ACTION_REFRESH_GET:

      }
   }

   private void parseFailed(Message message) {
      Snackbar.make(mainActivity.getRecyclerView(),
              R.string.parse_xml_error, Snackbar.LENGTH_SHORT).show();
      mainActivity.stopRefreshing();
   }

   private boolean parseXMLWithSax(String xmlData) {
      try {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         XMLReader reader = factory.newSAXParser().getXMLReader();

         XmlContentHandler xmlContentHandler = new XmlContentHandler();
         reader.setContentHandler(xmlContentHandler);
         reader.parse(new InputSource(new StringReader(xmlData)));
         LogHelper.logD("urlList->", xmlContentHandler.getUrlList().toString());

         ((MainActivity) context).addBingImages(xmlContentHandler.getBingImages());
         return true;
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }
}