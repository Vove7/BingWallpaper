package cn.vove7.bingwallpaper.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import cn.vove7.bingwallpaper.activitys.MainActivity;
import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.utils.BingImage;

import static cn.vove7.bingwallpaper.handler.MessageHandler.ACTION_LOAD_MORE;


/**
 * Created by Vove on 2017/1/23.
 * RecViewAdapter
 */

public class RecViewAdapter extends RecyclerView.Adapter<RecViewAdapter.ViewHolder> {
   private ArrayList<BingImage> bingImages;
   private MainActivity mainActivity;

   private View footerView;
   private LinearLayout llLoading;  // 正在加载view
   private LinearLayout llLoadError; // 错误view
   private LinearLayout llLoadedAll; // 加载完view
   private TextView errorTextOfFooter;
   public static final int STATUS_LOADING = 0;
   public static final int STATUS_NET_ERROR = 1;
   public static final int STATUS_ALL_OK = 2;
   public static final int STATUS_XML_ERROR = 3;


   class ViewHolder extends RecyclerView.ViewHolder {
      private ImageView bingImage;
      private TextView copyRightText;
      private TextView startDateText;
      private ProgressBar progressBar;
      private View itemView;


      public ViewHolder(View view) {
         super(view);
         if (footerView == null || itemView != footerView) {
            itemView = view;//
            bingImage = view.findViewById(R.id.bing_image);
            copyRightText = view.findViewById(R.id.copyright_text);
            startDateText = view.findViewById(R.id.startdate_text);
            progressBar = view.findViewById(R.id.progressbar);
         }
         if (footerView != null && footerView == itemView) {
            llLoading = itemView.findViewById(R.id.ll_footer_loading);
            llLoadError = itemView.findViewById(R.id.ll_footer_error);
            errorTextOfFooter = itemView.findViewById(R.id.error_text);
            llLoadError.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                  setFooter(STATUS_LOADING);
                  new Handler().postDelayed(new Runnable() {
                     @Override
                     public void run() {
                        mainActivity.getBingImages(ACTION_LOAD_MORE);
                     }
                  }, 300);
               }
            });
            llLoadedAll = itemView.findViewById(R.id.ll_footer_all_loaded);
            initFooterView();//初始隐藏footview
         }

      }
   }

   public RecViewAdapter(MainActivity activity, ArrayList<BingImage> images) {
      mainActivity = activity;
      bingImages = images;
   }

   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      switch (viewType) {
         case TYPE_ITEM: {
            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cardview_layout, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            //
            holder.itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(final View v) {
                  int pos = holder.getAdapterPosition();
                  Snackbar.make(view, bingImages.get(pos).getCopyRight(), Snackbar.LENGTH_SHORT).show();
               }
            });
            return holder;
         }
         case TYPE_FOOTER: {
            footerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_layout, parent, false);
            return new ViewHolder(footerView);
         }
         default:
            return null;
      }
   }

   public void setFooter(int status) {
      if (footerView == null)
         return;
      footerView.setVisibility(View.VISIBLE);
      switch (status) {
         case STATUS_LOADING: {
            llLoadedAll.setVisibility(View.GONE);
            llLoadError.setVisibility(View.GONE);
            llLoading.setVisibility(View.VISIBLE);
         }
         break;
         case STATUS_XML_ERROR: {
            errorTextOfFooter.setText(R.string.parse_xml_error);
            llLoadedAll.setVisibility(View.GONE);
            llLoadError.setVisibility(View.VISIBLE);
            llLoading.setVisibility(View.GONE);
         }break;
         case STATUS_NET_ERROR: {
            errorTextOfFooter.setText(R.string.no_net_tapme);
            llLoadedAll.setVisibility(View.GONE);
            llLoadError.setVisibility(View.VISIBLE);
            llLoading.setVisibility(View.GONE);
         }
         break;
         case STATUS_ALL_OK: {
            llLoadedAll.setVisibility(View.VISIBLE);
            llLoadError.setVisibility(View.GONE);
            llLoading.setVisibility(View.GONE);
         }
         break;
      }
   }

   private void initFooterView() {
      llLoadedAll.setVisibility(View.GONE);
      llLoadError.setVisibility(View.GONE);
      llLoading.setVisibility(View.GONE);
      footerView.setVisibility(View.GONE);
   }

   @Override
   public void onBindViewHolder(ViewHolder holder, int position) {
      if (getItemViewType(position) == TYPE_ITEM) {
         BingImage image = bingImages.get(position);
         runEnterAnimation(holder.itemView, position);

         RequestOptions requestOptions = new RequestOptions()
                 .centerCrop()
                 .override((int) (1366 * 0.8), (int) (768 * 0.8))
                 .skipMemoryCache(false)
                 .error(R.drawable.ic_error_black_36dp);
         Glide.with(holder.itemView)
                 .load(image.getUrlBase() + "_1366x768.jpg")
                 .apply(requestOptions)
                 .into(holder.bingImage);

         holder.progressBar.setVisibility(View.GONE);
         holder.copyRightText.setText(image.getCopyRight());
         holder.startDateText.setText(image.getStartDate());
      } else {

      }
   }

   private int lastAnimatedPosition = 0;
   private boolean animationsLocked = false;
   private boolean delayEnterAnimation = true;

   private void runEnterAnimation(View view, int position) {


      if (animationsLocked) return;//animationsLocked是布尔类型变量，一开始为false，确保仅屏幕一开始能够显示的item项才开启动画


      if (position > lastAnimatedPosition) {//lastAnimatedPosition是int类型变量，一开始为-1，这两行代码确保了recycleview滚动式回收利用视图时不会出现不连续的效果
         lastAnimatedPosition = position;
         view.setTranslationY(300);//相对于原始位置下方500
         view.setAlpha(0.f);//完全透明
         //每个item项两个动画，从透明到不透明，从下方移动到原来的位置
         //并且根据item的位置设置延迟的时间，达到一个接着一个的效果
         view.animate()
                 .translationY(0).alpha(1.f)//设置最终效果为完全不透明，并且在原来的位置
                 .setStartDelay(delayEnterAnimation ? 50 * (position) : 0)//根据item的位置设置延迟时间，达到依次动画一个接一个进行的效果
                 .setInterpolator(new DecelerateInterpolator(0.5f))//设置动画效果为在动画开始的地方快然后慢
                 .setDuration(500)
                 .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                       animationsLocked = true;//确保仅屏幕一开始能够显示的item项才开启动画，也就是说屏幕下方还没有显示的item项滑动时是没有动画效果
                    }
                 }).start();
      }
   }

   @Override
   public int getItemCount() {
      return bingImages.size() == 0 ? 0 : bingImages.size() + 1;//footer、网络提示
   }

   private static final int TYPE_ITEM = 1;
   private static final int TYPE_FOOTER = 2;

   @Override
   public int getItemViewType(int position) {
      return (position + 1 == getItemCount()) ? TYPE_FOOTER : TYPE_ITEM;

   }

}

