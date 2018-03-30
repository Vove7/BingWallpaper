package cn.vove7.bingwallpaper.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.activities.ViewImageActivity;
import cn.vove7.bingwallpaper.fragments.GalleryFragment;
import cn.vove7.bingwallpaper.utils.MyApplication;
import cn.vove7.bingwallpaper.utils.Utils;

import java.io.File;
import java.util.ArrayList;

import static cn.vove7.bingwallpaper.services.DownloadService.IMAGE_DIRECTORY;

public class GalleryRecyclerViewAdapter extends RecyclerView.Adapter<GalleryRecyclerViewAdapter.ViewHolder> {

   private final ArrayList<String> imagePaths;
   private final int img_width;
   private final int img_height;
   private GalleryFragment fragment;

   public GalleryRecyclerViewAdapter(GalleryFragment fragment, ArrayList<String> items, int screenWidth, int column_count) {
      this.fragment = fragment;
      img_width = screenWidth / column_count;
      img_height = 1080 * img_width / 1920;
      imagePaths = items;
   }

   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
              .inflate(R.layout.fragment_gallery, parent, false);
      return new ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(final ViewHolder holder, final int position) {
      File file = new File(IMAGE_DIRECTORY + imagePaths.get(position));
      RequestOptions requestOptions = new RequestOptions()
              .centerCrop()
              .override(img_width, img_height)
              .skipMemoryCache(true)
              .error(R.drawable.ic_error_white_48dp);
      Glide.with(holder.mView)
              .load(file).apply(requestOptions)
              .into(holder.mImageView);
      holder.mImageView.setOnClickListener(view -> {
         Intent viewIntent = new Intent(fragment.getContext(), ViewImageActivity.class);
         viewIntent.putExtra("from", ViewPageAdapter.IMAGE_FROM_LOCAL);
         viewIntent.putExtra("startdates", Utils.List2Array(imagePaths));
         viewIntent.putExtra("pos", position);
         fragment.startActivity(viewIntent);
      });

   }

   @Override
   public int getItemCount() {
      return imagePaths.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder {
      public final View mView;
      public final ImageView mImageView;

      public ViewHolder(View view) {
         super(view);
         mView = view;
         mImageView = view.findViewById(R.id.gallery_image);
         mImageView.setMinimumWidth(Utils.getScreenWidth(fragment.getContext())/2);
      }
   }
}
