package cn.vove7.bingwallpaper.utils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Vove on 2017/11/17.
 */

public class BingImages {
   @SerializedName("images")
   private ArrayList<BingImage> images;

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      for (BingImage image : images) {
         builder.append(image);
      }
      return builder.toString();
   }

   public ArrayList<BingImage> getImages() {
      return images;
   }

   public void setImages(ArrayList<BingImage> images) {
      this.images = images;
   }
}
