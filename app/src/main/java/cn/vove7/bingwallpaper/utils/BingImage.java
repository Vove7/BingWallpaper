package cn.vove7.bingwallpaper.utils;


/**
 * Created by Vove on 2017/11/7.
 */

public class BingImage {
   private String startDate;
   private String urlBase;
   private String copyRight;

   public BingImage(String startDate, String urlBase) {
      this.startDate = startDate;
      this.urlBase = urlBase;
   }

   public BingImage() {
   }

   public String getStartDate() {
      return startDate;
   }

   public String getUrlBase() {
      return urlBase;
   }

   public void setStartDate(String startDate) {
      this.startDate = startDate;
   }

   public void setUrlBase(String urlBase) {
      this.urlBase = urlBase;
   }

   public String getCopyRight() {
      String[] strs = copyRight.split("©");
      if (strs.length == 2)
         return strs[0].replace('(', ' ') + "\n(©" + strs[1];
      else return copyRight;
   }

   public void setCopyRight(String copyRight) {
      this.copyRight = copyRight;
   }
}
