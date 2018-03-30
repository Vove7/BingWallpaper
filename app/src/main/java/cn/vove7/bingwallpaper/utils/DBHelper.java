package cn.vove7.bingwallpaper.utils;

import org.litepal.crud.DataSupport;

import cn.vove7.bingwallpaper.model.DlHis;

public class DBHelper {
   public static boolean haveDownloaded(String url) {
      return DataSupport.where("url=?", url)
              .where("isFinish = ?", "1")
              .findFirst(DlHis.class) != null;
   }

   public static void setDownloadOk(String url) {
      DlHis his = new DlHis(url, true);
      his.save();
   }
}
