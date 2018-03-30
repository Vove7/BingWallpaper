package cn.vove7.bingwallpaper.model;

import org.litepal.crud.DataSupport;

public class DlHis extends DataSupport{
   private String url;
   private boolean isFinish;

   public DlHis(String url, boolean isFinish) {
      this.url = url;
      this.isFinish = isFinish;
   }

   public String getUrl() {
      return url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public boolean isFinish() {
      return isFinish;
   }

   public void setFinish(boolean finish) {
      isFinish = finish;
   }
}
