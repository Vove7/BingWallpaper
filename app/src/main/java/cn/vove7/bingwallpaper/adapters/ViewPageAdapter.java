package cn.vove7.bingwallpaper.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.utils.LogHelper;

import static cn.vove7.bingwallpaper.services.DownloadService.IMAGE_DIRECTORY;
import static cn.vove7.bingwallpaper.utils.Utils.isFileExist;

/**
 * Created by Vove on 2017/11/12.
 */

public class ViewPageAdapter extends PagerAdapter {
   private Context context;
   private String[] imagesUrl;
   private String[] startdates;
   private int imageFrom;
   ProgressBar progressBar;

   public static final int IMAGE_FROM_LOCAL = 0;
   public static final int IMAGE_FROM_INTERNET = 1;

   public ViewPageAdapter(Context context, String[] imagesUrl, String[] startdates, int imageFrom) {
      this.context = context;
      this.imagesUrl = imagesUrl;
      this.imageFrom = imageFrom;
      this.startdates = startdates;
   }

   @Override
   public int getCount() {
      if (imagesUrl != null)
         return imagesUrl.length;
      else return startdates.length;
   }

   @Override
   public void destroyItem(ViewGroup container, int position, Object object) {
      container.removeView((View) object);

   }

   @Override
   public boolean isViewFromObject(View view, Object object) {
      return view == object;
   }

   @Override
   public Object instantiateItem(ViewGroup container, int position) {
      View view = LayoutInflater.from(context).inflate(R.layout.fragment_view_image_activity, container, false);
      final ImageView imageView = view.findViewById(R.id.view_image);
      progressBar = view.findViewById(R.id.view_progressbar);

      switch (imageFrom) {
         case IMAGE_FROM_LOCAL: {
            LogHelper.logD(null, "from local***");
            File file = new File(IMAGE_DIRECTORY + startdates[position]);//充当path
            glideToView(container, imageView, file, null);
         }
         break;
         case IMAGE_FROM_INTERNET: {
            String filename = IMAGE_DIRECTORY + "/" + startdates[position] + ".jpg";
            LogHelper.logD(null, "filename->" + filename);
            if (isFileExist(filename)) {
               LogHelper.logD(null, "internet from local***");
               File file = new File(filename);
               glideToView(container, imageView, file, null);
            } else {
               LogHelper.logD(null, "from internet***");
               glideToView(container, imageView, null, imagesUrl[position] + "_1920x1080.jpg");
            }
         }
         break;
         default:
            return null;
      }
      container.addView(view);
      return view;

   }

   private RequestListener listener = new RequestListener() {
      @Override
      public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
         progressBar.setVisibility(View.GONE);
         return false;
      }

      @Override
      public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
         progressBar.setVisibility(View.GONE);
         return false;
      }
   };

   private void glideToView(ViewGroup container, ImageView v, File file, String url) {
      progressBar.setVisibility(View.VISIBLE);
      RequestOptions requestOptions = new RequestOptions()
              .centerCrop()
              .override(1920, 1080)
              .skipMemoryCache(true)
              .error(R.drawable.ic_error_white_48dp);
      RequestBuilder builder;
      if (file == null) {
         builder = Glide.with(container)
                 .load(url).apply(requestOptions)
                 .listener(listener);
      } else {
         requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
                 .skipMemoryCache(true);
         builder = Glide.with(container)
                 .load(file).apply(requestOptions)
                 .listener(listener);

      }
      builder.into(v);
   }

}
