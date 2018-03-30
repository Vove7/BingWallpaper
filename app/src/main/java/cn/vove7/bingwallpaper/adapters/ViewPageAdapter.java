package cn.vove7.bingwallpaper.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.ArrayList;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.utils.BingImage;
import cn.vove7.bingwallpaper.utils.LogHelper;

import static cn.vove7.bingwallpaper.services.DownloadService.IMAGE_DIRECTORY;
import static cn.vove7.bingwallpaper.utils.Utils.isFileExist;

/**
 * Created by Vove on 2017/11/12.
 * cn.vove7
 */

public class ViewPageAdapter extends PagerAdapter {
   private Context context;
   private ArrayList<BingImage> bingImages;
   private int imageFrom;
   private ProgressBar progressBar;

   public static final int IMAGE_FROM_LOCAL = 0;
   public static final int IMAGE_FROM_INTERNET = 1;

   public ViewPageAdapter(Context context, ArrayList<BingImage> bingImages, int imageFrom) {
      this.context = context;
      this.imageFrom = imageFrom;
      this.bingImages = bingImages;
   }

   @Override
   public int getCount() {
      return bingImages.size();
   }

   @Override
   public void destroyItem(ViewGroup container, int position, Object object) {
      container.removeView((View) object);

   }

   @Override
   public boolean isViewFromObject(View view, Object object) {
      return view == object;
   }

   @NonNull
   @Override
   public Object instantiateItem(ViewGroup container, int position) {
      View view = LayoutInflater.from(context).inflate(R.layout.fragment_view_image_activity, container, false);
      final PhotoView imageView = view.findViewById(R.id.view_image);
      imageView.setMaxScale(3.0f);
      imageView.enable();

      progressBar = view.findViewById(R.id.view_progressbar);

      switch (imageFrom) {
         case IMAGE_FROM_LOCAL: {
            LogHelper.logD(null, "from local***");
            File file = new File(getPath(position));//充当path
            Bitmap bitmap = BitmapFactory.decodeFile(getPath(position));
            glideToView(container, imageView, bitmap, null);
         }
         break;
         case IMAGE_FROM_INTERNET: {
            String filename = getPath(position) + ".jpg";
            LogHelper.logD(null, "filename->" + filename);
            if (isFileExist(filename)) {
               LogHelper.logD(null, "internet from local***");

               Bitmap bitmap = BitmapFactory.decodeFile(filename);
               glideToView(container, imageView, bitmap, null);

            } else {
               LogHelper.logD(null, "from internet***");

               glideToView(container, imageView, null, bingImages.get(position).getUrlBase() + "_1920x1080.jpg");
            }
         }
         break;
         default:
            return null;
      }
      container.addView(view);
      return view;

   }

   private String getPath(int position) {
      return IMAGE_DIRECTORY + "/" + bingImages.get(position).getStartDate();
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

   @SuppressLint("CheckResult")
   private void glideToView(ViewGroup container, ImageView v, Bitmap bitmap, String url) {
      progressBar.setVisibility(View.VISIBLE);
      RequestOptions requestOptions = new RequestOptions()
              .centerCrop()
              .skipMemoryCache(true)
              .error(R.drawable.ic_error_white_48dp);
      RequestBuilder builder;

      if (bitmap == null) {
         requestOptions.override(1920, 1080);
         builder = Glide.with(container)
                 .load(url).apply(requestOptions)
                 .listener(listener);
      } else {
         requestOptions.override(bitmap.getWidth(), bitmap.getHeight());
         requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
                 .skipMemoryCache(true);
         builder = Glide.with(container)
                 .load(bitmap).apply(requestOptions)
                 .listener(listener);

      }
      builder.into(v);
   }

}
