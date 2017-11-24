package cn.vove7.bingwallpaper.utils;


import com.google.gson.annotations.SerializedName;

/**
 * Created by Vove on 2017/11/7.
 * cn.vove7
 */

public class BingImage {
   @SerializedName("enddate")
   private String startDate;
   @SerializedName("urlbase")
   private String urlBase;
   @SerializedName("copyright")
   private String copyRight;
   @SerializedName("hsh")
   private String hsh;

   @Override
   public String toString() {
      return startDate + "\n" + urlBase + "\n" + copyRight + "\n" + hsh + "\n";
   }

   public BingImage(String startDate, String urlBase, String hsh) {
      this.startDate = startDate;
      this.urlBase = urlBase;
      this.hsh = hsh;
   }

   public BingImage(String startDate) {
      this.startDate = startDate;
   }

   public BingImage() {
   }

   public String getStartDate() {
      return startDate;
   }

   public String getUrlBase() {
      return "http://www.bing.com" + urlBase;
   }

   public String getRawUrlBase() {
      return urlBase;
   }
   public void setStartDate(String startDate) {
      this.startDate = startDate;
   }

   public void setUrlBase(String urlBase) {
      this.urlBase = urlBase;
   }

   public String getHsh() {
      return hsh;
   }

   public void setHsh(String hsh) {
      this.hsh = hsh;
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